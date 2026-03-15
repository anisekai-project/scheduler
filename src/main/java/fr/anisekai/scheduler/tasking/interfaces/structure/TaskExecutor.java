package fr.anisekai.scheduler.tasking.interfaces.structure;

import org.jetbrains.annotations.NotNull;

/**
 * Functional interface for task logic.
 *
 * @param <I>
 *         Input type.
 * @param <O>
 *         Output type.
 */
@FunctionalInterface
public interface TaskExecutor<I, O> {

    /**
     * Executes the business logic of the task.
     *
     * @param arguments
     *         The typed input arguments.
     *
     * @return The result of the execution.
     *
     * @throws Exception
     *         if execution fails.
     */
    @NotNull O execute(@NotNull I arguments) throws Exception;

}