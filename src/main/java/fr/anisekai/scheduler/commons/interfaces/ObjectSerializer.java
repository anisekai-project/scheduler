package fr.anisekai.scheduler.commons.interfaces;

public interface ObjectSerializer<T> {

    String serialize(T container);

    T deserialize(String raw);

}
