package fr.anisekai.scheduler.tasking.data;

import fr.anisekai.scheduler.tasking.enums.TaskStatus;
import fr.anisekai.scheduler.tasking.interfaces.structure.Task;

import java.time.Instant;

public class BookedTask implements Task {

    private final String     name;
    private final String     factoryName;
    private       TaskStatus status;
    private final byte       priority;
    private final String     arguments;
    private       byte       failureCount;
    private       Instant    startedAt;
    private       Instant    completedAt;
    private       Instant    createdAt;

    public BookedTask(String name, String factoryName, byte priority, String arguments) {

        this.name         = name;
        this.factoryName  = factoryName;
        this.status       = TaskStatus.SCHEDULED;
        this.priority     = priority;
        this.arguments    = arguments;
        this.failureCount = 0;
        this.startedAt    = null;
        this.completedAt  = null;
        this.createdAt    = Instant.now();
    }

    @Override
    public String getName() {

        return this.name;
    }

    @Override
    public String getFactoryName() {

        return this.factoryName;
    }

    @Override
    public TaskStatus getStatus() {

        return this.status;
    }

    @Override
    public void setStatus(TaskStatus status) {

        this.status = status;
    }

    @Override
    public byte getPriority() {

        return this.priority;
    }

    @Override
    public String getArguments() {

        return this.arguments;
    }

    @Override
    public byte getFailureCount() {

        return this.failureCount;
    }

    @Override
    public void setFailureCount(byte failureCount) {

        this.failureCount = failureCount;
    }

    @Override
    public Instant getStartedAt() {

        return this.startedAt;
    }

    @Override
    public void setStartedAt(Instant startedAt) {

        this.startedAt = startedAt;
    }

    @Override
    public Instant getCompletedAt() {

        return this.completedAt;
    }

    @Override
    public void setCompletedAt(Instant completedAt) {

        this.completedAt = completedAt;
    }

    @Override
    public Instant getCreatedAt() {

        return this.createdAt;
    }

    @Override
    public void setCreatedAt(Instant createdAt) {

        this.createdAt = createdAt;
    }

    @Override
    public void setFactoryName(String factoryName) {

    }

    @Override
    public void setName(String name) {

    }

    @Override
    public void setPriority(byte priority) {

    }

    @Override
    public void setArguments(String arguments) {

    }

}
