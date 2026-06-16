package fr.anisekai.scheduler.tasking.exceptions;

import fr.anisekai.scheduler.tasking.interfaces.FactoryRegistry;
import fr.anisekai.scheduler.tasking.interfaces.structure.TaskFactory;

/**
 * Exception thrown when a {@link fr.anisekai.scheduler.tasking.FactoryAware} was not able to find a requested
 * {@link TaskFactory}. This is also the exception recommended for any implementation of {@link FactoryRegistry}.
 */
public class FactoryAlreadyRegisteredException extends TaskSchedulerException {

    /**
     * Create a new {@link FactoryAlreadyRegisteredException} instance.
     *
     * @param factory
     *         The factory class that was requested.
     */
    public FactoryAlreadyRegisteredException(Class<? extends TaskFactory<?, ?>> factory) {

        super(String.format(
                "Unknown factory %s. Perhaps you forgot to call `registerFactory` ?",
                factory.getName()
        ));
    }

    /**
     * Create a new {@link FactoryAlreadyRegisteredException} instance.
     *
     * @param name
     *         The factory name that was requested.
     */
    public FactoryAlreadyRegisteredException(String name) {

        super(String.format(
                "Unknown factory %s. Perhaps you forgot to call `registerFactory` or there is a typo ?",
                name
        ));
    }


}
