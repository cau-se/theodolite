package rocks.theodolite.benchmarks.uc4.hazelcastjet.uc4specifics;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;
import java.io.IOException;

/**
 * Serializes and Deserializes a SensorGroupKey.
 */
public class SensorGroupKeySerializer implements StreamSerializer<SensorGroupKey> {

  private static final int TYPE_ID = 2;

  @Override
  public int getTypeId() {
    return TYPE_ID;
  }

  @Override
  public void write(final ObjectDataOutput out, final SensorGroupKey key) throws IOException {
    out.writeString(key.getSensorId());
    out.writeString(key.getGroup());
  }

  @Override
  public SensorGroupKey read(final ObjectDataInput in) throws IOException {
    return new SensorGroupKey(in.readString(), in.readString());
  }

}
