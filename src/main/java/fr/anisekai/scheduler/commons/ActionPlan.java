package fr.anisekai.scheduler.commons;

import fr.anisekai.scheduler.commons.actions.CreateAction;
import fr.anisekai.scheduler.commons.actions.DeleteAction;
import fr.anisekai.scheduler.commons.actions.UpdateAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public record ActionPlan<ID, E, T>(
        List<CreateAction<E>> creates,
        List<UpdateAction<ID, T>> updates,
        List<DeleteAction<ID>> deletes
) {

    public static class Builder<ID, E, T> {

        private final List<CreateAction<E>>     createActions;
        private final List<UpdateAction<ID, T>> updateActions;
        private final List<DeleteAction<ID>>    deleteActions;

        public Builder() {

            this.createActions = new ArrayList<>();
            this.updateActions = new ArrayList<>();
            this.deleteActions = new ArrayList<>();
        }

        public Builder<ID, E, T> create(E what) {

            this.createActions.add(new CreateAction<>(what));
            return this;
        }

        public Builder<ID, E, T> update(ID identifier, Consumer<T> updateHook) {

            this.updateActions.add(new UpdateAction<>(identifier, updateHook));
            return this;
        }

        public Builder<ID, E, T> delete(ID identifier) {

            this.deleteActions.add(new DeleteAction<>(identifier));
            return this;
        }

        public ActionPlan<ID, E, T> build() {

            return new ActionPlan<>(
                    Collections.unmodifiableList(this.createActions),
                    Collections.unmodifiableList(this.updateActions),
                    Collections.unmodifiableList(this.deleteActions)
            );
        }

    }

    public int size() {

        return this.creates().size() + this.updates().size() + this.deletes().size();
    }

    public boolean isEmpty() {

        return this.creates().isEmpty() && this.updates().isEmpty() && this.deletes().isEmpty();
    }

}
