package fr.anisekai.scheduler.tasking.data;

import fr.anisekai.scheduler.tasking.enums.TaskStatus;
import fr.anisekai.scheduler.tasking.interfaces.structure.Task;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

public class TestTask implements Task {

    private static final AtomicInteger ID_COUNTER = new AtomicInteger(0);

    private final Integer    id;
    private       String     factoryName;
    private       String     name;
    private       TaskStatus status;
    private       byte       priority;
    private       String     arguments;
    private       byte       failureCount;
    private       Instant    startedAt;
    private       Instant    completedAt;
    private       Instant    createdAt;

    public TestTask() {

        this.id = ID_COUNTER.incrementAndGet();
    }

    public Integer getId() {

        return id;
    }

    @Override
    public String getFactoryName() {

        return factoryName;
    }

    @Override
    public void setFactoryName(String factoryName) {

        this.factoryName = factoryName;
    }

    @Override
    public String getName() {

        return name;
    }

    @Override
    public void setName(String name) {

        this.name = name;
    }

    @Override
    public TaskStatus getStatus() {

        return status;
    }

    @Override
    public void setStatus(TaskStatus status) {

        this.status = status;
    }

    @Override
    public byte getPriority() {

        return priority;
    }

    @Override
    public void setPriority(byte priority) {

        this.priority = priority;
    }

    @Override
    public String getArguments() {

        return arguments;
    }

    @Override
    public void setArguments(String arguments) {

        this.arguments = arguments;
    }

    @Override
    public byte getFailureCount() {

        return failureCount;
    }

    @Override
    public void setFailureCount(byte failureCount) {

        this.failureCount = failureCount;
    }

    @Override
    public Instant getStartedAt() {

        return startedAt;
    }

    @Override
    public void setStartedAt(Instant startedAt) {

        this.startedAt = startedAt;
    }

    @Override
    public Instant getCompletedAt() {

        return completedAt;
    }

    @Override
    public void setCompletedAt(Instant completedAt) {

        this.completedAt = completedAt;
    }

    @Override
    public Instant getCreatedAt() {

        return createdAt;
    }

    @Override
    public void setCreatedAt(Instant createdAt) {

        this.createdAt = createdAt;
    }

}
