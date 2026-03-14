package fr.anisekai.scheduler.tasking.exceptions;

import fr.anisekai.scheduler.tasking.interfaces.structure.TaskFactory;

public class UnknownFactoryException extends TaskSchedulerException {

    public UnknownFactoryException(Class<? extends TaskFactory<?, ?, ?>> factory) {

        super(String.format(
                "Unknown factory %s. Perhaps you forgot to call `registerFactory` ?",
                factory.getName()
        ));
    }

    public UnknownFactoryException(String name) {

        super(String.format(
                "Unknown factory %s. Perhaps you forgot to call `registerFactory` or there is a typo ?",
                name
        ));
    }


}
