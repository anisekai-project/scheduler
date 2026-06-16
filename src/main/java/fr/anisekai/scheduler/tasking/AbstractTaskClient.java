package fr.anisekai.scheduler.tasking;

import fr.anisekai.scheduler.tasking.data.TaskMeta;
import fr.anisekai.scheduler.tasking.interfaces.FactoryRegistry;
import fr.anisekai.scheduler.tasking.interfaces.TaskClient;
import fr.anisekai.scheduler.tasking.interfaces.structure.TaskFactoryClient;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Minimal implementation of a task client, providing a sane default behavior for the {@link #tick()} method.
 */
public abstract class AbstractTaskClient implements TaskClient {

    private final FactoryRegistry<TaskFactoryClient<?, ?>> registry;

    /**
     * Create a new {@link AbstractTaskClient} instance.
     *
     * @param registry
     *         A {@link FactoryRegistry} implementation allowing to query for factories.
     */
    public AbstractTaskClient(@NotNull FactoryRegistry<TaskFactoryClient<?, ?>> registry) {

        this.registry = registry;
    }

    @Override
    public void tick() {

        Optional<TaskMeta> poll = this.poll();

        if (poll.isEmpty()) {
            return;
        }

        TaskMeta task = poll.get();

        TaskFactoryClient<?, ?> factory = this.registry.query(task.factoryName());

        try {
            String results = factory.execute(task);
            this.onSuccess(task, results);
        } catch (Exception e) {
            this.onFailure(task, e);
        }
    }

}
