package theodolite.commons.flink.serialization;

import javax.annotation.Nullable;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serde;
import theodolite.commons.flink.util.SerializableSupplier;

/**
 * A {@link KafkaSerializationSchema} and {@link KafkaDeserializationSchema} for an arbitrary
 * key-value-pair in Kafka, mapped to/from a Flink {@link Tuple2}.
 *
 * @param <K> Type of the key.
 * @param <V> Type of the value.
 */
public class FlinkKafkaKeyValueSerde<K, V>
    implements KafkaDeserializationSchema<Tuple2<K, V>>, KafkaSerializationSchema<Tuple2<K, V>> {

  private static final long serialVersionUID = 2469569396501933443L; // NOPMD

  private final SerializableSupplier<Serde<K>> keySerdeSupplier;
  private final SerializableSupplier<Serde<V>> valueSerdeSupplier;
  private final String topic;
  private final TypeInformation<Tuple2<K, V>> typeInfo;

  private transient Serde<K> keySerde;
  private transient Serde<V> valueSerde;

  /**
   * Create a new {@link FlinkKafkaKeyValueSerde}.
   */
  public FlinkKafkaKeyValueSerde(final String topic,
      final SerializableSupplier<Serde<K>> keySerdeSupplier,
      final SerializableSupplier<Serde<V>> valueSerdeSupplier,
      final TypeInformation<Tuple2<K, V>> typeInfo) {
    this.topic = topic;
    this.typeInfo = typeInfo;
    this.keySerdeSupplier = keySerdeSupplier;
    this.valueSerdeSupplier = valueSerdeSupplier;
  }

  @Override
  public boolean isEndOfStream(final Tuple2<K, V> nextElement) {
    return false;
  }

  @Override
  public Tuple2<K, V> deserialize(final ConsumerRecord<byte[], byte[]> record) {
    this.ensureInitialized();
    final K key = this.keySerde.deserializer().deserialize(this.topic, record.key());
    final V value = this.valueSerde.deserializer().deserialize(this.topic, record.value());
    return new Tuple2<>(key, value);
  }

  @Override
  public TypeInformation<Tuple2<K, V>> getProducedType() {
    return this.typeInfo;
  }

  @Override
  public ProducerRecord<byte[], byte[]> serialize(final Tuple2<K, V> element,
      @Nullable final Long timestamp) {
    this.ensureInitialized();
    final byte[] key = this.keySerde.serializer().serialize(this.topic, element.f0);
    final byte[] value = this.valueSerde.serializer().serialize(this.topic, element.f1);
    return new ProducerRecord<>(this.topic, key, value);
  }

  private void ensureInitialized() {
    if (this.keySerde == null || this.valueSerde == null) {
      this.keySerde = this.keySerdeSupplier.get();
      this.valueSerde = this.valueSerdeSupplier.get();
    }
  }

}
