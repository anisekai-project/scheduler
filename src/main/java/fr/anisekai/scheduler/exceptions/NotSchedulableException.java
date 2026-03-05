package fr.anisekai.scheduler.exceptions;

/**
 * Exception thrown when an event cannot be scheduled due to unavailability.
 * <p>
 * This may happen if no suitable time slot was found or if the requested time is already occupied.
 * </p>
 */
public class NotSchedulableException extends RuntimeException {

    /**
     * Create a {@link NotSchedulableException} with a default message indicating that no valid scheduling slot is
     * available.
     */
    public NotSchedulableException() {

        super("No free spot found or the spot indicated is not available for scheduling.");
    }

}
