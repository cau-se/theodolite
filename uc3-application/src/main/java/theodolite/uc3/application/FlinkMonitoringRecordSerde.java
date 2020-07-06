package theodolite.uc3.application;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.lang.reflect.InvocationTargetException;
import kieker.common.record.IMonitoringRecord;
import kieker.common.record.factory.IRecordFactory;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.kafka.common.serialization.Serde;
import titan.ccp.common.kieker.kafka.IMonitoringRecordSerde;

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
    implements DeserializationSchema<R>, SerializationSchema<R> {

  private static final long serialVersionUID = 1627501594578930655L; //NOPMD

  private final String topic;
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
  public R deserialize(final byte[] data) {
    ensureInitialized();
    return this.serde.deserializer().deserialize(this.topic, data);
  }

  @Override
  public boolean isEndOfStream(final R nextElement) {
    return false;
  }

  @Override
  public byte[] serialize(final R record) {
    ensureInitialized();
    return this.serde.serializer().serialize(this.topic, record);
  }

  @Override
  public TypeInformation<R> getProducedType() {
    return TypeExtractor.getForClass(recordClass);
  }

  private void ensureInitialized() {
    if (this.serde == null) {
      try {
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
    final byte[] data = this.serialize(record);
    output.writeInt(data.length);
    output.writeBytes(data);
  }

  @Override
  public R read(final Kryo kryo, final Input input, final Class<R> type) {
    final int numBytes = input.readInt();
    return deserialize(input.readBytes(numBytes));
  }
}
