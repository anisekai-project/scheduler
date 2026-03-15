package fr.anisekai.scheduler.tasking;

import fr.anisekai.scheduler.tasking.data.TaskMeta;
import fr.anisekai.scheduler.tasking.interfaces.TaskClient;
import fr.anisekai.scheduler.tasking.interfaces.structure.TaskFactory;

import java.util.Optional;
import java.util.Set;

public abstract class AbstractTaskClient extends FactoryAware implements TaskClient {

    public AbstractTaskClient(Set<TaskFactory<?, ?>> factories) {

        super(factories);
    }

    @Override
    public void tick() {

        Optional<TaskMeta> poll = this.poll();

        if (poll.isEmpty()) {
            return;
        }

        TaskMeta task = poll.get();

        TaskFactory<?, ?> factory = this.getFactory(task.factoryName());

        try {
            String results = factory.execute(task);
            this.onSuccess(task, results);
        } catch (Exception e) {
            this.onFailure(task, e);
        }
    }

}
