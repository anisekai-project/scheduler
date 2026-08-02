package fr.anisekai.scheduler.tasking;

import fr.anisekai.scheduler.commons.interfaces.ObjectSerializer;
import fr.anisekai.scheduler.tasking.data.TaskMeta;
import fr.anisekai.scheduler.tasking.data.io.TestInput;
import fr.anisekai.scheduler.tasking.data.io.TestOutput;
import fr.anisekai.scheduler.tasking.interfaces.factories.ClientFactory;
import fr.anisekai.scheduler.tasking.interfaces.factories.FactoryRegistry;
import fr.anisekai.scheduler.tasking.interfaces.structure.TaskHandler;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.shadow.de.siegmar.fastcsv.util.Nullable;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("Client Orchestrator")
public class ClientOrchestratorTests {

    private ClientFactory<TestInput, TestOutput> factory;
    private FactoryRegistry<ClientFactory<?, ?>> registry;

    private TaskHandler<TestInput, TestOutput> handler;

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setUp() {

        this.registry = mock(CustomFactoryRegistry.class);
        this.factory  = spy(new ClientFactory<TestInput, TestOutput>() {

            @Override
            public @NotNull TaskHandler<TestInput, TestOutput> getHandler() {

                return ClientOrchestratorTests.this.handler;
            }

            @Override
            public @NotNull String getName() {

                return "test-factory";
            }

            @Override
            public @NotNull ObjectSerializer<TestInput> getArgumentsSerializer() {

                return TestInput.CODEC;
            }

            @Override
            public @NotNull ObjectSerializer<TestOutput> getResultSerializer() {

                return TestOutput.CODEC;
            }
        });

        Set<ClientFactory<TestInput, TestOutput>> factories = Collections.singleton(this.factory);

        doReturn(this.factory).when(this.registry).query(any(Class.class));
        doReturn(this.factory).when(this.registry).query(anyString());
        doReturn(factories).when(this.registry).getFactories();
    }

    private AbstractClientOrchestrator createOrchestrator(@Nullable TaskMeta task) {

        return new AbstractClientOrchestrator(this.registry) {
            @Override
            public Optional<TaskMeta> poll() {

                return Optional.ofNullable(task);
            }
        };
    }

    public void useSucceedingHandler() {

        this.handler = TestInput::toOutput;
    }

    public Throwable useFailingHandler() {

        RuntimeException theVeryMeanException = new RuntimeException("failure");

        this.handler = _ -> {
            throw theVeryMeanException;
        };

        return theVeryMeanException;
    }


    @Test
    @DisplayName("Should do nothing when no task polled")
    public void shouldDoNothingWhenNoTaskPolled() {

        AbstractClientOrchestrator orchestrator = this.createOrchestrator(null);
        orchestrator.tick();
        verify(this.registry, never()).query(anyString());
    }

    @Test
    @DisplayName("Should call onSuccess() when handler succeeds")
    public void shouldCallOnSuccessWhenHandlerSucceed() {

        this.useSucceedingHandler();

        TaskMeta                   task         = new TaskMeta(UUID.randomUUID(), "test-factory", "input:arguments");
        AbstractClientOrchestrator orchestrator = spy(this.createOrchestrator(task));

        orchestrator.tick();

        verify(orchestrator).onSuccess(task, "output:stnemugra");
        verify(orchestrator, never()).onFailure(any(), any());
    }

    @Test
    @DisplayName("Should call onFailure() when handler fails")
    public void shouldCallOnFailureWhenHandlerFails() {

        Throwable ex = this.useFailingHandler();

        TaskMeta                   task         = new TaskMeta(UUID.randomUUID(), "test-factory", "input:arguments");
        AbstractClientOrchestrator orchestrator = spy(this.createOrchestrator(task));

        orchestrator.tick();

        verify(orchestrator).onFailure(task, ex);
        verify(orchestrator, never()).onSuccess(any(), any());
    }

    public interface CustomClientFactory extends ClientFactory<TestInput, TestOutput> {

    }

    public interface CustomFactoryRegistry extends FactoryRegistry<ClientFactory<?, ?>> {

    }

}
