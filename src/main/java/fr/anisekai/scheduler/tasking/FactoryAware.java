package fr.anisekai.scheduler.tasking;

import fr.anisekai.scheduler.tasking.exceptions.UnknownFactoryException;
import fr.anisekai.scheduler.tasking.interfaces.TaskFactoryAware;
import fr.anisekai.scheduler.tasking.interfaces.structure.Task;
import fr.anisekai.scheduler.tasking.interfaces.structure.TaskFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class FactoryAware<E extends Task> implements TaskFactoryAware<E> {

    private final Set<TaskFactory<E, ?, ?>> factories;

    public FactoryAware(Set<TaskFactory<E, ?, ?>> factories) {

        this.factories = factories;
    }

    @Override
    public @NotNull TaskFactory<E, ?, ?> getFactory(@NotNull String name) {

        return this.factories
                .stream()
                .filter(item -> item.getName().equals(name))
                .findAny()
                .orElseThrow(() -> new UnknownFactoryException(name));
    }

    @Override
    public @NotNull <F extends TaskFactory<E, ?, ?>> F getFactory(@NotNull Class<F> factory) {

        return this.factories
                .stream()
                .filter(factory::isInstance)
                .map(factory::cast)
                .findAny()
                .orElseThrow(() -> new UnknownFactoryException(factory));
    }

}
