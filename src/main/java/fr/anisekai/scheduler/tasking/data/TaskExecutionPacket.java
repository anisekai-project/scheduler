package fr.anisekai.scheduler.tasking.data;

import fr.anisekai.scheduler.tasking.interfaces.structure.TaskInterface;

public record TaskExecutionPacket<T extends TaskInterface>(T task) {

}
