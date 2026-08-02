package fr.anisekai.scheduler.tasking.interfaces.factories;

import fr.anisekai.scheduler.tasking.exceptions.UnknownFactoryException;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Contract used by a class capable of referencing a {@link Factory} by its name or class.
 *
 * @param <T>
 *         Common interface shared between all {@link Factory}.
 */
public interface FactoryRegistry<T extends Factory<?, ?>> {

    /**
     * Retrieve a specific factory from this factory registry object.
     *
     * @param name
     *         The factory name
     *
     * @return The {@link Factory} instance.
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
     * @return The {@link Factory} instance.
     *
     * @throws UnknownFactoryException
     *         if the factory class did not match any registered factories.
     */
    <F extends T> @NotNull F query(@NotNull Class<F> factory);

    /**
     * Retrieve all factories registered in this factory registry object. It is recommended to return an unmodifiable
     * collection to avoid any issues.
     *
     * @return A {@link Collection} of {@link Factory}.
     */
    Collection<T> getFactories();

}
