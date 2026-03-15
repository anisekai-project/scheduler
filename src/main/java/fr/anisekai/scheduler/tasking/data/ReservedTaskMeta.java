package fr.anisekai.scheduler.tasking.data;

import fr.anisekai.scheduler.commons.actions.CreateAction;
import fr.anisekai.scheduler.tasking.enums.TaskStatus;
import org.jetbrains.annotations.NotNull;

/**
 * Structure used in {@link CreateAction} when a task needs to be created (queued) in a persistance layer.
 * <p>
 * The status is voluntarily omitted because it would have been a 'static' value set to {@link TaskStatus#SCHEDULED}
 * otherwise. It is expected from the service managing the persistance layer to enforce this status upon task creation.
 *
 * @param factoryName
 *         The factory name that created the task.
 * @param name
 *         The name of the task.
 * @param arguments
 *         The arguments to use when executing the task.
 * @param priority
 *         The priority of the task.
 */
public record ReservedTaskMeta(
        @NotNull String factoryName,
        @NotNull String name,
        @NotNull String arguments,
        @NotNull byte priority
) {

}
