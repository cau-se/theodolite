package rocks.theodolite.benchmarks.httpbridge;

import com.google.gson.Gson;

/**
 * A {@link Deserializer} based on GSON.
 *
 * @param <T> Type to be serialized from.
 */
public class GsonDeserializer<T> implements Deserializer<T> {

  private final Gson gson;
  private final Class<T> targetClass;

  public GsonDeserializer(final Class<T> targetClass) {
    this(new Gson(), targetClass);
  }

  public GsonDeserializer(final Gson gson, final Class<T> targetClass) {
    this.gson = gson;
    this.targetClass = targetClass;
  }

  @Override
  public T deserialize(final String json) {
    return this.gson.fromJson(json, this.targetClass);
  }

}
