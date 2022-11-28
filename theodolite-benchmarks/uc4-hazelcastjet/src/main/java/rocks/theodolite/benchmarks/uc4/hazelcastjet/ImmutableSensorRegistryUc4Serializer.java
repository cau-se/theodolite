package rocks.theodolite.benchmarks.uc4.hazelcastjet;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;
import java.io.IOException;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.ImmutableSensorRegistry;

/**
 * {@link StreamSerializer} for Hazelcast Jet to serialize and deserialize an
 * {@link ImmutableSensorRegistry}.
 */
public class ImmutableSensorRegistryUc4Serializer
    implements StreamSerializer<ImmutableSensorRegistry> {

  private static final int TYPE_ID = 3;

  @Override
  public int getTypeId() {
    return TYPE_ID;
  }

  @Override
  public void write(final ObjectDataOutput out, final ImmutableSensorRegistry object)
      throws IOException {
    final String sensorRegistryJson = object.toJson();
    out.writeString(sensorRegistryJson);
  }

  @Override
  public ImmutableSensorRegistry read(final ObjectDataInput in) throws IOException {
    final String sensorRegistryJson = in.readString();
    return (ImmutableSensorRegistry) ImmutableSensorRegistry.fromJson(sensorRegistryJson);
  }

}
