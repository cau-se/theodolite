package rocks.theodolite.benchmarks.uc4.beam.serialization;

import org.apache.kafka.common.serialization.Serde;
import rocks.theodolite.benchmarks.commons.kafka.simpleserdes.BufferSerde;
import rocks.theodolite.benchmarks.commons.kafka.simpleserdes.ReadBuffer;
import rocks.theodolite.benchmarks.commons.kafka.simpleserdes.SimpleSerdes;
import rocks.theodolite.benchmarks.commons.kafka.simpleserdes.WriteBuffer;
import rocks.theodolite.benchmarks.uc4.beam.SensorParentKey;

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
