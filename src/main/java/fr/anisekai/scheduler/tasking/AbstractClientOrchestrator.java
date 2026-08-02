package fr.anisekai.scheduler.tasking;

import fr.anisekai.scheduler.tasking.data.TaskMeta;
import fr.anisekai.scheduler.tasking.interfaces.factories.ClientFactory;
import fr.anisekai.scheduler.tasking.interfaces.factories.FactoryRegistry;
import fr.anisekai.scheduler.tasking.interfaces.orchestrator.ClientOrchestrator;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * Minimal implementation of a task client, providing a sane default behavior for the {@link #tick()} method.
 */
public abstract class AbstractClientOrchestrator implements ClientOrchestrator {

    private final FactoryRegistry<ClientFactory<?, ?>> registry;

    /**
     * Create a new {@link AbstractClientOrchestrator} instance.
     *
     * @param registry
     *         A {@link FactoryRegistry} implementation allowing to query for factories.
     */
    public AbstractClientOrchestrator(@NotNull FactoryRegistry<ClientFactory<?, ?>> registry) {

        this.registry = registry;
    }

    @Override
    public void tick() {

        Optional<TaskMeta> poll = this.poll();

        if (poll.isEmpty()) {
            return;
        }

        TaskMeta task = poll.get();

        ClientFactory<?, ?> factory = this.registry.query(task.factoryName());

        try {
            String results = factory.execute(task);
            this.onSuccess(task, results);
        } catch (Exception e) {
            this.onFailure(task, e);
        }
    }

}
