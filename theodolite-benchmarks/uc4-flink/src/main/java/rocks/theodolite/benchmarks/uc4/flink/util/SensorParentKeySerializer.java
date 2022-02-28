package rocks.theodolite.benchmarks.uc4.flink.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.io.Serializable;

/**
 * Kryo serializer for {@link SensorParentKey}.
 */
public final class SensorParentKeySerializer extends Serializer<SensorParentKey>
    implements Serializable {

  private static final long serialVersionUID = -867781963471414857L; // NOPMD

  @Override
  public void write(final Kryo kryo, final Output output, final SensorParentKey object) {
    output.writeString(object.getSensor());
    output.writeString(object.getParent());
  }

  @Override
  public SensorParentKey read(final Kryo kryo, final Input input,
      final Class<SensorParentKey> type) {
    final String sensor = input.readString();
    final String parent = input.readString();
    return new SensorParentKey(sensor, parent);
  }
}
