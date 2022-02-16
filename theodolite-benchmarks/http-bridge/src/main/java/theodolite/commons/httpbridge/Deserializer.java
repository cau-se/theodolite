package theodolite.commons.httpbridge;

/**
 * A class for converting objects to strings.
 *
 * @param <T> Type to be deserialized from.
 */
@FunctionalInterface
public interface Deserializer<T> {

  T deserialize(String json);

}
