package fr.anisekai.scheduler.tasking.interfaces;

import fr.anisekai.scheduler.tasking.exceptions.UnknownFactoryException;
import fr.anisekai.scheduler.tasking.interfaces.structure.TaskFactory;
import org.jetbrains.annotations.NotNull;

public interface TaskFactoryAware {

    /**
     * Retrieve a specific factory from this factory aware object.
     *
     * @param name
     *         The factory name
     *
     * @return The {@link TaskFactory} instance.
     *
     * @throws UnknownFactoryException
     *         if the factory class did not match any registered factories.
     */
    @NotNull TaskFactory<?, ?> getFactory(@NotNull String name);

    /**
     * Retrieve a specific factory from this factory aware object.
     *
     * @param factory
     *         The factory class
     * @param <F>
     *         The factory type.
     *
     * @return The {@link TaskFactory} instance.
     *
     * @throws UnknownFactoryException
     *         if the factory class did not match any registered factories.
     */
    <F extends TaskFactory<?, ?>> @NotNull F getFactory(@NotNull Class<F> factory);

}
