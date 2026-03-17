package fr.anisekai.scheduler.commons.interfaces;

/**
 * Contact used by a class allowing to serialize and deserialize a specific object type.
 *
 * @param <T>
 *         Type of the object to serialize / deserialize.
 */
public interface ObjectSerializer<T> {

    /**
     * Serialize the provided object to string.
     *
     * @param container
     *         The object to serialize
     *
     * @return The serialized object.
     */
    String serialize(T container);

    /**
     * Deserialize the provided string into the type handled by this {@link ObjectSerializer}
     *
     * @param raw
     *         The string to deserialize.
     *
     * @return The deserialized object.
     */
    T deserialize(String raw);

}
