package fr.anisekai.scheduler.tasking.interfaces.structure;

import fr.anisekai.scheduler.commons.interfaces.ObjectSerializer;
import fr.anisekai.scheduler.tasking.data.TaskMeta;
import fr.anisekai.scheduler.tasking.enums.TaskStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Defines how to create, execute, and handle results for a specific task type.
 *
 * @param <I>
 *         The input argument type
 * @param <O>
 *         The output result type
 */
public interface TaskFactory<I, O> {

    /**
     * Retrieve this factory name that will be tied to any task created for this factory.
     * <p>
     * <b>Note:</b> There is no defensive checks against duplicated factory name. It is the developer responsibility to
     * ensure uniqueness.
     *
     * @return The factory name.
     */
    @NotNull String getName();

    /**
     * Generate a task name from the given arguments. Most of the time, implementation will keep the task name same as
     * the factory name, unless {@link #allowDuplicated()} returns {@code true}, in which case multiple tasks from the
     * same factory might exist at the same time, depending on their input arguments.
     *
     * @param arguments
     *         The input arguments
     *
     * @return The task name.
     */
    default @NotNull String getTaskName(@NotNull I arguments) {

        return this.getName();
    }

    /**
     * Retrieve the {@link ObjectSerializer} instance allowing to manage the input arguments of each task of this
     * factory.
     *
     * @return An {@link ObjectSerializer} instance.
     */
    @NotNull ObjectSerializer<I> getArgumentsSerializer();

    /**
     * Retrieve the {@link ObjectSerializer} instance allowing to manage the output result of each task of this
     * factory.
     *
     * @return An {@link ObjectSerializer} instance.
     */
    @NotNull ObjectSerializer<O> getResultSerializer();

    /**
     * Handle the result of a successful task.
     *
     * @param task
     *         The successful task.
     * @param result
     *         The result of the task.
     */
    default void onSuccess(@NotNull TaskMeta task, @NotNull O result) {

    }

    /**
     * Handle the failure on a task.
     *
     * @param task
     *         The failing task.
     * @param error
     *         The reason.
     */
    default void onFailure(@NotNull TaskMeta task, @NotNull String error) {

    }

    /**
     * Check if this factory allows duplicated task names.
     * <p>
     * If this method returns {@code false}, which is the default behavior, the task scheduler will ensure no task can
     * be scheduled with the same name exclusively if the already scheduled task has the status
     * {@link TaskStatus#SCHEDULED}, even if the input arguments aren't equals. It will, however, update the already
     * scheduled task priority if the new one requires a higher priority.
     * <p>
     * <b>Note:</b> If this method returns {@code true}, you might also want to override {@link #getTaskName(Object)}
     * to ensure no conflict arise when queuing tasks.
     *
     * @return {@code false} if duplicated are not allowed, {@code true} otherwise.
     */
    default boolean allowDuplicated() {

        return false;
    }

}