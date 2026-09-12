package fr.anisekai.scheduler.event.exceptions;

import fr.anisekai.scheduler.event.interfaces.entities.Planifiable;

/**
 * Exception thrown when attempting to schedule a {@link Planifiable} with an invalid or non-positive duration.
 * <p>
 * This exception is considered safe to expose to end users.
 */
public class InvalidSchedulingDurationException extends EventSchedulerException {

    /**
     * Constructs a new {@link InvalidSchedulingDurationException} with a default error message.
     */
    public InvalidSchedulingDurationException() {

        super("Unable to schedule an event with an invalid duration.");
    }

    /**
     * Constructs a new exception with a precise validation message.
     *
     * @param message
     *         Description of the invalid temporal input.
     */
    public InvalidSchedulingDurationException(String message) {

        super(message);
    }

}
