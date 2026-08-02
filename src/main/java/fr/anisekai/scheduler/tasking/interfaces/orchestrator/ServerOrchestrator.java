package fr.anisekai.scheduler.tasking.interfaces.orchestrator;

import fr.anisekai.scheduler.commons.ActionPlan;
import fr.anisekai.scheduler.tasking.data.ReservedTaskMeta;
import fr.anisekai.scheduler.tasking.data.TaskExecutedPacket;
import fr.anisekai.scheduler.tasking.data.TaskFailedPacket;
import fr.anisekai.scheduler.tasking.enums.TaskStatus;
import fr.anisekai.scheduler.tasking.interfaces.factories.ServerFactory;
import fr.anisekai.scheduler.tasking.interfaces.structure.TaskClient;
import fr.anisekai.scheduler.tasking.interfaces.structure.TaskInterface;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Contracts used by a class capable of managing tasks.
 *
 * @param <E>
 *         The type of the task.
 */
public interface ServerOrchestrator<E extends TaskInterface> {

    /**
     * Retrieve all executable tasks available.
     *
     * @return A list of tasks.
     */
    @NotNull List<E> getTasks();

    /**
     * Try to retrieve an executable task that can be handled by the requesting {@link TaskClient}.
     *
     * @param client
     *         The {@link TaskClient} requesting a task.
     *
     * @return A task to execute, if one matched.
     */
    Optional<E> poll(@NotNull TaskClient client);

    /**
     * Queue one or more tasks from the provided factory.
     *
     * @param factoryClass
     *         The {@link ServerFactory} class for which the tasks will be queued.
     * @param arguments
     *         The arguments collection, one for each task.
     * @param <F>
     *         The factory type.
     * @param <I>
     *         The argument type.
     *
     * @return An {@link ActionPlan} containing all actions required to store the results of this method in a
     *         persistance layer.
     */
    default <F extends ServerFactory<E, I, ?>, I> @NotNull ActionPlan<UUID, ReservedTaskMeta, E> queue(@NotNull Class<F> factoryClass, @NotNull Collection<I> arguments) {

        return this.queue(factoryClass, arguments, TaskInterface.PRIORITY_DEFAULT);
    }

    /**
     * Queue one or more tasks from the provided factory.
     *
     * @param factoryClass
     *         The {@link ServerFactory} class for which the tasks will be queued.
     * @param arguments
     *         The arguments collection, one for each task.
     * @param priority
     *         The priority for the queued tasks.
     * @param <F>
     *         The factory type.
     * @param <I>
     *         The argument type.
     *
     * @return An {@link ActionPlan} containing all actions required to store the results of this method in a
     *         persistance layer.
     */
    <F extends ServerFactory<E, I, ?>, I> @NotNull ActionPlan<UUID, ReservedTaskMeta, E> queue(@NotNull Class<F> factoryClass, @NotNull Collection<I> arguments, byte priority);

    /**
     * Queue one or more tasks from the provided factory.
     *
     * @param factory
     *         The {@link ServerFactory} for which the tasks will be queued.
     * @param arguments
     *         The arguments collection, one for each task.
     * @param <F>
     *         The factory type.
     * @param <I>
     *         The argument type.
     *
     * @return An {@link ActionPlan} containing all actions required to store the results of this method in a
     *         persistance layer.
     */
    default <F extends ServerFactory<E, I, ?>, I> @NotNull ActionPlan<UUID, ReservedTaskMeta, E> queue(@NotNull F factory, @NotNull Collection<I> arguments) {

        return this.queue(factory, arguments, TaskInterface.PRIORITY_DEFAULT);
    }

    /**
     * Queue one or more tasks from the provided factory.
     *
     * @param factory
     *         The {@link ServerFactory} for which the tasks will be queued.
     * @param arguments
     *         The arguments collection, one for each task.
     * @param priority
     *         The priority for the queued tasks.
     * @param <F>
     *         The factory type.
     * @param <I>
     *         The argument type.
     *
     * @return An {@link ActionPlan} containing all actions required to store the results of this method in a
     *         persistance layer.
     */
    <F extends ServerFactory<E, I, ?>, I> @NotNull ActionPlan<UUID, ReservedTaskMeta, E> queue(@NotNull F factory, @NotNull Collection<I> arguments, byte priority);


    /**
     * Called whenever the current {@link ServerOrchestrator} implementation considers a task as successfully executed.
     *
     * @param packet
     *         The packet containing all information about a task success.
     * @param <I>
     *         The type of the task's input.
     * @param <R>
     *         The type of the task's output.
     *
     * @return An {@link ActionPlan} containing all actions required to store the results of this method in a
     *         persistance layer.
     */
    <I, R> @NotNull ActionPlan<UUID, ReservedTaskMeta, E> resolve(@NotNull TaskExecutedPacket<E, R> packet);

    /**
     * Called whenever the current {@link ServerOrchestrator} implementation considers a task execution as failed. This
     * will be called at every failure until the task eventually reach {@link TaskStatus#FAILED}.
     *
     * @param packet
     *         The packet containing all information about a task failure.
     * @param <I>
     *         The type of the task's input.
     *
     * @return An {@link ActionPlan} containing all actions required to store the results of this method in a
     *         persistance layer.
     */
    <I> @NotNull ActionPlan<UUID, ReservedTaskMeta, E> resolve(@NotNull TaskFailedPacket<E> packet);

}
