package theodolite.uc1.application;

import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.typeutils.TypeExtractor;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;
import titan.ccp.common.kieker.kafka.IMonitoringRecordSerde;
import titan.ccp.models.records.ActivePowerRecord;
import titan.ccp.models.records.ActivePowerRecordFactory;

/**
 * This class wraps the serializer and deserializer implementations for {@link ActivePowerRecord}
 * from {@link IMonitoringRecordSerde}
 * into the Flink {@link DeserializationSchema} and {@link SerializationSchema} interfaces.
 * This class is serializable by Flink.
 */
public class ActivePowerRecordDeSerializer implements DeserializationSchema<ActivePowerRecord>,
                                                      SerializationSchema<ActivePowerRecord> {

  private final String topic;

  private transient Deserializer<ActivePowerRecord> deserializer;
  private transient Serializer<ActivePowerRecord> serializer;

  public ActivePowerRecordDeSerializer(final String topic) {
    this.topic = topic;
  }

  @Override
  public ActivePowerRecord deserialize(final byte[] data) {
    if (this.deserializer == null) {
      this.deserializer = IMonitoringRecordSerde.deserializer(new ActivePowerRecordFactory());
    }
    return this.deserializer.deserialize(this.topic, data);
  }

  @Override
  public boolean isEndOfStream(final ActivePowerRecord nextElement) {
    return false;
  }

  @Override
  public byte[] serialize(final ActivePowerRecord record) {
    if (this.serializer == null) {
      this.serializer = IMonitoringRecordSerde.serializer();
    }
    return serializer.serialize(this.topic, record);
  }

  @Override
  public TypeInformation<ActivePowerRecord> getProducedType() {
    return TypeExtractor.getForClass(ActivePowerRecord.class);
  }
}
