package fr.anisekai.scheduler.event.exceptions;

import fr.anisekai.scheduler.event.interfaces.entities.Planifiable;

/**
 * Exception thrown when attempting to delay {@link Planifiable} in a way that would cause overlap with existing
 * {@link Planifiable}.
 */
public class DelayOverlapException extends EventSchedulerException {

    /**
     * Creates a new {@code DelayOverlapException} with the specified detail message.
     *
     * @param message
     *         The detail message describing the cause of the exception
     */

    public DelayOverlapException(String message) {

        super(message);
    }

}
