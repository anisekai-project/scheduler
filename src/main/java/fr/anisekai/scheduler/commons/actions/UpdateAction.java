package fr.anisekai.scheduler.commons.actions;

import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public record UpdateAction<ID, T>(@NotNull ID targetId, @NotNull Consumer<T> hook) {

}
