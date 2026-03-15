package fr.anisekai.scheduler.tasking.data;

import fr.anisekai.scheduler.tasking.interfaces.structure.Task;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Minimal structure of a task required for a client to execute that said task.
 *
 * @param identifier
 *         The identifier of the task.
 * @param factoryName
 *         The factory name that created the task.
 * @param arguments
 *         The arguments to use when executing the task.
 */
public record TaskMeta(
        @NotNull UUID identifier,
        @NotNull String factoryName,
        @NotNull String arguments
) {

    /**
     * Create a new instance of {@link TaskMeta} using data contained within the provided task.
     *
     * @param task
     *         The task from which data will be used.
     *
     * @return A {@link TaskMeta} instance.
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull TaskMeta of(@NotNull Task task) {

        return new TaskMeta(task.getId(), task.getFactoryName(), task.getArguments());
    }

}
