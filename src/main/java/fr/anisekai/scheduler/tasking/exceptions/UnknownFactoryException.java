package fr.anisekai.scheduler.tasking.exceptions;

import fr.anisekai.scheduler.tasking.interfaces.factories.Factory;
import fr.anisekai.scheduler.tasking.interfaces.factories.FactoryRegistry;

/**
 * Exception thrown when a {@link FactoryRegistry} was not able to find a requested {@link Factory}.
 */
public class UnknownFactoryException extends TaskSchedulerException {

    /**
     * Create a new {@link UnknownFactoryException} instance.
     *
     * @param factory
     *         The factory class that was requested.
     */
    public UnknownFactoryException(Class<? extends Factory<?, ?>> factory) {

        super(String.format(
                "Unknown factory %s. Perhaps you forgot to call `registerFactory` ?",
                factory.getName()
        ));
    }

    /**
     * Create a new {@link UnknownFactoryException} instance.
     *
     * @param name
     *         The factory name that was requested.
     */
    public UnknownFactoryException(String name) {

        super(String.format(
                "Unknown factory %s. Perhaps you forgot to call `registerFactory` or there is a typo ?",
                name
        ));
    }


}
