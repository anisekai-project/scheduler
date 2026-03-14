package fr.anisekai.scheduler.tasking;

import fr.anisekai.scheduler.tasking.interfaces.TaskClient;
import fr.anisekai.scheduler.tasking.interfaces.structure.Task;
import fr.anisekai.scheduler.tasking.interfaces.structure.TaskFactory;

import java.util.Optional;
import java.util.Set;

public abstract class AbstractTaskClient<E extends Task> extends FactoryAware<E> implements TaskClient<E> {

    public AbstractTaskClient(Set<TaskFactory<E, ?, ?>> factories) {

        super(factories);
    }

    @Override
    public void tick() {

        Optional<E> poll = this.poll();

        if (poll.isEmpty()) {
            return;
        }

        E task = poll.get();

        TaskFactory<E, ?, ?> factory = this.getFactory(task.getFactoryName());

        try {
            String results = factory.execute(task);
            this.onSuccess(task, results);
        } catch (Exception e) {
            this.onFailure(task, e);
        }
    }

}
