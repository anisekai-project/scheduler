package fr.anisekai.scheduler.tasking;

import fr.anisekai.scheduler.tasking.data.TaskMeta;
import fr.anisekai.scheduler.tasking.interfaces.structure.TaskExecutor;
import fr.anisekai.scheduler.tasking.interfaces.structure.TaskFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Minimal implementation of a task factory, providing a default behavior for the {@link #execute(TaskMeta)} method.
 *
 * @param <I>
 *         The input argument type
 * @param <O>
 *         The output result type
 */
public abstract class AbstractTaskFactory<I, O> implements TaskFactory<I, O> {

    /**
     * Create a new {@link AbstractTaskFactory} instance.
     */
    public AbstractTaskFactory() {

    }

    @Override
    public @NotNull String execute(@NotNull TaskMeta task) throws Exception {

        I                  arguments = this.getArgumentsSerializer().deserialize(task.arguments());
        TaskExecutor<I, O> executor  = this.getExecutor();
        O                  results;

        try {
            results = executor.execute(arguments);
        } catch (Exception e) {
            this.onFailure(task, e.getMessage());
            throw e;
        }

        this.onSuccess(task, results);
        return this.getResultSerializer().serialize(results);
    }

}
