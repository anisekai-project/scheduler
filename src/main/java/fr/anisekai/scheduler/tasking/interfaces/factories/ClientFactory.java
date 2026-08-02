package fr.anisekai.scheduler.tasking.interfaces.factories;

import fr.anisekai.scheduler.tasking.data.TaskMeta;
import fr.anisekai.scheduler.tasking.interfaces.structure.TaskHandler;
import org.jetbrains.annotations.NotNull;

/**
 * Specification of a {@link Factory} that can also create its own {@link TaskHandler} and execute them.
 *
 * @param <I>
 *         The input argument type
 * @param <O>
 *         The output result type
 */
public non-sealed interface ClientFactory<I, O> extends Factory<I, O> {

    /**
     * Retrieve the executor allowing to run a task.
     *
     * @return A {@link TaskHandler}.
     */
    @NotNull TaskHandler<I, O> getHandler();

    default O run(TaskMeta meta) throws Exception {

        I input = this.getArgumentsSerializer().deserialize(meta.arguments());
        return this.getHandler().handle(input);
    }

    default String execute(TaskMeta meta) throws Exception {

        O output = this.run(meta);
        return this.getResultSerializer().serialize(output);
    }

}
