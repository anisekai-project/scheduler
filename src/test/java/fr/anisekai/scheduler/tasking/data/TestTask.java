package fr.anisekai.scheduler.tasking.data;

import fr.anisekai.scheduler.tasking.enums.TaskStatus;
import fr.anisekai.scheduler.tasking.interfaces.structure.TaskInterface;

import java.time.Instant;
import java.util.UUID;

public class TestTask implements TaskInterface {

    private final UUID       id;
    private       String     factoryName;
    private       String     name;
    private       TaskStatus status;
    private       byte       priority;
    private       String     arguments;
    private       byte       failureCount;
    private       Instant    createdAt;

    public TestTask() {

        this.id = UUID.randomUUID();
    }

    public UUID getId() {

        return this.id;
    }

    @Override
    public String getFactoryName() {

        return this.factoryName;
    }

    @Override
    public void setFactoryName(String factoryName) {

        this.factoryName = factoryName;
    }

    @Override
    public String getName() {

        return this.name;
    }

    @Override
    public void setName(String name) {

        this.name = name;
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
    public void setPriority(byte priority) {

        this.priority = priority;
    }

    @Override
    public String getArguments() {

        return this.arguments;
    }

    @Override
    public void setArguments(String arguments) {

        this.arguments = arguments;
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
    public void setStartedAt(Instant startedAt) {

    }

    @Override
    public void setCompletedAt(Instant completedAt) {

    }

    @Override
    public Instant getCreatedAt() {

        return this.createdAt;
    }

    public void setCreatedAt(Instant createdAt) {

        this.createdAt = createdAt;
    }

}
