package fr.anisekai.scheduler.tasking.interfaces;

import fr.anisekai.scheduler.tasking.data.TaskMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Contract used by a class capable of executing tasks.
 */
public interface TaskClient {

    /**
     * Retrieve the next task to execute.
     *
     * @return An optional task.
     */
    Optional<TaskMeta> poll();

    /**
     * Check if a task can be executed and execute it if found.
     */
    void tick();

    /**
     * Called whenever a task is successfully executed.
     *
     * @param task
     *         The successful task.
     * @param result
     *         The task results.
     */
    default void onSuccess(@NotNull TaskMeta task, @NotNull String result) {

    }

    /**
     * Called whenever a task fails during execution.
     *
     * @param task
     *         The successful task.
     * @param throwable
     *         The error that caused the failure.
     */
    default void onFailure(@NotNull TaskMeta task, @NotNull Throwable throwable) {

    }

}
