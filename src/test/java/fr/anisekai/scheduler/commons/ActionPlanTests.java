package fr.anisekai.scheduler.commons;

import fr.anisekai.scheduler.commons.actions.CreateAction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Action plan")
class ActionPlanTests {

    @Test
    @DisplayName("Should snapshot lists passed to the canonical constructor")
    void shouldSnapshotConstructorArguments() {

        List<CreateAction<String>> creates = new ArrayList<>();
        ActionPlan<Long, String, String> plan = new ActionPlan<>(creates, List.of(), List.of());

        creates.add(new CreateAction<>("late"));

        assertEquals(0, plan.creates().size());
        assertThrows(UnsupportedOperationException.class, () -> plan.creates().clear());
    }

    @Test
    @DisplayName("Should not change an existing plan when its builder is reused")
    void shouldSnapshotBuilderState() {

        ActionPlan.Builder<Long, String, String> builder = new ActionPlan.Builder<>();
        ActionPlan<Long, String, String> first = builder.create("first").build();

        builder.create("second");

        assertEquals(1, first.creates().size());
    }

}
