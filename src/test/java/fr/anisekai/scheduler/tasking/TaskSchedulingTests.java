package fr.anisekai.scheduler.tasking;

import fr.anisekai.scheduler.commons.ActionPlan;
import fr.anisekai.scheduler.commons.actions.UpdateAction;
import fr.anisekai.scheduler.commons.interfaces.ObjectSerializer;
import fr.anisekai.scheduler.tasking.data.TestTask;
import fr.anisekai.scheduler.tasking.data.io.TestInput;
import fr.anisekai.scheduler.tasking.data.io.TestOutput;
import fr.anisekai.scheduler.tasking.enums.TaskStatus;
import fr.anisekai.scheduler.tasking.exceptions.TaskSchedulerException;
import fr.anisekai.scheduler.tasking.interfaces.structure.Task;
import fr.anisekai.scheduler.tasking.interfaces.structure.TaskExecutor;
import fr.anisekai.scheduler.tasking.interfaces.structure.TaskFactory;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static fr.anisekai.scheduler.ActionPlanAssertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Task Scheduling")
public class TaskSchedulingTests {

    private static final TestInput  TEST_INPUT_1  = new TestInput("unit-tests-1");
    private static final TestInput  TEST_INPUT_2  = new TestInput("unit-tests-2");
    private static final TestOutput TEST_OUTPUT_1 = new TestOutput("1-stset-tinu");
    private static final TestOutput TEST_OUTPUT_2 = new TestOutput("2-stset-tinu");
    private static final byte       TASK_FAILS_AT = (byte) 3;

    private static final Collection<TestInput> INPUTS_1 = Collections.singletonList(TEST_INPUT_1);
    private static final Collection<TestInput> INPUTS_2 = Collections.singletonList(TEST_INPUT_2);

    private static final String TEST_INPUT_1_STR  = TestInput.CODEC.serialize(TEST_INPUT_1);
    private static final String TEST_INPUT_2_STR  = TestInput.CODEC.serialize(TEST_INPUT_2);
    private static final String TEST_OUTPUT_1_STR = TestOutput.CODEC.serialize(TEST_OUTPUT_1);
    private static final String TEST_OUTPUT_2_STR = TestOutput.CODEC.serialize(TEST_OUTPUT_2);

    @Spy
    private TaskFactory<TestTask, TestInput, TestOutput> factoryOne;

    @Spy
    private TaskFactory<TestTask, TestInput, TestOutput> factoryTwo;

    private List<TestTask> tasks;


    @BeforeEach
    public void setUp() throws Exception {

        this.tasks = new ArrayList<>();

        // Configure factories
        this.configureFactory(this.factoryOne, "one", TEST_OUTPUT_1_STR);
        this.configureFactory(this.factoryTwo, "two", TEST_OUTPUT_2_STR);
    }

    private void configureFactory(TaskFactory<TestTask, TestInput, TestOutput> factory, String suffix, String output) throws Exception {

        lenient().when(factory.getName()).thenReturn("test-factory-" + suffix);
        lenient().when(factory.getArgumentsSerializer()).thenReturn(TestInput.CODEC);
        lenient().when(factory.getResultSerializer()).thenReturn(TestOutput.CODEC);
        lenient().when(factory.getTaskName(any(TestInput.class)))
                 .thenAnswer(invocationOnMock -> {
                     TestInput input = invocationOnMock.getArgument(0);
                     return String.format("%s:%s", factory.getName(), input.example());
                 });
        lenient().when(factory.execute(any())).thenReturn(output);
    }

    public TestTask createTask(TaskFactory<TestTask, TestInput, TestOutput> factory, TaskStatus status, byte priority) {

        TestTask task = new TestTask();
        task.setName(factory.getTaskName(TEST_INPUT_1));
        task.setFactoryName(factory.getName());
        task.setStatus(status);
        task.setPriority(priority);
        this.tasks.add(task);
        return task;
    }

    public TestTask createTask(TaskFactory<TestTask, TestInput, TestOutput> factory, TaskStatus status) {

        return this.createTask(factory, status, Task.PRIORITY_DEFAULT);
    }

    @Nested
    @DisplayName("Orchestrator")
    class TaskOrchestratorTests {

        private AbstractTaskOrchestrator<Integer, TestTask> orchestrator;

        @BeforeEach
        public void setUp() {

            this.orchestrator = spy(new AbstractTaskOrchestrator<Integer, TestTask>(
                    Set.of(factoryOne, factoryTwo),
                    TASK_FAILS_AT
            ) {
                @Override
                public Integer extractIdentifier(TestTask task) {

                    return task.getId();
                }

                @Override
                public List<TestTask> getTasks() {

                    return tasks;
                }
            });
        }

        @Test
        @SuppressWarnings("unchecked")
        @DisplayName("Should find factory by class")
        void shouldFindFactoryInstanceByClass() {

            assertDoesNotThrow(() -> this.orchestrator.getFactory(factoryOne.getClass()));
        }

        @Test
        @DisplayName("Should find factory by name")
        void shouldFindFactoryInstanceByName() {

            assertDoesNotThrow(() -> this.orchestrator.getFactory(factoryOne.getName()));
        }

        @Test
        @DisplayName("Should queue new task")
        void shouldQueueNewTask() {

            ActionPlan<Integer, Task, TestTask> plan = orchestrator.queue(factoryOne, INPUTS_1);

            Task what = assertSingleCreateAction(plan).what();

            assertEquals(factoryOne.getName(), what.getFactoryName());
            assertEquals(factoryOne.getTaskName(TEST_INPUT_1), what.getName());
            assertEquals(TEST_INPUT_1_STR, what.getArguments());
            assertEquals(TaskStatus.SCHEDULED, what.getStatus());
            assertEquals(Task.PRIORITY_DEFAULT, what.getPriority());
        }

        @Test
        @SuppressWarnings("unchecked")
        @DisplayName("Should queue new task with factory by class")
        void shouldQueueNewTaskWithFactoryByClass() {

            ActionPlan<Integer, Task, TestTask> plan = orchestrator.queue(factoryOne.getClass(), INPUTS_1);

            Task what = assertSingleCreateAction(plan).what();

            assertEquals(factoryOne.getName(), what.getFactoryName());
            assertEquals(factoryOne.getTaskName(TEST_INPUT_1), what.getName());
            assertEquals(TEST_INPUT_1_STR, what.getArguments());
            assertEquals(TaskStatus.SCHEDULED, what.getStatus());
            assertEquals(Task.PRIORITY_DEFAULT, what.getPriority());
        }

        @Test
        @DisplayName("Should update priority on duplicated scheduled task")
        void shouldUpdatePriorityOnDuplicatedScheduledTask() {

            TestTask existing = createTask(factoryOne, TaskStatus.SCHEDULED);
            ActionPlan<Integer, Task, TestTask> plan = orchestrator.queue(
                    factoryOne,
                    INPUTS_1,
                    Task.PRIORITY_URGENT
            );

            UpdateAction<Integer, TestTask> update = assertSingleUpdateAction(plan);
            assertEquals(existing.getId(), update.targetId());

            update.hook().accept(existing);
            assertEquals(Task.PRIORITY_URGENT, existing.getPriority());
        }

        @Test
        @DisplayName("Should not update priority on duplicated scheduled task when priority is lower")
        void shouldNotUpdatePriorityOnDuplicatedScheduledTaskWhenPriorityIsLower() {

            createTask(factoryOne, TaskStatus.SCHEDULED, Task.PRIORITY_URGENT);
            ActionPlan<Integer, Task, TestTask> plan = orchestrator.queue(factoryOne, INPUTS_1);
            assertEmptyPlan(plan);
        }

        @Test
        @DisplayName("Should queue on duplicated non-scheduled task")
        void shouldQueueOnDuplicatedNonScheduledTask() {

            createTask(factoryOne, TaskStatus.SUCCEEDED);
            ActionPlan<Integer, Task, TestTask> plan = orchestrator.queue(factoryOne, INPUTS_1);

            Task what = assertSingleCreateAction(plan).what();

            assertEquals(factoryOne.getName(), what.getFactoryName());
            assertEquals(factoryOne.getTaskName(TEST_INPUT_1), what.getName());
            assertEquals(TEST_INPUT_1_STR, what.getArguments());
            assertEquals(TaskStatus.SCHEDULED, what.getStatus());
            assertEquals(Task.PRIORITY_DEFAULT, what.getPriority());
        }

        @Test
        @DisplayName("Should queue on non-duplicated task from same factory")
        void shouldQueueOnNonDuplicatedTaskFromSameFactory() {

            createTask(factoryOne, TaskStatus.SCHEDULED);
            ActionPlan<Integer, Task, TestTask> plan = orchestrator.queue(factoryOne, INPUTS_2);

            Task what = assertSingleCreateAction(plan).what();

            assertEquals(factoryOne.getName(), what.getFactoryName());
            assertEquals(factoryOne.getTaskName(TEST_INPUT_2), what.getName());
            assertEquals(TEST_INPUT_2_STR, what.getArguments());
            assertEquals(TaskStatus.SCHEDULED, what.getStatus());
            assertEquals(Task.PRIORITY_DEFAULT, what.getPriority());
        }

        @Test
        @DisplayName("Should queue on duplicated task when factory allows it")
        void shouldQueueOnDuplicatedTaskWhenFactoryAllowsIt() {

            when(factoryOne.allowDuplicated()).thenReturn(true);

            createTask(factoryOne, TaskStatus.SCHEDULED);
            ActionPlan<Integer, Task, TestTask> plan = orchestrator.queue(factoryOne, INPUTS_1);

            Task what = assertSingleCreateAction(plan).what();

            assertEquals(factoryOne.getName(), what.getFactoryName());
            assertEquals(factoryOne.getTaskName(TEST_INPUT_1), what.getName());
            assertEquals(TEST_INPUT_1_STR, what.getArguments());
            assertEquals(TaskStatus.SCHEDULED, what.getStatus());
            assertEquals(Task.PRIORITY_DEFAULT, what.getPriority());
        }

        @Test
        @DisplayName("Flagging as succeeded should fail when task was not flagged as executing")
        void flaggingAsSucceededShouldFailWhenTaskWasNotFlaggedAsExecuting() {

            TestTask existing = createTask(factoryOne, TaskStatus.SCHEDULED);
            assertThrows(
                    TaskSchedulerException.class,
                    () -> this.orchestrator.resolveSuccess(existing, TEST_OUTPUT_1_STR)
            );
        }

        @Test
        @DisplayName("Should flag task as succeeded when resolveSucces() is called")
        void flaggingAsSucceededShouldSucceedWhenTaskWasFlaggedAsExecuting() {

            TestTask existing = createTask(factoryOne, TaskStatus.EXECUTING);

            ActionPlan<Integer, Task, TestTask> plan = this.orchestrator.resolveSuccess(existing, TEST_OUTPUT_1_STR);

            UpdateAction<Integer, TestTask> update = assertSingleUpdateAction(plan);
            assertEquals(existing.getId(), update.targetId());

            update.hook().accept(existing);
            assertEquals(TaskStatus.SUCCEEDED, existing.getStatus());
        }

        @Test
        @DisplayName("Flagging as scheduled during failure should succeed when task fail count is below 3")
        void flaggingAsScheduledDuringFailureShouldSucceedWhenTaskFailCountIsBelowThree() {

            TestTask                            existing = createTask(factoryOne, TaskStatus.EXECUTING);
            ActionPlan<Integer, Task, TestTask> plan     = this.orchestrator.resolveFailure(existing, "fail reason");

            UpdateAction<Integer, TestTask> update = assertSingleUpdateAction(plan);
            assertEquals(existing.getId(), update.targetId());

            update.hook().accept(existing);
            assertEquals(TaskStatus.SCHEDULED, existing.getStatus());
        }

        @Test
        @DisplayName("Flagging as failed during failure should succeed when task fail count is 3")
        void flaggingAsFailedDuringFailureShouldSucceedWhenTaskFailCountIsThree() {

            TestTask existing = createTask(factoryOne, TaskStatus.EXECUTING);
            existing.setFailureCount((byte) (TASK_FAILS_AT - 1));

            ActionPlan<Integer, Task, TestTask> plan = this.orchestrator.resolveFailure(existing, "fail reason");

            UpdateAction<Integer, TestTask> update = assertSingleUpdateAction(plan);
            assertEquals(existing.getId(), update.targetId());

            update.hook().accept(existing);
            assertEquals(TaskStatus.FAILED, existing.getStatus());
        }

        @Test
        @DisplayName("Flagging as failed should fail when task was not flagged as executing")
        void flaggingAsFailedShouldFailWhenTaskWasNotFlaggedAsExecuting() {

            TestTask existing = createTask(factoryOne, TaskStatus.SCHEDULED);
            assertThrows(
                    TaskSchedulerException.class,
                    () -> this.orchestrator.resolveFailure(existing, "fail reason")
            );
        }

        @Test
        @DisplayName("Polling a task should return one when using a factory with tasks")
        void pollingTaskShouldReturnOneWhenUsingFactoryWithTasks() {

            createTask(factoryOne, TaskStatus.SCHEDULED);
            Optional<TestTask> polled = this.orchestrator.poll(Collections.singletonList(factoryOne));
            assertTrue(polled.isPresent());
        }

        @Test
        @DisplayName("Polling a task should return nothing when using a factory with non-scheduled tasks")
        void pollingTaskShouldReturnNothingWhenUsingFactoryWithNonScheduledTasks() {

            createTask(factoryOne, TaskStatus.EXECUTING);
            createTask(factoryOne, TaskStatus.SUCCEEDED);
            createTask(factoryOne, TaskStatus.FAILED);
            createTask(factoryOne, TaskStatus.CANCELED);

            Optional<TestTask> polled = this.orchestrator.poll(Collections.singletonList(factoryOne));
            assertTrue(polled.isEmpty());
        }

        @Test
        @DisplayName("Polling a task should return nothing when using a factory without tasks")
        void pollingTaskShouldReturnNothingWhenUsingFactoryWithoutTasks() {

            createTask(factoryOne, TaskStatus.SCHEDULED);

            Optional<TestTask> polled = this.orchestrator.poll(Collections.singletonList(factoryTwo));
            assertTrue(polled.isEmpty());
        }

    }

    @Nested
    @DisplayName("Client")
    class TaskClientTests {

        private AbstractTaskClient<TestTask> client;

        @BeforeEach
        public void setUp() {

            this.client = spy(new AbstractTaskClient<TestTask>(Set.of(factoryOne, factoryTwo)) {
                @Override
                public Optional<TestTask> poll() {

                    return Optional.empty();
                }

                @Override
                public void onSuccess(TestTask task, String result) {

                }

                @Override
                public void onFailure(TestTask task, Throwable throwable) {

                }
            });
        }

        @Test
        @DisplayName("Client should successfully execute task when poll returns valid task")
        void clientShouldSuccessfullyExecuteTaskWhenPollReturnsValidTask() {

            TestTask task = createTask(factoryOne, TaskStatus.EXECUTING);

            lenient().when(client.poll()).thenReturn(Optional.of(task));
            client.tick();

            verify(client).onSuccess(eq(task), any(String.class));
        }

        @Test
        @DisplayName("Client should successfully fail task when poll returns invalid task")
        void clientShouldSuccessfullyFailTaskWhenPollReturnsInvalidTask() throws Exception {

            TestTask                      task    = createTask(factoryOne, TaskStatus.EXECUTING);
            UnsupportedOperationException failure = new UnsupportedOperationException("failure");
            when(factoryOne.execute(any())).thenThrow(failure);

            lenient().when(client.poll()).thenReturn(Optional.of(task));
            client.tick();

            verify(client).onFailure(eq(task), eq(failure));
        }

        @Test
        @DisplayName("Client should do nothing when poll returns nothing")
        void clientShouldDoNothingWhenPollReturnsNothing() throws Exception {

            lenient().when(client.poll()).thenReturn(Optional.empty());
            client.tick();

            verify(client, never()).getFactory(any(String.class));
            verify(factoryOne, never()).execute(any());
            verify(client, never()).onFailure(any(), any());
            verify(client, never()).onSuccess(any(), any());
        }

    }

    @Nested
    @DisplayName("Factory")
    class TaskFactoryTests {

        @Mock
        private AbstractTaskFactory<TestTask, TestInput, TestOutput> factory;

        @BeforeEach
        public void setUp() {

            this.factory = spy(new AbstractTaskFactory<TestTask, TestInput, TestOutput>() {
                @Override
                public @NotNull String getName() {

                    return factoryOne.getName();
                }

                @Override
                public @NotNull TaskExecutor<TestInput, TestOutput> getExecutor() {

                    return factoryOne.getExecutor();
                }

                @Override
                public @NotNull String getTaskName(TestInput arguments) {

                    return factoryOne.getTaskName(arguments);
                }

                @Override
                public @NotNull ObjectSerializer<TestInput> getArgumentsSerializer() {

                    return factoryOne.getArgumentsSerializer();
                }

                @Override
                public @NotNull ObjectSerializer<TestOutput> getResultSerializer() {

                    return factoryOne.getResultSerializer();
                }

                @Override
                public void onSuccess(TestTask task, TestOutput result) {

                }

                @Override
                public void onFailure(TestTask task, String error) {

                }
            });
        }

        @Test
        @SuppressWarnings("unchecked")
        @DisplayName("Should call failure handler and throw exception on executor error")
        void shouldCallFailureHandlerAndThrowExceptionOnExecutorError() throws Exception {

            Throwable error = new UnsupportedOperationException("failure");
            TestTask  task  = createTask(this.factory, TaskStatus.EXECUTING);
            task.setArguments(TEST_INPUT_1_STR);

            TaskExecutor<TestInput, TestOutput> executor = mock(TaskExecutor.class);

            when(this.factory.getExecutor()).thenReturn(executor);
            when(executor.execute(any())).thenThrow(error);

            UnsupportedOperationException got = assertThrows(
                    UnsupportedOperationException.class,
                    () -> this.factory.execute(task)
            );

            assertEquals(error, got);
            verify(this.factory).onFailure(any(), eq(error.getMessage()));
        }

        @Test
        @SuppressWarnings("unchecked")
        @DisplayName("Should call failure handler and throw exception on executor error")
        void shouldClassSuccessHandlerOnExecutorSuccess() throws Exception {

            TestTask task = createTask(this.factory, TaskStatus.EXECUTING);
            task.setArguments(TEST_INPUT_1_STR);

            TaskExecutor<TestInput, TestOutput> executor = mock(TaskExecutor.class);

            when(this.factory.getExecutor()).thenReturn(executor);
            when(executor.execute(any())).thenReturn(TEST_OUTPUT_1);

            String res = this.factory.execute(task);

            assertEquals(TEST_OUTPUT_1_STR, res);

            verify(this.factory).onSuccess(any(), eq(TEST_OUTPUT_1));
        }


    }

}
