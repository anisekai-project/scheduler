package fr.anisekai.scheduler.tasking.exceptions;

import fr.anisekai.scheduler.tasking.enums.TaskStatus;

/**
 * Exception thrown when an attempt is made to notify a task resolution (success or failure) when the task status was
 * not set to {@link TaskStatus#EXECUTING}.
 */
public class NonExecutingTaskException extends TaskSchedulerException {

    /**
     * Create a new {@link NonExecutingTaskException}
     */
    public NonExecutingTaskException() {

        super("Cannot resolve task success: Task is not flagged as executing.");
    }

}
