package titan.ccp.aggregation.streamprocessing;

import org.apache.kafka.common.serialization.Serde;
import titan.ccp.common.kafka.simpleserdes.BufferSerde;
import titan.ccp.common.kafka.simpleserdes.ReadBuffer;
import titan.ccp.common.kafka.simpleserdes.SimpleSerdes;
import titan.ccp.common.kafka.simpleserdes.WriteBuffer;

/**
 * {@link Serde} factory for {@link SensorParentKey}.
 */
public final class SensorParentKeySerde implements BufferSerde<SensorParentKey> {

  private SensorParentKeySerde() {}

  @Override
  public void serialize(final WriteBuffer buffer, final SensorParentKey key) {
    buffer.putString(key.getSensor());
    buffer.putString(key.getParent());
  }

  @Override
  public SensorParentKey deserialize(final ReadBuffer buffer) {
    final String sensor = buffer.getString();
    final String parent = buffer.getString();
    return new SensorParentKey(sensor, parent);
  }

  public static Serde<SensorParentKey> serde() {
    return SimpleSerdes.create(new SensorParentKeySerde());
  }

}
