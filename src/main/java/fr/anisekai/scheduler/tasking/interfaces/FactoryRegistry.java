package fr.anisekai.scheduler.tasking.interfaces;

import fr.anisekai.scheduler.tasking.exceptions.UnknownFactoryException;
import fr.anisekai.scheduler.tasking.interfaces.structure.TaskFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Contract used by a class capable of referencing a {@link TaskFactory} by its name or class.
 */
public interface FactoryRegistry<T extends TaskFactory<?, ?>> {

    /**
     * Retrieve a specific factory from this factory registry object.
     *
     * @param name
     *         The factory name
     *
     * @return The {@link TaskFactory} instance.
     *
     * @throws UnknownFactoryException
     *         if the factory class did not match any registered factories.
     */
    @NotNull T query(@NotNull String name);

    /**
     * Retrieve a specific factory from this factory registry object.
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
    <F extends T> @NotNull F query(@NotNull Class<F> factory);

    /**
     * Retrieve all factories registered in this factory registry object. It is recommended to return an unmodifiable
     * collection to avoid any issues.
     *
     * @return A {@link Collection} of {@link TaskFactory}.
     */
    Collection<T> getFactories();

}
