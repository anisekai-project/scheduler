package fr.anisekai.scheduler.tasking.exceptions;

import fr.anisekai.scheduler.tasking.FactoryAware;
import fr.anisekai.scheduler.tasking.interfaces.TaskFactoryAware;
import fr.anisekai.scheduler.tasking.interfaces.structure.TaskFactory;

/**
 * Exception thrown when a {@link FactoryAware} was not able to find a requested {@link TaskFactory}. This is also the
 * exception recommended for any implementation of {@link TaskFactoryAware}.
 */
public class UnknownFactoryException extends TaskSchedulerException {

    /**
     * Create a new {@link UnknownFactoryException} instance.
     *
     * @param factory
     *         The factory class that was requested.
     */
    public UnknownFactoryException(Class<? extends TaskFactory<?, ?>> factory) {

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
