package fr.anisekai.scheduler.event.exceptions;

/**
 * Generic exception used by all sub-exception regarding the event scheduling module.
 */
public class EventSchedulerException extends RuntimeException {

    /**
     * Create a new {@link EventSchedulerException} instance.
     *
     * @param message
     *         The error message.
     */
    public EventSchedulerException(String message) {

        super(message);
    }

}
