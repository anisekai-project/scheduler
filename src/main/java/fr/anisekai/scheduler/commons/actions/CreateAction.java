package fr.anisekai.scheduler.commons.actions;

import org.jetbrains.annotations.NotNull;

/**
 * Represent a contract requesting a creation with the provided data.
 *
 * @param what
 *         The data to use to create something.
 * @param <T>
 *         The type of the data carrier.
 */
public record CreateAction<T>(@NotNull T what) {

}
