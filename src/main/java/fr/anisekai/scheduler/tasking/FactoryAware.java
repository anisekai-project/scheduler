package fr.anisekai.scheduler.tasking;

import fr.anisekai.scheduler.tasking.exceptions.UnknownFactoryException;
import fr.anisekai.scheduler.tasking.interfaces.TaskFactoryAware;
import fr.anisekai.scheduler.tasking.interfaces.structure.TaskFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Complete implementation of a {@link TaskFactoryAware} interface.
 */
public class FactoryAware<T extends TaskFactory<?, ?>> implements TaskFactoryAware {

    private final Set<T> factories;

    /**
     * Create a new {@link FactoryAware} instance.
     *
     * @param factories
     *         A set of {@link TaskFactory} this {@link FactoryAware} will use.
     */
    public FactoryAware(Set<T> factories) {

        this.factories = factories;
    }

    @Override
    public @NotNull T getFactory(@NotNull String name) {

        return this.factories
                .stream()
                .filter(item -> item.getName().equals(name))
                .findAny()
                .orElseThrow(() -> new UnknownFactoryException(name));
    }

    @Override
    public @NotNull <F extends TaskFactory<?, ?>> F getFactory(@NotNull Class<F> factory) {

        return this.factories
                .stream()
                .filter(factory::isInstance)
                .map(factory::cast)
                .findAny()
                .orElseThrow(() -> new UnknownFactoryException(factory));
    }

}
