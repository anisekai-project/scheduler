package fr.anisekai.scheduler.tasking.interfaces;

import fr.anisekai.scheduler.commons.ActionPlan;
import fr.anisekai.scheduler.tasking.data.ReservedTaskMeta;
import fr.anisekai.scheduler.tasking.interfaces.structure.Task;
import fr.anisekai.scheduler.tasking.interfaces.structure.TaskFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Contracts used by a class capable of managing tasks.
 *
 * @param <E>
 *         The type of the task.
 */
public interface TaskOrchestrator<E extends Task> {

    /**
     * Retrieve all executable tasks available.
     *
     * @return A list of tasks.
     */
    @NotNull List<E> getTasks();

    /**
     * Try to retrieve an executable task compatible with one of the provided factories.
     *
     * @param supportedFactories
     *         Collection of supported factories.
     *
     * @return A task to execute, if one matched.
     */
    Optional<E> poll(@NotNull Collection<TaskFactory<?, ?>> supportedFactories);

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
     *
     * @return An {@link ActionPlan} containing all actions required to store the results of this method in a
     *         persistance layer.
     */
    default <F extends TaskFactory<I, ?>, I> @NotNull ActionPlan<UUID, ReservedTaskMeta, E> queue(@NotNull Class<F> factoryClass, @NotNull Collection<I> arguments) {

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
     *
     * @return An {@link ActionPlan} containing all actions required to store the results of this method in a
     *         persistance layer.
     */
    <F extends TaskFactory<I, ?>, I> @NotNull ActionPlan<UUID, ReservedTaskMeta, E> queue(@NotNull Class<F> factoryClass, @NotNull Collection<I> arguments, byte priority);

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
     *
     * @return An {@link ActionPlan} containing all actions required to store the results of this method in a
     *         persistance layer.
     */
    default <F extends TaskFactory<I, ?>, I> @NotNull ActionPlan<UUID, ReservedTaskMeta, E> queue(@NotNull F factory, @NotNull Collection<I> arguments) {

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
     *
     * @return An {@link ActionPlan} containing all actions required to store the results of this method in a
     *         persistance layer.
     */
    <F extends TaskFactory<I, ?>, I> @NotNull ActionPlan<UUID, ReservedTaskMeta, E> queue(@NotNull F factory, @NotNull Collection<I> arguments, byte priority);

    /**
     * Notify the provided task factory of the task success, with its raw results.
     *
     * @param task
     *         The successful task.
     * @param data
     *         The raw result of the task.
     * @param <O>
     *         The output type of the task.
     *
     * @return An {@link ActionPlan} containing all actions required to store the results of this method in a
     *         persistance layer.
     */
    default <O> @NotNull ActionPlan<UUID, ReservedTaskMeta, E> resolveSuccess(@NotNull E task, @NotNull String data) {

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
     * @param <O>
     *         The output type of the task.
     *
     * @return An {@link ActionPlan} containing all actions required to store the results of this method in a
     *         persistance layer.
     */
    <O> @NotNull ActionPlan<UUID, ReservedTaskMeta, E> resolveSuccess(@NotNull E task, @NotNull String data, @NotNull Consumer<E> updater);

    /**
     * Notify the provided task factory of the task failure, with its error message.
     *
     * @param task
     *         The successful task.
     * @param reason
     *         The error message that caused the failure.
     *
     * @return An {@link ActionPlan} containing all actions required to store the results of this method in a
     *         persistance layer.
     */
    default @NotNull ActionPlan<UUID, ReservedTaskMeta, E> resolveFailure(@NotNull E task, @NotNull String reason) {

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
     * @return An {@link ActionPlan} containing all actions required to store the results of this method in a
     *         persistance layer.
     */
    @NotNull ActionPlan<UUID, ReservedTaskMeta, E> resolveFailure(@NotNull E task, @NotNull String reason, @NotNull Consumer<E> updater);

}
