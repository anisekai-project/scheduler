package fr.anisekai.scheduler.tasking;

import fr.anisekai.scheduler.tasking.exceptions.UnknownFactoryException;
import fr.anisekai.scheduler.tasking.interfaces.TaskFactoryAware;
import fr.anisekai.scheduler.tasking.interfaces.structure.TaskFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class FactoryAware implements TaskFactoryAware {

    private final Set<TaskFactory<?, ?>> factories;

    public FactoryAware(Set<TaskFactory<?, ?>> factories) {

        this.factories = factories;
    }

    @Override
    public @NotNull TaskFactory<?, ?> getFactory(@NotNull String name) {

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
