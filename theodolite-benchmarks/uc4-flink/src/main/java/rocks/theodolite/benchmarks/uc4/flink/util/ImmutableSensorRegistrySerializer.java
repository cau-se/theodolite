package rocks.theodolite.benchmarks.uc4.flink.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.io.Serializable;
import titan.ccp.model.sensorregistry.ImmutableSensorRegistry;

/**
 * A {@link Serializer} for {@link ImmutableSensorRegistry}s.
 */
public class ImmutableSensorRegistrySerializer extends Serializer<ImmutableSensorRegistry>
    implements Serializable {

  private static final long serialVersionUID = 1806411056006113017L; // NOPMD

  @Override
  public void write(final Kryo kryo, final Output output, final ImmutableSensorRegistry object) {
    output.writeString(object.toJson());
  }

  @Override
  public ImmutableSensorRegistry read(final Kryo kryo, final Input input,
      final Class<ImmutableSensorRegistry> type) {
    return (ImmutableSensorRegistry) ImmutableSensorRegistry.fromJson(input.readString());
  }
}
