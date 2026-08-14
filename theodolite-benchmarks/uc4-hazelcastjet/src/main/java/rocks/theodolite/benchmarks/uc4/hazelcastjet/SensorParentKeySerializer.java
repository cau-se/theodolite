package rocks.theodolite.benchmarks.uc4.hazelcastjet;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;
import java.io.IOException;
import rocks.theodolite.benchmarks.uc4.commons.SensorParentKey;

/**
 * Serializes and deserializes a {@link SensorParentKey} for Hazelcast Jet.
 */
public class SensorParentKeySerializer implements StreamSerializer<SensorParentKey> {

  private static final int TYPE_ID = 2;

  @Override
  public int getTypeId() {
    return TYPE_ID;
  }

  @Override
  public void write(final ObjectDataOutput out, final SensorParentKey key) throws IOException {
    out.writeString(key.getSensor());
    out.writeString(key.getParent());
  }

  @Override
  public SensorParentKey read(final ObjectDataInput in) throws IOException {
    return new SensorParentKey(in.readString(), in.readString());
  }

}