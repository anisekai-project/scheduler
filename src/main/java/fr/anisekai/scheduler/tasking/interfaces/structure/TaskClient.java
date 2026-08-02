package fr.anisekai.scheduler.tasking.interfaces.structure;

import fr.anisekai.scheduler.tasking.interfaces.factories.Factory;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

/**
 * Represent a client that can poll for and execute tasks.
 */
public interface TaskClient {

    /**
     * Retrieve this client UUID.
     *
     * @return A UUID.
     */
    UUID getId();

    /**
     * Retrieve all supported factories by this client.
     *
     * @return A collection of {@link Factory}.
     */
    @NotNull Collection<Factory<?, ?>> getSupportedFactories();

}
