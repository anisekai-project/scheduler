package fr.anisekai.scheduler.tasking;

import fr.anisekai.scheduler.tasking.interfaces.structure.Task;
import fr.anisekai.scheduler.tasking.interfaces.structure.TaskExecutor;
import fr.anisekai.scheduler.tasking.interfaces.structure.TaskFactory;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractTaskFactory<T extends Task, I, O> implements TaskFactory<T, I, O> {

    @Override
    public @NotNull String execute(T task) throws Exception {

        I                  arguments = this.getArgumentsSerializer().deserialize(task.getArguments());
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
