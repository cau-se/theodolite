package theodolite.commons.httpbridge;

@FunctionalInterface
public interface Deserializer<T> {

  T deserialize(String json);

}
