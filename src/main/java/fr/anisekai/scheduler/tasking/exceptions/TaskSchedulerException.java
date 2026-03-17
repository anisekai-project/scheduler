package fr.anisekai.scheduler.tasking.exceptions;

/**
 * Generic exception used by all sub-exception regarding the task scheduling module.
 */
public class TaskSchedulerException extends RuntimeException {

    /**
     * Create a new {@link TaskSchedulerException} instance.
     *
     * @param message
     *         The error message.
     */
    public TaskSchedulerException(String message) {

        super(message);
    }

}
