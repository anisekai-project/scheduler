package fr.anisekai.scheduler.tasking;

import fr.anisekai.scheduler.commons.ActionPlan;
import fr.anisekai.scheduler.tasking.data.ReservedTaskMeta;
import fr.anisekai.scheduler.tasking.data.TaskExecutedPacket;
import fr.anisekai.scheduler.tasking.data.TaskExecutionPacket;
import fr.anisekai.scheduler.tasking.data.TaskFailedPacket;
import fr.anisekai.scheduler.tasking.enums.TaskStatus;
import fr.anisekai.scheduler.tasking.exceptions.NonExecutingTaskException;
import fr.anisekai.scheduler.tasking.interfaces.factories.Factory;
import fr.anisekai.scheduler.tasking.interfaces.factories.FactoryRegistry;
import fr.anisekai.scheduler.tasking.interfaces.factories.ServerFactory;
import fr.anisekai.scheduler.tasking.interfaces.orchestrator.ServerOrchestrator;
import fr.anisekai.scheduler.tasking.interfaces.structure.TaskClient;
import fr.anisekai.scheduler.tasking.interfaces.structure.TaskInterface;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.*;

/**
 * Minimal implementation of a task orchestrator, providing a default behavior for a general purpose task system.
 *
 * @param <E>
 *         The type of the task.
 */
public abstract class AbstractServerOrchestrator<E extends TaskInterface> implements ServerOrchestrator<E> {

    private static final Comparator<TaskInterface> POLL_COMPARATOR = Comparator
            .comparing(TaskInterface::getPriority)
            .thenComparing(TaskInterface::getCreatedAt, Comparator.reverseOrder());

    private final int                                     maxFailures;
    private final FactoryRegistry<ServerFactory<E, ?, ?>> registry;

    /**
     * Create a new {@link AbstractServerOrchestrator} instance.
     *
     * @param registry
     *         A {@link FactoryRegistry} implementation allowing to query for factories.
     * @param maxFailures
     *         Maximum amount of failure allowed for a task before switching to the status {@link TaskStatus#FAILED}.
     */
    public AbstractServerOrchestrator(FactoryRegistry<ServerFactory<E, ?, ?>> registry, int maxFailures) {

        this.registry    = registry;
        this.maxFailures = maxFailures;
    }

    @Override
    public Optional<E> poll(@NotNull TaskClient client) {

        List<@NotNull String> supportedFactoryNames = client.getSupportedFactories()
                                                            .stream()
                                                            .map(Factory::getName)
                                                            .toList();

        Optional<E> optionalTask = this.getTasks()
                                       .stream()
                                       .filter(task -> task.getStatus() == TaskStatus.SCHEDULED)
                                       .filter(task -> supportedFactoryNames.contains(task.getFactoryName()))
                                       .max(POLL_COMPARATOR);

        optionalTask.ifPresent(task -> {
            ServerFactory<E, ?, ?> factory = this.registry.query(task.getFactoryName());
            factory.onAssigningTask(new TaskExecutionPacket<>(task));
        });

        return optionalTask;
    }

    @Override
    public @NotNull <F extends ServerFactory<E, I, ?>, I> ActionPlan<UUID, ReservedTaskMeta, E> queue(@NotNull Class<F> factoryClass, @NotNull Collection<I> arguments, byte priority) {

        F factory = this.registry.query(factoryClass);
        return this.queue(factory, arguments, priority);
    }

    @Override
    public @NotNull <F extends ServerFactory<E, I, ?>, I> ActionPlan<UUID, ReservedTaskMeta, E> queue(@NotNull F factory, @NotNull Collection<I> arguments, byte priority) {

        List<E>                                       tasks = this.getTasks();
        ActionPlan.Builder<UUID, ReservedTaskMeta, E> plan  = new ActionPlan.Builder<>();
        Map<String, E>                                existingTasks = new HashMap<>();
        Set<String>                                   queuedNames   = new HashSet<>();
        Set<String>                                   updatedNames  = new HashSet<>();
        boolean                                       allowDuplicated = factory.allowDuplicated();

        if (!allowDuplicated) {
            tasks.stream()
                 .filter(task -> task.getStatus() == TaskStatus.SCHEDULED)
                 .filter(task -> task.getFactoryName().equals(factory.getName()))
                 .forEach(task -> existingTasks.putIfAbsent(task.getName(), task));
            queuedNames.addAll(existingTasks.keySet());
        }

        for (I argument : arguments) {
            String name = factory.getTaskName(argument);

            if (!allowDuplicated && !queuedNames.add(name)) {
                E task = existingTasks.get(name);
                if (task != null && task.getPriority() < priority && updatedNames.add(name)) {
                    plan.update(task.getId(), t -> t.setPriority(priority));
                }
                continue;
            }

            String rawArguments = factory.getArgumentsSerializer().serialize(argument);
            plan.create(new ReservedTaskMeta(factory.getName(), name, rawArguments, priority));
        }

        return plan.build();
    }

    @Override
    public @NotNull <I> ActionPlan<UUID, ReservedTaskMeta, E> resolve(@NotNull TaskFailedPacket<E> packet) {

        if (packet.task().getStatus() != TaskStatus.EXECUTING) {
            throw new NonExecutingTaskException();
        }

        ServerFactory<E, I, ?> factory = (ServerFactory<E, I, ?>) this.registry.query(packet.task().getFactoryName());

        return new ActionPlan.Builder<UUID, ReservedTaskMeta, E>()
                .update(
                        packet.task().getId(),
                        item -> {
                            factory.onFailure(new TaskFailedPacket<>(item, packet.exception()));
                            item.setStartedAt(null);
                            item.setFailureCount((byte) (item.getFailureCount() + 1));
                            item.setStatus(item.getFailureCount() >= this.maxFailures ? TaskStatus.FAILED : TaskStatus.SCHEDULED);
                        }
                ).build();
    }

    @Override
    public @NotNull <I, R> ActionPlan<UUID, ReservedTaskMeta, E> resolve(@NotNull TaskExecutedPacket<E, R> packet) {

        if (packet.task().getStatus() != TaskStatus.EXECUTING) {
            throw new NonExecutingTaskException();
        }

        ServerFactory<E, I, R> factory = (ServerFactory<E, I, R>) this.registry.query(packet.task().getFactoryName());

        return new ActionPlan.Builder<UUID, ReservedTaskMeta, E>()
                .update(
                        packet.task().getId(),
                        item -> {
                            factory.onSuccess(new TaskExecutedPacket<>(item, packet.result()));
                            item.setStatus(TaskStatus.SUCCEEDED);
                            item.setCompletedAt(Instant.now());
                        }
                ).build();
    }

}
