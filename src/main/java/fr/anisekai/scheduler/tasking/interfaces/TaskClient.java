package fr.anisekai.scheduler.tasking.interfaces;

import fr.anisekai.scheduler.tasking.interfaces.structure.Task;

import java.util.Optional;

public interface TaskClient<E extends Task> {

    /**
     * Retrieve the next task to execute.
     *
     * @return An optional task.
     */
    Optional<E> poll();

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
    void onSuccess(E task, String result);

    /**
     * Called whenever a task fails during execution.
     *
     * @param task
     *         The successful task.
     * @param throwable
     *         The error that caused the failure.
     */
    void onFailure(E task, Throwable throwable);

}
