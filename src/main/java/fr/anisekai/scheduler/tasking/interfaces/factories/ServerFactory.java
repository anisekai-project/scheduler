package fr.anisekai.scheduler.tasking.interfaces.factories;

import fr.anisekai.scheduler.tasking.data.TaskExecutedPacket;
import fr.anisekai.scheduler.tasking.data.TaskExecutionPacket;
import fr.anisekai.scheduler.tasking.data.TaskFailedPacket;
import fr.anisekai.scheduler.tasking.enums.TaskStatus;
import fr.anisekai.scheduler.tasking.interfaces.structure.TaskInterface;
import org.jetbrains.annotations.NotNull;

/**
 * Interface allowing any {@link Factory} to listen for specific server-side events related to tasks.
 *
 * @param <T>
 *         The task type
 * @param <I>
 *         The input argument type
 * @param <O>
 *         The output result type
 */
public non-sealed interface ServerFactory<T extends TaskInterface, I, O> extends Factory<I, O> {

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
     * Called when a task is attributed to a worker.
     *
     * @param packet
     *         The packet containing the task and its input.
     */
    default void onAssigningTask(@NotNull TaskExecutionPacket<T> packet) {

    }

    /**
     * Handle the result of a successful task.
     *
     * @param packet
     *         The packet containing the task, its input and the result.
     */
    default void onSuccess(@NotNull TaskExecutedPacket<T, O> packet) {

    }

    /**
     * Handle the failure on a task.
     *
     * @param packet
     *         The packet containing the task, its input and the error.
     */
    default void onFailure(@NotNull TaskFailedPacket<T> packet) {

    }

}
