package fr.anisekai.scheduler.tasking;

import fr.anisekai.scheduler.commons.ActionPlan;
import fr.anisekai.scheduler.commons.actions.UpdateAction;
import fr.anisekai.scheduler.commons.interfaces.ObjectSerializer;
import fr.anisekai.scheduler.tasking.data.ReservedTaskMeta;
import fr.anisekai.scheduler.tasking.data.TaskExecutedPacket;
import fr.anisekai.scheduler.tasking.data.TaskFailedPacket;
import fr.anisekai.scheduler.tasking.data.TestTask;
import fr.anisekai.scheduler.tasking.data.io.TestInput;
import fr.anisekai.scheduler.tasking.data.io.TestOutput;
import fr.anisekai.scheduler.tasking.enums.TaskStatus;
import fr.anisekai.scheduler.tasking.exceptions.NonExecutingTaskException;
import fr.anisekai.scheduler.tasking.interfaces.factories.ClientFactory;
import fr.anisekai.scheduler.tasking.interfaces.factories.FactoryRegistry;
import fr.anisekai.scheduler.tasking.interfaces.factories.ServerFactory;
import fr.anisekai.scheduler.tasking.interfaces.structure.TaskClient;
import fr.anisekai.scheduler.tasking.interfaces.structure.TaskInterface;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;
import java.util.*;

import static fr.anisekai.scheduler.ActionPlanAssertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("Server Orchestrator")
public class ServerOrchestratorTests {

    private ServerFactory<TestTask, TestInput, TestOutput> factory;
    private FactoryRegistry<ServerFactory<TestTask, ?, ?>> registry;

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void setUp() {

        this.factory  = spy(new CustomServerFactory() {
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

            @Override
            public @NotNull String getTaskName(@NonNull TestInput arguments) {

                return "super-task";
            }
        });
        this.registry = mock(CustomFactoryRegistry.class);

        Set<ServerFactory<TestTask, TestInput, TestOutput>> factories = Collections.singleton(this.factory);

        doReturn(this.factory).when(this.registry).query(any(Class.class));
        doReturn(this.factory).when(this.registry).query(anyString());
        doReturn(factories).when(this.registry).getFactories();
    }

    private Orchestrator createOrchestrator(TestTask... initialTasks) {

        return new Orchestrator(this.registry, 3) {
            @Override
            public @NotNull List<TestTask> getTasks() {

                return Arrays.asList(initialTasks);
            }
        };
    }

    private TestTask createTask(ServerFactory<TestTask, TestInput, TestOutput> factory, TaskStatus status, TestInput input, byte priority) {

        TestTask task = new TestTask();
        task.setName(factory.getTaskName(input));
        task.setFactoryName(factory.getName());
        task.setStatus(status);
        task.setPriority(priority);
        return task;
    }

    public interface CustomServerFactory extends ServerFactory<TestTask, TestInput, TestOutput> {

    }

    public interface CustomFactoryRegistry extends FactoryRegistry<ServerFactory<TestTask, ?, ?>> {

    }

    // Abstract to avoid unchecked warning
    public abstract static class Orchestrator extends AbstractServerOrchestrator<TestTask> {

        public Orchestrator(FactoryRegistry<ServerFactory<TestTask, ?, ?>> registry, int maxFailures) {

            super(registry, maxFailures);
        }

    }

    @Nested
    @DisplayName("Queue")
    public class OrchestratorQueuingTests {

        @Test
        @DisplayName("Should queue new task (by factory)")
        public void shouldQueueNewTaskByByFactory() {

            Orchestrator    orchestrator = ServerOrchestratorTests.this.createOrchestrator();
            TestInput       arg          = new TestInput("test");
            List<TestInput> args         = Collections.singletonList(arg);

            ActionPlan<UUID, ReservedTaskMeta, TestTask> plan = orchestrator.queue(
                    ServerOrchestratorTests.this.factory,
                    args
            );

            ReservedTaskMeta what = assertSingleCreateAction(plan).what();

            assertEquals(ServerOrchestratorTests.this.factory.getName(), what.factoryName());
            assertEquals(ServerOrchestratorTests.this.factory.getTaskName(arg), what.name());
            assertEquals(TestInput.CODEC.serialize(arg), what.arguments());
            assertEquals(TaskInterface.PRIORITY_DEFAULT, what.priority());
        }

        @Test
        @DisplayName("Should queue new task (by class)")
        public void shouldQueueNewTaskByClass() {

            Orchestrator    orchestrator = ServerOrchestratorTests.this.createOrchestrator();
            TestInput       arg          = new TestInput("test");
            List<TestInput> args         = Collections.singletonList(arg);

            ActionPlan<UUID, ReservedTaskMeta, TestTask> plan = orchestrator.queue(CustomServerFactory.class, args);

            ReservedTaskMeta what = assertSingleCreateAction(plan).what();

            assertEquals(ServerOrchestratorTests.this.factory.getName(), what.factoryName());
            assertEquals(ServerOrchestratorTests.this.factory.getTaskName(arg), what.name());
            assertEquals(TestInput.CODEC.serialize(arg), what.arguments());
            assertEquals(TaskInterface.PRIORITY_DEFAULT, what.priority());
        }

        @Test
        @DisplayName("Should queue new task when same name but different factory")
        public void shouldQueueNewTaskWhenSameNameButDifferentFactory() {

            TestInput       arg  = new TestInput("test");
            List<TestInput> args = Collections.singletonList(arg);

            TestTask task = ServerOrchestratorTests.this.createTask(
                    ServerOrchestratorTests.this.factory,
                    TaskStatus.SCHEDULED,
                    arg,
                    TaskInterface.PRIORITY_DEFAULT
            );
            task.setFactoryName("another-factory");

            Orchestrator orchestrator = ServerOrchestratorTests.this.createOrchestrator(task);

            ActionPlan<UUID, ReservedTaskMeta, TestTask> plan = orchestrator.queue(CustomServerFactory.class, args);

            ReservedTaskMeta what = assertSingleCreateAction(plan).what();

            assertEquals(ServerOrchestratorTests.this.factory.getName(), what.factoryName());
            assertEquals(ServerOrchestratorTests.this.factory.getTaskName(arg), what.name());
            assertEquals(TestInput.CODEC.serialize(arg), what.arguments());
            assertEquals(TaskInterface.PRIORITY_DEFAULT, what.priority());
        }

        @Test
        @DisplayName("Should queue new task when same factory but different name")
        public void shouldQueueNewTaskWhenSameFactoryButDifferentName() {

            TestInput       arg  = new TestInput("test");
            List<TestInput> args = Collections.singletonList(arg);

            TestTask task = ServerOrchestratorTests.this.createTask(
                    ServerOrchestratorTests.this.factory,
                    TaskStatus.SCHEDULED,
                    arg,
                    TaskInterface.PRIORITY_DEFAULT
            );
            task.setName("another-name");

            Orchestrator orchestrator = ServerOrchestratorTests.this.createOrchestrator(task);

            ActionPlan<UUID, ReservedTaskMeta, TestTask> plan = orchestrator.queue(CustomServerFactory.class, args);

            ReservedTaskMeta what = assertSingleCreateAction(plan).what();

            assertEquals(ServerOrchestratorTests.this.factory.getName(), what.factoryName());
            assertEquals(ServerOrchestratorTests.this.factory.getTaskName(arg), what.name());
            assertEquals(TestInput.CODEC.serialize(arg), what.arguments());
            assertEquals(TaskInterface.PRIORITY_DEFAULT, what.priority());
        }

        @Test
        @DisplayName("Should queue new task on duplicated already executed")
        public void shouldQueueNewTaskOnDuplicatedAlreadyExecuted() {

            TestInput       arg  = new TestInput("test");
            List<TestInput> args = Collections.singletonList(arg);

            TestTask task = ServerOrchestratorTests.this.createTask(
                    ServerOrchestratorTests.this.factory,
                    TaskStatus.SUCCEEDED,
                    arg,
                    TaskInterface.PRIORITY_DEFAULT
            );

            Orchestrator orchestrator = ServerOrchestratorTests.this.createOrchestrator(task);

            ActionPlan<UUID, ReservedTaskMeta, TestTask> plan = orchestrator.queue(
                    CustomServerFactory.class,
                    args,
                    TaskInterface.PRIORITY_URGENT
            );
            ReservedTaskMeta what = assertSingleCreateAction(plan).what();

            assertEquals(ServerOrchestratorTests.this.factory.getName(), what.factoryName());
            assertEquals(ServerOrchestratorTests.this.factory.getTaskName(arg), what.name());
            assertEquals(TestInput.CODEC.serialize(arg), what.arguments());
            assertEquals(TaskInterface.PRIORITY_URGENT, what.priority());
        }

        @Test
        @DisplayName("Should queue new task on duplicated when factory allows duplicates")
        public void shouldQueueNewTaskOnDuplicatedWhenFactoryAllowsDuplicates() {

            doReturn(true).when(ServerOrchestratorTests.this.factory).allowDuplicated();
            TestInput       arg  = new TestInput("test");
            List<TestInput> args = Collections.singletonList(arg);

            TestTask task = ServerOrchestratorTests.this.createTask(
                    ServerOrchestratorTests.this.factory,
                    TaskStatus.SCHEDULED,
                    arg,
                    TaskInterface.PRIORITY_DEFAULT
            );

            Orchestrator orchestrator = ServerOrchestratorTests.this.createOrchestrator(task);

            ActionPlan<UUID, ReservedTaskMeta, TestTask> plan = orchestrator.queue(CustomServerFactory.class, args);

            ReservedTaskMeta what = assertSingleCreateAction(plan).what();

            assertEquals(ServerOrchestratorTests.this.factory.getName(), what.factoryName());
            assertEquals(ServerOrchestratorTests.this.factory.getTaskName(arg), what.name());
            assertEquals(TestInput.CODEC.serialize(arg), what.arguments());
            assertEquals(TaskInterface.PRIORITY_DEFAULT, what.priority());
        }

        @Test
        @DisplayName("Should update task priority when lower & duplicated")
        public void shouldUpdateTaskPriorityWhenLowerAndDuplicated() {

            TestInput       arg  = new TestInput("test");
            List<TestInput> args = Collections.singletonList(arg);

            TestTask task = ServerOrchestratorTests.this.createTask(
                    ServerOrchestratorTests.this.factory,
                    TaskStatus.SCHEDULED,
                    arg,
                    TaskInterface.PRIORITY_DEFAULT
            );

            Orchestrator orchestrator = ServerOrchestratorTests.this.createOrchestrator(task);

            ActionPlan<UUID, ReservedTaskMeta, TestTask> plan = orchestrator.queue(
                    CustomServerFactory.class,
                    args,
                    TaskInterface.PRIORITY_URGENT
            );
            UpdateAction<UUID, TestTask> update = assertSingleUpdateAction(plan);

            assertEquals(task.getId(), update.targetId());

            update.hook().accept(task);
            assertEquals(TaskInterface.PRIORITY_URGENT, task.getPriority());
        }

        @Test
        @DisplayName("Should not update task priority when higher & duplicated")
        public void shouldNotUpdateTaskPriorityWhenHigherAndDuplicated() {

            TestInput       arg  = new TestInput("test");
            List<TestInput> args = Collections.singletonList(arg);

            TestTask task = ServerOrchestratorTests.this.createTask(
                    ServerOrchestratorTests.this.factory,
                    TaskStatus.SCHEDULED,
                    arg,
                    TaskInterface.PRIORITY_URGENT
            );

            Orchestrator orchestrator = ServerOrchestratorTests.this.createOrchestrator(task);

            ActionPlan<UUID, ReservedTaskMeta, TestTask> plan = orchestrator.queue(
                    CustomServerFactory.class,
                    args,
                    TaskInterface.PRIORITY_DEFAULT
            );
            assertEmptyPlan(plan);
        }

    }

    @Nested
    @DisplayName("Poll")
    public class OrchestratorPollTests {

        private TaskClient client;

        @BeforeEach
        public void setUp() {

            CustomClientFactory factory = mock(CustomClientFactory.class);
            this.client = mock(TaskClient.class);

            doReturn("test-factory").when(factory).getName();
            doReturn(TestInput.CODEC).when(factory).getArgumentsSerializer();
            doReturn(TestOutput.CODEC).when(factory).getResultSerializer();

            doReturn(UUID.randomUUID()).when(this.client).getId();
            doReturn(Collections.singletonList(factory)).when(this.client).getSupportedFactories();
        }

        @Test
        @DisplayName("Should not poll task when orchestrator empty")
        public void shouldNotPollTaskWhenOrchestratorEmpty() {

            Orchestrator       orchestrator = ServerOrchestratorTests.this.createOrchestrator();
            Optional<TestTask> polled       = orchestrator.poll(this.client);
            assertTrue(polled.isEmpty());
        }

        @Test
        @DisplayName("Should poll one task when orchestrator not empty")
        public void shouldPollOneTaskWhenOrchestratorNotEmpty() {

            TestInput arg = new TestInput("test");
            TestTask task = ServerOrchestratorTests.this.createTask(
                    ServerOrchestratorTests.this.factory,
                    TaskStatus.SCHEDULED,
                    arg,
                    TaskInterface.PRIORITY_URGENT
            );

            Orchestrator       orchestrator = ServerOrchestratorTests.this.createOrchestrator(task);
            Optional<TestTask> polled       = orchestrator.poll(this.client);

            assertTrue(polled.isPresent());
            assertEquals(task, polled.get());

            verify(ServerOrchestratorTests.this.factory).onAssigningTask(argThat(packet -> packet.task() == task));
        }

        @Test
        @DisplayName("Should not poll task when task is already executing")
        public void shouldNotPollTaskWhenTaskIsAlreadyExecuting() {

            TestInput arg = new TestInput("test");
            TestTask task = ServerOrchestratorTests.this.createTask(
                    ServerOrchestratorTests.this.factory,
                    TaskStatus.EXECUTING,
                    arg,
                    TaskInterface.PRIORITY_URGENT
            );

            Orchestrator       orchestrator = ServerOrchestratorTests.this.createOrchestrator(task);
            Optional<TestTask> polled       = orchestrator.poll(this.client);

            assertTrue(polled.isEmpty());
        }


        @Test
        @DisplayName("Should poll the highest priority task")
        public void shouldPollTheHighestPriorityTask() {

            TestInput arg = new TestInput("test");

            TestTask lowPriorityTask = ServerOrchestratorTests.this.createTask(
                    ServerOrchestratorTests.this.factory,
                    TaskStatus.SCHEDULED,
                    arg,
                    TaskInterface.PRIORITY_MANUAL_LOW
            );

            TestTask highPriorityTask = ServerOrchestratorTests.this.createTask(
                    ServerOrchestratorTests.this.factory,
                    TaskStatus.SCHEDULED,
                    arg,
                    TaskInterface.PRIORITY_URGENT
            );

            // Ensure we have a realistic delay between the two tasks
            highPriorityTask.setCreatedAt(lowPriorityTask.getCreatedAt().plus(2, ChronoUnit.MINUTES));

            Orchestrator orchestrator = ServerOrchestratorTests.this.createOrchestrator(
                    lowPriorityTask,
                    highPriorityTask
            );

            Optional<TestTask> polled = orchestrator.poll(this.client);

            assertTrue(polled.isPresent());
            assertEquals(highPriorityTask, polled.get());

            verify(ServerOrchestratorTests.this.factory).onAssigningTask(argThat(packet -> packet.task() == highPriorityTask));
        }

        public interface CustomClientFactory extends ClientFactory<TestInput, TestOutput> {

        }

    }

    @Nested
    @DisplayName("Resolve")
    public class OrchestratorResolveTests {

        @Nested
        @DisplayName("Failure")
        public class ResolveFailureTests {

            @Test
            @DisplayName("Should throw when task is not executing")
            public void shouldThrowWhenTaskIsNotExecuting() {

                TestInput arg = new TestInput("test");
                TestTask task = ServerOrchestratorTests.this.createTask(
                        ServerOrchestratorTests.this.factory,
                        TaskStatus.SCHEDULED,
                        arg,
                        TaskInterface.PRIORITY_DEFAULT
                );

                Orchestrator               orchestrator = ServerOrchestratorTests.this.createOrchestrator(task);
                TaskFailedPacket<TestTask> packet       = new TaskFailedPacket<>(task, new RuntimeException());

                assertThrows(NonExecutingTaskException.class, () -> orchestrator.resolve(packet));
            }

            @Test
            @DisplayName("Should update the task failure count")
            public void shouldUpdateTheTaskFailureCount() {

                TestInput arg = new TestInput("test");
                TestTask task = ServerOrchestratorTests.this.createTask(
                        ServerOrchestratorTests.this.factory,
                        TaskStatus.EXECUTING,
                        arg,
                        TaskInterface.PRIORITY_DEFAULT
                );

                byte expectedFailureCount = (byte) (task.getFailureCount() + 1);

                Orchestrator               orchestrator = ServerOrchestratorTests.this.createOrchestrator(task);
                TaskFailedPacket<TestTask> packet       = new TaskFailedPacket<>(task, new RuntimeException());

                ActionPlan<UUID, ReservedTaskMeta, TestTask> plan   = orchestrator.resolve(packet);
                UpdateAction<UUID, TestTask>                 update = assertSingleUpdateAction(plan);

                assertEquals(task.getId(), update.targetId());
                update.hook().accept(task);

                assertEquals(expectedFailureCount, task.getFailureCount());
                assertNull(task.getStartedAt());
                assertEquals(TaskStatus.SCHEDULED, task.getStatus());

                verify(ServerOrchestratorTests.this.factory)
                        .onFailure(argThat(item ->
                                                   item.task().getId().equals(task.getId()) &&
                                                           item.exception() == packet.exception()
                        ));
            }


            @Test
            @DisplayName("Should update the task status when too much failures")
            public void shouldUpdateTheTaskStatusWhenTooMuchFailures() {

                TestInput arg = new TestInput("test");
                TestTask task = ServerOrchestratorTests.this.createTask(
                        ServerOrchestratorTests.this.factory,
                        TaskStatus.EXECUTING,
                        arg,
                        TaskInterface.PRIORITY_DEFAULT
                );

                task.setFailureCount((byte) 2);
                byte expectedFailureCount = (byte) 3;

                Orchestrator               orchestrator = ServerOrchestratorTests.this.createOrchestrator(task);
                TaskFailedPacket<TestTask> packet       = new TaskFailedPacket<>(task, new RuntimeException());

                ActionPlan<UUID, ReservedTaskMeta, TestTask> plan   = orchestrator.resolve(packet);
                UpdateAction<UUID, TestTask>                 update = assertSingleUpdateAction(plan);

                assertEquals(task.getId(), update.targetId());
                update.hook().accept(task);

                assertEquals(expectedFailureCount, task.getFailureCount());
                assertNull(task.getStartedAt());
                assertEquals(TaskStatus.FAILED, task.getStatus());

                verify(ServerOrchestratorTests.this.factory)
                        .onFailure(argThat(item ->
                                                   item.task().getId().equals(task.getId()) &&
                                                           item.exception() == packet.exception()
                        ));
            }

        }

        @Nested
        @DisplayName("Success")
        public class ResolveSuccessTests {

            @Test
            @DisplayName("Should throw when task is not executing")
            public void shouldThrowWhenTaskIsNotExecuting() {

                TestInput arg = new TestInput("test");
                TestTask task = ServerOrchestratorTests.this.createTask(
                        ServerOrchestratorTests.this.factory,
                        TaskStatus.SCHEDULED,
                        arg,
                        TaskInterface.PRIORITY_DEFAULT
                );

                Orchestrator orchestrator = ServerOrchestratorTests.this.createOrchestrator(task);

                TaskExecutedPacket<TestTask, TestOutput> packet = new TaskExecutedPacket<>(
                        task,
                        new TestOutput("")
                );

                assertThrows(NonExecutingTaskException.class, () -> orchestrator.resolve(packet));
            }

            @Test
            @DisplayName("Should update the task to a success state")
            public void shouldUpdateTheTaskToSuccessState() {

                TestInput arg = new TestInput("test");
                TestTask task = ServerOrchestratorTests.this.createTask(
                        ServerOrchestratorTests.this.factory,
                        TaskStatus.EXECUTING,
                        arg,
                        TaskInterface.PRIORITY_DEFAULT
                );

                Orchestrator orchestrator = ServerOrchestratorTests.this.createOrchestrator(task);

                TaskExecutedPacket<TestTask, TestOutput> packet = new TaskExecutedPacket<>(
                        task,
                        new TestOutput("")
                );

                ActionPlan<UUID, ReservedTaskMeta, TestTask> plan   = orchestrator.resolve(packet);
                UpdateAction<UUID, TestTask>                 update = assertSingleUpdateAction(plan);

                assertEquals(task.getId(), update.targetId());
                update.hook().accept(task);

                assertNotNull(task.getCompletedAt());
                assertEquals(TaskStatus.SUCCEEDED, task.getStatus());

                verify(ServerOrchestratorTests.this.factory)
                        .onSuccess(argThat(item ->
                                                   item.task().getId().equals(task.getId()) &&
                                                           item.result() == packet.result()
                        ));
            }

        }

    }

}
