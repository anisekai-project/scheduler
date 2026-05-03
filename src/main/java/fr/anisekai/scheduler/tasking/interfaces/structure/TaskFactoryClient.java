package fr.anisekai.scheduler.tasking.interfaces.structure;

import fr.anisekai.scheduler.tasking.data.TaskMeta;
import org.jetbrains.annotations.NotNull;

/**
 * Specification of a {@link TaskFactory} that can also create its own {@link TaskExecutor} and execute them.
 *
 * @param <I>
 *         The input argument type
 * @param <O>
 *         The output result type
 */
public interface TaskFactoryClient<I, O> extends TaskFactory<I, O> {

    /**
     * Performs the standard execution flow for the provided task metadata. This involves argument deserialization,
     * invocation of the {@link TaskExecutor}, and result serialization.
     *
     * @param task
     *         The task to execute.
     *
     * @return The raw result of the task.
     *
     * @throws Exception
     *         if the execution of the task failed.
     */
    @NotNull String execute(@NotNull TaskMeta task) throws Exception;

    /**
     * Retrieve the executor allowing to run a task.
     *
     * @return A {@link TaskExecutor}.
     */
    @NotNull TaskExecutor<I, O> getExecutor();

}
