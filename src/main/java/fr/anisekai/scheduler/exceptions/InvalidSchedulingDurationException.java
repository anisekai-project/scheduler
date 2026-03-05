package fr.anisekai.scheduler.exceptions;

import fr.anisekai.scheduler.interfaces.entities.Planifiable;

/**
 * Exception thrown when attempting to schedule a {@link Planifiable} with an invalid or non-positive duration.
 * <p>
 * This exception is considered safe to expose to end users.
 */
public class InvalidSchedulingDurationException extends RuntimeException {

    /**
     * Constructs a new {@link InvalidSchedulingDurationException} with a default error message.
     */
    public InvalidSchedulingDurationException() {

        super("Unable to schedule an event with an invalid duration.");
    }

}
