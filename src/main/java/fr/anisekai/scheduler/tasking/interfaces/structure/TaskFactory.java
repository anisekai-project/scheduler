package fr.anisekai.scheduler.tasking.interfaces.structure;

import fr.anisekai.scheduler.commons.interfaces.ObjectSerializer;
import fr.anisekai.scheduler.tasking.data.TaskMeta;
import fr.anisekai.scheduler.tasking.enums.TaskStatus;
import org.jetbrains.annotations.NotNull;


/**
 * Defines how to create, execute, and handle results for a specific task type.
 */
public interface TaskFactory<I, O> {

    /**
     * Unique identifier for this type of task.
     */
    @NotNull String getName();

    /**
     * Retrieve the executor allowing to run a task.
     *
     * @return A {@link TaskExecutor}.
     */
    @NotNull TaskExecutor<I, O> getExecutor();

    /**
     * Execute the provided task.
     *
     * @param task
     *         The task to execute.
     *
     * @return The raw result of the task.
     */
    @NotNull String execute(@NotNull TaskMeta task) throws Exception;

    /**
     * Generate a task name from the given arguments. Most of the time implementation would keep the task name same as
     * the factory name, unless {@link #allowDuplicated()} returns {@code true}.
     *
     * @param arguments
     *         The input arguments
     *
     * @return The task name.
     */
    @NotNull String getTaskName(@NotNull I arguments);

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
     *
     * @return {@code false} if duplicated are not allowed, {@code true} otherwise.
     */
    default boolean allowDuplicated() {

        return false;
    }

}