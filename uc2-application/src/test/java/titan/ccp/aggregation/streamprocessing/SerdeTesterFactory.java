package titan.ccp.aggregation.streamprocessing;

import org.apache.kafka.common.serialization.Serde;

public class SerdeTesterFactory<T> {

  private static final String DEFAULT_TOPIC = "";

  private final Serde<T> serde;

  public SerdeTesterFactory(final Serde<T> serde) {
    this.serde = serde;
  }

  public SerdeTester<T> create(final T object) {
    return this.create(object, DEFAULT_TOPIC);
  }

  public SerdeTester<T> create(final T object, final String topic) {
    final byte[] serializedBytes = this.serde.serializer().serialize(topic, object);
    final T serdedObject = this.serde.deserializer().deserialize(topic, serializedBytes);
    return new SerdeTester<>(object, serdedObject);
  }

}
