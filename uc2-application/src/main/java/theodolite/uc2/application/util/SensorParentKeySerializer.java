package theodolite.uc2.application.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.io.Serializable;

/**
 * Kryo serializer for {@link SensorParentKey}.
 */
public final class SensorParentKeySerializer extends Serializer<SensorParentKey> implements Serializable {

  @Override
  public void write(Kryo kryo, Output output, SensorParentKey object) {
    output.writeString(object.getSensor());
    output.writeString(object.getParent());
  }

  @Override
  public SensorParentKey read(Kryo kryo, Input input, Class<SensorParentKey> type) {
    final String sensor = input.readString();
    final String parent = input.readString();
    return new SensorParentKey(sensor, parent);
  }
}
