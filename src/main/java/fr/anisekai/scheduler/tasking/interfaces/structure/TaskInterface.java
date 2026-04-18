package fr.anisekai.scheduler.tasking.interfaces.structure;

import fr.anisekai.scheduler.tasking.enums.TaskStatus;

import java.time.Instant;
import java.util.UUID;

/**
 * Interface representing the contract of a task.
 */
public interface TaskInterface {

    /**
     * The default priority for any task, when not provided.
     */
    byte PRIORITY_DEFAULT = 0;

    /**
     * Represent a low priority for a task that has been added by an automatic task.
     */
    byte PRIORITY_AUTOMATIC_LOW = 1;

    /**
     * Represent a low priority for a task that has been added manually.
     */
    byte PRIORITY_MANUAL_LOW = 2;

    /**
     * Represent a high priority for a task that has been added by an automatic task.
     */
    byte PRIORITY_AUTOMATIC_HIGH = 3;

    /**
     * Represent a high priority for a task that has been added manually.
     */
    byte PRIORITY_MANUAL_HIGH = 4;

    /**
     * Represent a priority for a task that should be executed as soon as possible.
     */
    byte PRIORITY_URGENT = 5;

    /**
     * Retrieve this task UUID.
     *
     * @return A UUID.
     */
    UUID getId();

    /**
     * Retrieve this task factory name.
     *
     * @return A task factory name.
     */
    String getFactoryName();

    /**
     * Define this task factory name.
     *
     * @param factoryName
     *         A task factory name.
     */
    void setFactoryName(String factoryName);

    /**
     * Retrieve this task name.
     *
     * @return A task name.
     */
    String getName();

    /**
     * Define this task name.
     *
     * @param name
     *         A task name.
     */
    void setName(String name);

    /**
     * Retrieve this task status.
     *
     * @return A task status.
     */
    TaskStatus getStatus();

    /**
     * Define this task status.
     *
     * @param status
     *         A task status.
     */
    void setStatus(TaskStatus status);

    /**
     * Retrieve this task priority.
     *
     * @return A task priority.
     */
    byte getPriority();

    /**
     * Define this task priority.
     *
     * @param priority
     *         A task priority.
     */
    void setPriority(byte priority);

    /**
     * Retrieve this task arguments.
     *
     * @return A string representing the serialized arguments.
     */
    String getArguments();

    /**
     * Define this task arguments.
     *
     * @param arguments
     *         A string representing the serialized arguments.
     */
    void setArguments(String arguments);

    /**
     * Retrieve this task failure count.
     *
     * @return A failure count.
     */
    byte getFailureCount();

    /**
     * Define this task failure count.
     *
     * @param failureCount
     *         A failure count.
     */
    void setFailureCount(byte failureCount);

    /**
     * Define this task start time.
     *
     * @param startedAt
     *         A start time.
     */
    void setStartedAt(Instant startedAt);

    /**
     * Define this task completion time.
     *
     * @param completedAt
     *         A completion time.
     */
    void setCompletedAt(Instant completedAt);

    /**
     * Retrieve this task creation time.
     *
     * @return A creation time.
     */
    Instant getCreatedAt();

}
