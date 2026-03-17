package fr.anisekai.scheduler.commons.actions;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

/**
 * Represent a contract requesting the update of an element targeted by the provided identifier.
 *
 * @param targetId
 *         The identifier of the element requested to be deleted.
 * @param hook
 *         The {@link Consumer} to use to patch the persisted object.
 * @param <ID>
 *         The type of the identifier.
 * @param <T>
 *         The type of the object in the persistance layer.
 */
public record UpdateAction<ID, T>(@NotNull ID targetId, @NotNull Consumer<T> hook) {

}
