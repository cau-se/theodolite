package theodolite.commons.flink.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import kieker.common.record.IMonitoringRecord;
import kieker.common.record.factory.IRecordFactory;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.flink.streaming.connectors.kafka.KafkaDeserializationSchema;
import org.apache.flink.streaming.connectors.kafka.KafkaSerializationSchema;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import titan.ccp.common.kieker.kafka.IMonitoringRecordSerde;
import titan.ccp.models.records.ActivePowerRecord;
import titan.ccp.models.records.AggregatedActivePowerRecord;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;

/**
 * This class wraps the serializer and deserializer implementations for {@link IMonitoringRecord}
 * from {@link IMonitoringRecordSerde}
 * into the Flink {@link DeserializationSchema} and {@link SerializationSchema}
 * and Kryo {@link Serializer} interfaces.
 * It is used for serialization to and from Kafka as well as internal serialization
 * between Flink instances.
 * This class is also itself serializable by Flink.
 * @param <R> The specific record type that extends {@link IMonitoringRecord}
 * @param <F> The specific record factory type that extends {@link IRecordFactory<R>}
 */
public class FlinkMonitoringRecordSerde<R extends IMonitoringRecord, F extends IRecordFactory<R>>
    extends Serializer<R>
    implements KafkaDeserializationSchema<R>,
               KafkaSerializationSchema<R> {

  private static final long serialVersionUID = -5687951056995646212L; //NOPMD

  private final String topic;
  private transient Serde<String> keySerde;
  private transient Serde<R> serde;

  private final Class<R> recordClass;
  private final Class<F> recordFactoryClass;

  /**
   * Creates a new FlinkMonitoringRecordSerde.
   * @param topic
   *  The Kafka topic to/from which to serialize/deserialize.
   * @param recordClass
   *  The class of the serialized/deserialized record.
   * @param recordFactoryClass
   *  The class of the factory for the serialized/deserialized record.
   */
  public FlinkMonitoringRecordSerde(final String topic,
                                    final Class<R> recordClass,
                                    final Class<F> recordFactoryClass) {
    this.topic = topic;
    this.recordClass = recordClass;
    this.recordFactoryClass = recordFactoryClass;
  }

  @Override
  public boolean isEndOfStream(final R nextElement) {
    return false;
  }

  @Override
  public TypeInformation<R> getProducedType() {
    return TypeExtractor.getForClass(recordClass);
  }

  @Override
  public R deserialize(ConsumerRecord<byte[], byte[]> record) {
    ensureInitialized();
    return this.serde.deserializer().deserialize(this.topic, record.value());
  }

  @Override
  public ProducerRecord<byte[], byte[]> serialize(R element, @Nullable Long timestamp) {
    ensureInitialized();
    String identifier = null;
    if (element instanceof ActivePowerRecord) {
      identifier = ((ActivePowerRecord) element).getIdentifier();
    }
    if (element instanceof AggregatedActivePowerRecord) {
      identifier = ((AggregatedActivePowerRecord) element).getIdentifier();
    }
    final byte[] key = this.keySerde.serializer().serialize(this.topic, identifier);
    final byte[] value = this.serde.serializer().serialize(this.topic, element);
    return new ProducerRecord<>(this.topic, key, value);
  }

  private void ensureInitialized() {
    if (this.keySerde == null || this.serde == null) {
      try {
        this.keySerde = Serdes.String();
        this.serde = IMonitoringRecordSerde.serde(
            recordFactoryClass.getDeclaredConstructor().newInstance());
      } catch (NoSuchMethodException | InstantiationException | IllegalAccessException
          | InvocationTargetException e) {
        e.printStackTrace(); //NOPMD
      }
    }
  }

  @Override
  public void write(final Kryo kryo, final Output output, final R record) {
    ensureInitialized();
    final byte[] data = this.serde.serializer().serialize(this.topic, record);
    output.writeInt(data.length);
    output.writeBytes(data);
  }

  @Override
  public R read(final Kryo kryo, final Input input, final Class<R> type) {
    ensureInitialized();
    final int numBytes = input.readInt();
    return this.serde.deserializer().deserialize(this.topic, input.readBytes(numBytes));
  }
}
