package fr.anisekai.scheduler.tasking;

import fr.anisekai.scheduler.tasking.data.TaskMeta;
import fr.anisekai.scheduler.tasking.interfaces.TaskClient;
import fr.anisekai.scheduler.tasking.interfaces.structure.TaskFactoryClient;

import java.util.Optional;
import java.util.Set;

/**
 * Minimal implementation of a task client, providing a sane default behavior for the {@link #tick()} method.
 */
public abstract class AbstractTaskClient extends FactoryAware<TaskFactoryClient<?, ?>> implements TaskClient {

    /**
     * Create a new {@link AbstractTaskClient} instance.
     *
     * @param factories
     *         Set of factories that this client will support.
     */
    public AbstractTaskClient(Set<TaskFactoryClient<?, ?>> factories) {

        super(factories);
    }

    @Override
    public void tick() {

        Optional<TaskMeta> poll = this.poll();

        if (poll.isEmpty()) {
            return;
        }

        TaskMeta task = poll.get();

        TaskFactoryClient<?, ?> factory = this.getFactory(task.factoryName());

        try {
            String results = factory.execute(task);
            this.onSuccess(task, results);
        } catch (Exception e) {
            this.onFailure(task, e);
        }
    }

}
