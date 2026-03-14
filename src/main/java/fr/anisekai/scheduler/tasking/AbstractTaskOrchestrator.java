package fr.anisekai.scheduler.tasking;

import fr.anisekai.scheduler.commons.ActionPlan;
import fr.anisekai.scheduler.tasking.data.BookedTask;
import fr.anisekai.scheduler.tasking.enums.TaskStatus;
import fr.anisekai.scheduler.tasking.exceptions.TaskSchedulerException;
import fr.anisekai.scheduler.tasking.interfaces.TaskOrchestrator;
import fr.anisekai.scheduler.tasking.interfaces.structure.Task;
import fr.anisekai.scheduler.tasking.interfaces.structure.TaskFactory;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.*;
import java.util.function.Consumer;

public abstract class AbstractTaskOrchestrator<ID, E extends Task> extends FactoryAware<E> implements TaskOrchestrator<ID, E> {

    private final int maxFailures;

    public AbstractTaskOrchestrator(@NotNull Set<TaskFactory<E, ?, ?>> factories, int maxFailures) {

        super(factories);
        this.maxFailures = maxFailures;
    }

    @Override
    public Optional<E> poll(Collection<TaskFactory<E, ?, ?>> supportedFactories) {

        List<String> supportedFactoryNames = supportedFactories.stream().map(TaskFactory::getName).toList();

        return this.getTasks()
                   .stream()
                   .filter(task -> task.getStatus() == TaskStatus.SCHEDULED)
                   .filter(task -> supportedFactoryNames.contains(task.getFactoryName()))
                   .min(Comparator.comparing(Task::getCreatedAt));
    }

    @Override
    public <F extends TaskFactory<E, I, ?>, I> ActionPlan<ID, Task, E> queue(Class<F> factoryClass, Collection<I> arguments, byte priority) {

        F factory = this.getFactory(factoryClass);
        return this.queue(factory, arguments, priority);
    }

    @Override
    public <F extends TaskFactory<E, I, ?>, I> ActionPlan<ID, Task, E> queue(F factory, Collection<I> arguments, byte priority) {

        List<E>                         existingTasks = this.getTasks();
        ActionPlan.Builder<ID, Task, E> plan          = new ActionPlan.Builder<>();

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
                        plan.update(this.extractIdentifier(task), t -> t.setPriority(priority)).build();
                    }
                    continue;
                }
            }

            String rawArguments = factory.getArgumentsSerializer().serialize(argument);
            plan.create(new BookedTask(name, factory.getName(), priority, rawArguments));
        }

        return plan.build();
    }

    @Override
    public <O> ActionPlan<ID, Task, E> resolveSuccess(E task, String data, Consumer<E> updater) {

        if (task.getStatus() != TaskStatus.EXECUTING) {
            throw new TaskSchedulerException("Cannot resolve task success: Task is not flagged as executing.");
        }

        TaskFactory<E, ?, O> factory = (TaskFactory<E, ?, O>) this.getFactory(task.getFactoryName());
        O                    results = factory.getResultSerializer().deserialize(data);

        factory.onSuccess(task, results);

        return new ActionPlan.Builder<ID, Task, E>()
                .update(
                        this.extractIdentifier(task),
                        item -> {
                            item.setStatus(TaskStatus.SUCCEEDED);
                            item.setCompletedAt(Instant.now());
                            updater.accept(item);
                        }
                ).build();
    }

    @Override
    public ActionPlan<ID, Task, E> resolveFailure(E task, String reason, Consumer<E> updater) {

        if (task.getStatus() != TaskStatus.EXECUTING) {
            throw new TaskSchedulerException("Cannot resolve task success: Task is not flagged as executing.");
        }

        TaskFactory<E, ?, ?> factory = this.getFactory(task.getFactoryName());

        factory.onFailure(task, reason);

        return new ActionPlan.Builder<ID, Task, E>()
                .update(
                        this.extractIdentifier(task),
                        item -> {
                            item.setStartedAt(null);
                            item.setFailureCount((byte) (item.getFailureCount() + 1));
                            item.setStatus(item.getFailureCount() >= this.maxFailures ? TaskStatus.FAILED : TaskStatus.SCHEDULED);
                            updater.accept(item);
                        }
                ).build();
    }

}
