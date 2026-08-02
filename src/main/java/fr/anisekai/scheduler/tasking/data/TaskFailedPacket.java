package fr.anisekai.scheduler.tasking.data;

import fr.anisekai.scheduler.tasking.interfaces.structure.TaskInterface;

public record TaskFailedPacket<T extends TaskInterface>(T task, Exception exception) {

}
