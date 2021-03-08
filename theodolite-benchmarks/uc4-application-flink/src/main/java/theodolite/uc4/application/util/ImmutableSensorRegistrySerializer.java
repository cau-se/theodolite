package theodolite.uc4.application.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import titan.ccp.model.sensorregistry.ImmutableSensorRegistry;

import java.io.Serializable;

public class ImmutableSensorRegistrySerializer extends Serializer<ImmutableSensorRegistry> implements Serializable {

  private static final long serialVersionUID = 1806411056006113017L;

  @Override
  public void write(Kryo kryo, Output output, ImmutableSensorRegistry object) {
    output.writeString(object.toJson());
  }

  @Override
  public ImmutableSensorRegistry read(Kryo kryo, Input input, Class<ImmutableSensorRegistry> type) {
    return (ImmutableSensorRegistry) ImmutableSensorRegistry.fromJson(input.readString());
  }
}
