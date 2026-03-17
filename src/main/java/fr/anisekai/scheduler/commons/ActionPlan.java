package fr.anisekai.scheduler.commons;

import fr.anisekai.scheduler.commons.actions.CreateAction;
import fr.anisekai.scheduler.commons.actions.DeleteAction;
import fr.anisekai.scheduler.commons.actions.UpdateAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * Record containing 3 different collection of actions, each representing a specific write operation to a persistance
 * layer.
 *
 * @param creates
 *         List of {@link CreateAction}, allowing to create objects in the persistance layer.
 * @param updates
 *         List of {@link UpdateAction}, allowing to update objects in the persistance layer.
 * @param deletes
 *         List of {@link DeleteAction}, allowing to delete objects from the persistance layer.
 * @param <ID>
 *         Type of the identifier used to target an already existing object to update in or delete from the persistance
 *         layer.
 * @param <E>
 *         Type of the temporary object holding data to create an object in the persistance layer.
 * @param <T>
 *         Type of the actual object in the persistance layer to update.
 */
public record ActionPlan<ID, E, T>(
        List<CreateAction<E>> creates,
        List<UpdateAction<ID, T>> updates,
        List<DeleteAction<ID>> deletes
) {

    /**
     * Builder class allowing to easily construct an {@link ActionPlan}.
     *
     * @param <ID>
     *         Type of the identifier used to target an already existing object to update in or delete from the
     *         persistance layer.
     * @param <E>
     *         Type of the temporary object holding data to create an object in the persistance layer.
     * @param <T>
     *         Type of the actual object in the persistance layer to update.
     */
    public static class Builder<ID, E, T> {

        private final List<CreateAction<E>>     createActions;
        private final List<UpdateAction<ID, T>> updateActions;
        private final List<DeleteAction<ID>>    deleteActions;

        /**
         * Create a new {@link Builder} instance.
         */
        public Builder() {

            this.createActions = new ArrayList<>();
            this.updateActions = new ArrayList<>();
            this.deleteActions = new ArrayList<>();
        }

        /**
         * Add a create action to this builder, providing the minimal object require to build the complete object within
         * the persistance layer.
         *
         * @param what
         *         The object containing the data required to create the final object in the persistance layer.
         *
         * @return The {@link Builder}, for chaining.
         */
        public Builder<ID, E, T> create(E what) {

            this.createActions.add(new CreateAction<>(what));
            return this;
        }

        /**
         * Add an update action to this builder, providing a {@link Consumer} that should be applied to the object
         * targeted by the {@code identifier} provided.
         *
         * @param identifier
         *         The identifier allowing to target a specific object within a persistance layer.
         * @param updateHook
         *         A {@link Consumer} to use to update the persisted object.
         *
         * @return The {@link Builder}, for chaining.
         */
        public Builder<ID, E, T> update(ID identifier, Consumer<T> updateHook) {

            this.updateActions.add(new UpdateAction<>(identifier, updateHook));
            return this;
        }

        /**
         * Add a delete action to this builder, providing the {@code identifier} allowing to target a persisted object.
         *
         * @param identifier
         *         The identifier allowing to target a specific object within a persistance layer.
         *
         * @return The {@link Builder}, for chaining.
         */
        public Builder<ID, E, T> delete(ID identifier) {

            this.deleteActions.add(new DeleteAction<>(identifier));
            return this;
        }

        /**
         * Build the {@link ActionPlan}.
         *
         * @return An {@link ActionPlan} with all registered action within this builder.
         */
        public ActionPlan<ID, E, T> build() {

            return new ActionPlan<>(
                    Collections.unmodifiableList(this.createActions),
                    Collections.unmodifiableList(this.updateActions),
                    Collections.unmodifiableList(this.deleteActions)
            );
        }

    }

    /**
     * Retrieve the total amount of actions contained within this {@link ActionPlan}.
     *
     * @return The amount of actions.
     */
    public int size() {

        return this.creates().size() + this.updates().size() + this.deletes().size();
    }

    /**
     * Check if this {@link ActionPlan} contains any action.
     *
     * @return {@code true} if this {@link ActionPlan} contains no action at all, {@code false} otherwise.
     */
    public boolean isEmpty() {

        return this.creates().isEmpty() && this.updates().isEmpty() && this.deletes().isEmpty();
    }

}
