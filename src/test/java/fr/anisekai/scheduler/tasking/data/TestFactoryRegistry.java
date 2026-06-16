package fr.anisekai.scheduler.tasking.data;

import fr.anisekai.scheduler.tasking.exceptions.UnknownFactoryException;
import fr.anisekai.scheduler.tasking.interfaces.FactoryRegistry;
import fr.anisekai.scheduler.tasking.interfaces.structure.TaskFactory;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TestFactoryRegistry<T extends TaskFactory<?, ?>> implements FactoryRegistry<T> {

    private final Map<Class<?>, T> factories = new HashMap<>();

    @Override
    public @NonNull T query(@NotNull String name) {

        return this.getFactories()
                   .stream()
                   .filter(factory -> name.equals(factory.getName()))
                   .findFirst()
                   .orElseThrow(() -> new UnknownFactoryException(name));
    }

    @Override
    public @NonNull <F extends T> F query(@NotNull Class<F> factory) {

        if (!this.factories.containsKey(factory)) {
            throw new UnknownFactoryException(factory);
        }

        return (F) this.factories.get(factory);
    }

    @Override
    public Collection<T> getFactories() {

        return Collections.unmodifiableCollection(this.factories.values());
    }

    @SafeVarargs
    public final void apply(T... factories) {

        this.factories.clear();
        for (T factory : factories) {
            this.factories.put(factory.getClass(), factory);
        }
    }

}
