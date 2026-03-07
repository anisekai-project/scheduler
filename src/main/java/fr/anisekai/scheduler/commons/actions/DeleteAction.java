package fr.anisekai.scheduler.commons.actions;

/**
 * Represent a contract requesting the deletion of an element targeted by the provided identifier.
 *
 * @param targetId
 *         The identifier of the element requested to be deleted.
 * @param <ID>
 *         The type of the identifier.
 */
public record DeleteAction<ID>(ID targetId) {

}
