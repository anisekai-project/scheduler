package fr.anisekai.scheduler.tasking.interfaces.structure;

import fr.anisekai.scheduler.tasking.enums.TaskStatus;

import java.time.Instant;

public interface Task {

    byte PRIORITY_DEFAULT        = 0;
    byte PRIORITY_AUTOMATIC_LOW  = 1;
    byte PRIORITY_MANUAL_LOW     = 2;
    byte PRIORITY_AUTOMATIC_HIGH = 3;
    byte PRIORITY_MANUAL_HIGH    = 4;
    byte PRIORITY_URGENT         = 5;

    String getFactoryName();

    void setFactoryName(String factoryName);

    String getName();

    void setName(String name);

    TaskStatus getStatus();

    void setStatus(TaskStatus status);

    byte getPriority();

    void setPriority(byte priority);

    String getArguments();

    void setArguments(String arguments);

    byte getFailureCount();

    void setFailureCount(byte failureCount);

    Instant getStartedAt();

    void setStartedAt(Instant startedAt);

    Instant getCompletedAt();

    void setCompletedAt(Instant completedAt);

    Instant getCreatedAt();

    void setCreatedAt(Instant createdAt);

}
