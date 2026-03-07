package fr.anisekai.scheduler.commons.actions;

import java.util.function.Consumer;

public record UpdateAction<ID, T>(ID targetId, Consumer<T> hook) {

}
