package fr.anisekai.scheduler.tasking.enums;

/**
 * Represents the list of a scheduled task during its lifecycle.
 */
public enum TaskStatus {

    /**
     * The task has been scheduled and is waiting to be executed.
     */
    SCHEDULED,

    /**
     * The task is currently being executed.
     */
    EXECUTING,

    /**
     * The task execution has failed.
     */
    FAILED,

    /**
     * The task was executed successfully.
     */
    SUCCEEDED,

    /**
     * The task was canceled before completion.
     */
    CANCELED
}


