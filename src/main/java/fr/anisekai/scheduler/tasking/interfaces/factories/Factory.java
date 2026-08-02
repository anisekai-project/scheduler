package fr.anisekai.scheduler.tasking.interfaces.factories;

import fr.anisekai.scheduler.commons.interfaces.ObjectSerializer;
import org.jetbrains.annotations.NotNull;

/**
 * Defines how to create, execute, and handle results for a specific task type.
 *
 * @param <I>
 *         The input argument type
 * @param <O>
 *         The output result type
 */
public sealed interface Factory<I, O> permits ClientFactory, ServerFactory {

    /**
     * Retrieve this factory name that will be tied to any task created for this factory.
     * <p>
     * <b>Note:</b> There is no defensive checks against duplicated factory name. It is the developer responsibility to
     * ensure uniqueness.
     *
     * @return The factory name.
     */
    @NotNull String getName();

    /**
     * Retrieve the {@link ObjectSerializer} instance allowing to manage the input arguments of each task of this
     * factory.
     *
     * @return An {@link ObjectSerializer} instance.
     */
    @NotNull ObjectSerializer<I> getArgumentsSerializer();

    /**
     * Retrieve the {@link ObjectSerializer} instance allowing to manage the output result of each task of this
     * factory.
     *
     * @return An {@link ObjectSerializer} instance.
     */
    @NotNull ObjectSerializer<O> getResultSerializer();

}