package fr.anisekai.scheduler.tasking;

import fr.anisekai.scheduler.commons.ActionPlan;
import fr.anisekai.scheduler.tasking.data.ReservedTaskMeta;
import fr.anisekai.scheduler.tasking.data.TaskMeta;
import fr.anisekai.scheduler.tasking.enums.TaskStatus;
import fr.anisekai.scheduler.tasking.exceptions.NonExecutingTaskException;
import fr.anisekai.scheduler.tasking.interfaces.TaskOrchestrator;
import fr.anisekai.scheduler.tasking.interfaces.structure.Task;
import fr.anisekai.scheduler.tasking.interfaces.structure.TaskFactory;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

/**
 * Minimal implementation of a task orchestrator, providing a default behavior for a general purpose task system.
 *
 * @param <E>
 *         The type of the task.
 */
public abstract class AbstractTaskOrchestrator<E extends Task> extends FactoryAware implements TaskOrchestrator<E> {

    private final int maxFailures;

    /**
     * Create a new {@link AbstractTaskOrchestrator} instance.
     *
     * @param factories
     *         A set of {@link TaskFactory} this orchestrator will use.
     * @param maxFailures
     *         Maximum amount of failure allowed for a task before switching to the status {@link TaskStatus#FAILED}.
     */
    public AbstractTaskOrchestrator(@NotNull Set<TaskFactory<?, ?>> factories, int maxFailures) {

        super(factories);
        this.maxFailures = maxFailures;
    }

    @Override
    public Optional<E> poll(@NotNull Collection<TaskFactory<?, ?>> supportedFactories) {

        List<String> supportedFactoryNames = supportedFactories.stream().map(TaskFactory::getName).toList();

        return this.getTasks()
                   .stream()
                   .filter(task -> task.getStatus() == TaskStatus.SCHEDULED)
                   .filter(task -> supportedFactoryNames.contains(task.getFactoryName()))
                   .min(Comparator.comparing(Task::getCreatedAt));
    }

    @Override
    public <F extends TaskFactory<I, ?>, I> @NotNull ActionPlan<UUID, ReservedTaskMeta, E> queue(@NotNull Class<F> factoryClass, @NotNull Collection<I> arguments, byte priority) {

        F factory = this.getFactory(factoryClass);
        return this.queue(factory, arguments, priority);
    }

    @Override
    public <F extends TaskFactory<I, ?>, I> @NotNull ActionPlan<UUID, ReservedTaskMeta, E> queue(@NotNull F factory, @NotNull Collection<I> arguments, byte priority) {

        List<E>                                       existingTasks = this.getTasks();
        ActionPlan.Builder<UUID, ReservedTaskMeta, E> plan          = new ActionPlan.Builder<>();

        for (I argument : arguments) {
            String name = factory.getTaskName(argument);

            if (!factory.allowDuplicated()) {
                Optional<E> existing = existingTasks
                        .stream()
                        .filter(t -> t.getStatus() == TaskStatus.SCHEDULED)
                        .filter(t -> t.getName().equals(name) && t.getFactoryName().equals(factory.getName()))
                        .findFirst();

                if (existing.isPresent()) {
                    E task = existing.get();
                    if (task.getPriority() < priority) {
                        plan.update(task.getId(), t -> t.setPriority(priority)).build();
                    }
                    continue;
                }
            }

            String rawArguments = factory.getArgumentsSerializer().serialize(argument);
            plan.create(new ReservedTaskMeta(factory.getName(), name, rawArguments, priority));
        }

        return plan.build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <O> @NotNull ActionPlan<UUID, ReservedTaskMeta, E> resolveSuccess(@NotNull E task, @NotNull String data, @NotNull Consumer<E> updater) {

        if (task.getStatus() != TaskStatus.EXECUTING) {
            throw new NonExecutingTaskException();
        }

        TaskFactory<?, O> factory = (TaskFactory<?, O>) this.getFactory(task.getFactoryName());
        O                 results = factory.getResultSerializer().deserialize(data);
        TaskMeta          meta    = TaskMeta.of(task);

        factory.onSuccess(meta, results);

        return new ActionPlan.Builder<UUID, ReservedTaskMeta, E>()
                .update(
                        task.getId(),
                        item -> {
                            item.setStatus(TaskStatus.SUCCEEDED);
                            item.setCompletedAt(Instant.now());
                            updater.accept(item);
                        }
                ).build();
    }

    @Override
    public @NotNull ActionPlan<UUID, ReservedTaskMeta, E> resolveFailure(@NotNull E task, @NotNull String reason, @NotNull Consumer<E> updater) {

        if (task.getStatus() != TaskStatus.EXECUTING) {
            throw new NonExecutingTaskException();
        }

        TaskFactory<?, ?> factory = this.getFactory(task.getFactoryName());
        TaskMeta          meta    = TaskMeta.of(task);

        factory.onFailure(meta, reason);

        return new ActionPlan.Builder<UUID, ReservedTaskMeta, E>()
                .update(
                        task.getId(),
                        item -> {
                            item.setStartedAt(null);
                            item.setFailureCount((byte) (item.getFailureCount() + 1));
                            item.setStatus(item.getFailureCount() >= this.maxFailures ? TaskStatus.FAILED : TaskStatus.SCHEDULED);
                            updater.accept(item);
                        }
                ).build();
    }

}
