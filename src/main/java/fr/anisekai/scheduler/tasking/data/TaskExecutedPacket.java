package fr.anisekai.scheduler.tasking.data;

import fr.anisekai.scheduler.tasking.interfaces.structure.TaskInterface;

public record TaskExecutedPacket<T extends TaskInterface, R>(T task, R result) {

}
