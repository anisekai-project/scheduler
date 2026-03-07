package fr.anisekai.scheduler;

import fr.anisekai.scheduler.commons.ActionPlan;
import fr.anisekai.scheduler.commons.actions.CreateAction;
import fr.anisekai.scheduler.commons.actions.DeleteAction;
import fr.anisekai.scheduler.commons.actions.UpdateAction;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class ActionPlanAssertions {

    private ActionPlanAssertions() {}

    public static <A, B, C> CreateAction<B> assertSingleCreateAction(ActionPlan<A, B, C> plan) {

        assertPlanActions(plan, 1, 0, 0);
        return plan.creates().getFirst();
    }

    public static <A, B, C> UpdateAction<A, C> assertSingleUpdateAction(ActionPlan<A, B, C> plan) {

        assertPlanActions(plan, 0, 1, 0);
        return plan.updates().getFirst();
    }

    public static <A, B, C> DeleteAction<A> assertSingleDeleteAction(ActionPlan<A, B, C> plan) {

        assertPlanActions(plan, 0, 0, 1);
        return plan.deletes().getFirst();
    }

    public static <A, B, C> CreateAction<B> assertSingleCreateAction(ActionPlan<A, B, C> plan, Predicate<CreateAction<B>> filter) {

        var actions = plan.creates().stream().filter(filter).toList();
        assertEquals(1, actions.size(), "Expected 1 create action matching the requirement.");
        return actions.getFirst();
    }

    public static <A, B, C> UpdateAction<A, C> assertSingleUpdateAction(ActionPlan<A, B, C> plan, Predicate<UpdateAction<A, C>> filter) {

        var actions = plan.updates().stream().filter(filter).toList();
        assertEquals(1, actions.size(), "Expected 1 update action matching the requirement.");
        return actions.getFirst();
    }

    public static <A, B, C> DeleteAction<A> assertSingleDeleteAction(ActionPlan<A, B, C> plan, Predicate<DeleteAction<A>> filter) {

        var actions = plan.deletes().stream().filter(filter).toList();
        assertEquals(1, actions.size(), "Expected 1 delete action matching the requirement.");
        return actions.getFirst();
    }

    public static void assertEmptyPlan(ActionPlan<?, ?, ?> plan) {

        assertPlanActions(plan, 0, 0, 0);
    }

    public static void assertPlanActions(ActionPlan<?, ?, ?> plan, int creates, int updates, int deletes) {

        int total = creates + updates + deletes;

        assertEquals(
                total,
                plan.size(),
                String.format("Expected %s action(s) to be planned.", total)
        );

        assertEquals(
                creates,
                plan.creates().size(),
                String.format("Expected %s create action(s) to be planned.", creates)
        );

        assertEquals(
                updates,
                plan.updates().size(),
                String.format("Expected %s update action(s) to be planned.", updates)
        );

        assertEquals(
                deletes,
                plan.deletes().size(),
                String.format("Expected %s delete action(s) to be planned.", deletes)
        );
    }

}
