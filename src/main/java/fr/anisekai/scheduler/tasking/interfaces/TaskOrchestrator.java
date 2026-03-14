package fr.anisekai.scheduler.tasking.interfaces;

import fr.anisekai.scheduler.commons.ActionPlan;
import fr.anisekai.scheduler.tasking.interfaces.structure.Task;
import fr.anisekai.scheduler.tasking.interfaces.structure.TaskFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public interface TaskOrchestrator<ID, E extends Task> {

    /**
     * Retrieve the identifier of the provided task.
     *
     * @param task
     *         The task from which the identifier should be determined.
     *
     * @return An identifier.
     */
    ID extractIdentifier(E task);

    /**
     * Retrieve all executable tasks available.
     *
     * @return A list of tasks.
     */
    List<E> getTasks();

    /**
     * Try to retrieve an executable task compatible with one of the provided factories.
     *
     * @param supportedFactories
     *         Collection of supported factories.
     *
     * @return A task to execute, if one matched.
     */
    Optional<E> poll(Collection<TaskFactory<E, ?, ?>> supportedFactories);

    /**
     * Queue one or more tasks from the provided factory.
     *
     * @param factoryClass
     *         The {@link TaskFactory} class for which the tasks will be queued.
     * @param arguments
     *         The arguments collection, one for each task.
     * @param <F>
     *         The factory type.
     * @param <I>
     *         The argument type.
     */
    default <F extends TaskFactory<E, I, ?>, I> ActionPlan<ID, Task, E> queue(Class<F> factoryClass, Collection<I> arguments) {

        return this.queue(factoryClass, arguments, Task.PRIORITY_DEFAULT);
    }

    /**
     * Queue one or more tasks from the provided factory.
     *
     * @param factoryClass
     *         The {@link TaskFactory} class for which the tasks will be queued.
     * @param arguments
     *         The arguments collection, one for each task.
     * @param priority
     *         The priority for the queued tasks.
     * @param <F>
     *         The factory type.
     * @param <I>
     *         The argument type.
     */
    <F extends TaskFactory<E, I, ?>, I> ActionPlan<ID, Task, E> queue(Class<F> factoryClass, Collection<I> arguments, byte priority);

    /**
     * Queue one or more tasks from the provided factory.
     *
     * @param factory
     *         The {@link TaskFactory} for which the tasks will be queued.
     * @param arguments
     *         The arguments collection, one for each task.
     * @param <F>
     *         The factory type.
     * @param <I>
     *         The argument type.
     */
    default <F extends TaskFactory<E, I, ?>, I> ActionPlan<ID, Task, E> queue(F factory, Collection<I> arguments) {

        return this.queue(factory, arguments, Task.PRIORITY_DEFAULT);
    }

    /**
     * Queue one or more tasks from the provided factory.
     *
     * @param factory
     *         The {@link TaskFactory} for which the tasks will be queued.
     * @param arguments
     *         The arguments collection, one for each task.
     * @param priority
     *         The priority for the queued tasks.
     * @param <F>
     *         The factory type.
     * @param <I>
     *         The argument type.
     */
    <F extends TaskFactory<E, I, ?>, I> ActionPlan<ID, Task, E> queue(F factory, Collection<I> arguments, byte priority);

    /**
     * Notify the provided task factory of the task success, with its raw results.
     *
     * @param task
     *         The successful task.
     * @param data
     *         The raw result of the task.
     *
     * @return An {@link ActionPlan} allowing to update the task.
     */
    default <O> ActionPlan<ID, Task, E> resolveSuccess(E task, String data) {

        return this.resolveSuccess(task, data, _ -> {});
    }

    /**
     * Notify the provided task factory of the task success, with its raw results.
     *
     * @param task
     *         The successful task.
     * @param data
     *         The raw result of the task.
     * @param updater
     *         Consumer called when the task is being updated, allowing further customization over the task properties.
     *
     * @return An {@link ActionPlan} allowing to update the task.
     */
    <O> ActionPlan<ID, Task, E> resolveSuccess(E task, String data, Consumer<E> updater);

    /**
     * Notify the provided task factory of the task failure, with its error message.
     *
     * @param task
     *         The successful task.
     * @param reason
     *         The error message that caused the failure.
     *
     * @return An {@link ActionPlan} allowing to update the task.
     */
    default ActionPlan<ID, Task, E> resolveFailure(E task, String reason) {

        return this.resolveFailure(task, reason, _ -> {});
    }

    /**
     * Notify the provided task factory of the task failure, with its error message.
     *
     * @param task
     *         The successful task.
     * @param reason
     *         The error message that caused the failure.
     * @param updater
     *         Consumer called when the task is being updated, allowing further customization over the task properties.
     *
     * @return An {@link ActionPlan} allowing to update the task.
     */
    ActionPlan<ID, Task, E> resolveFailure(E task, String reason, Consumer<E> updater);

}
