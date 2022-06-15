package rocks.theodolite.benchmarks.commons.model.sensorregistry.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.MachineSensor;

/**
 * {@link JsonSerializer} for {@link MachineSensor}s.
 */
public final class MachineSensorSerializer implements JsonSerializer<MachineSensor> {

  @Override
  public JsonElement serialize(final MachineSensor sensor, final Type type,
      final JsonSerializationContext context) {
    final JsonObject jsonSensorObject = new JsonObject();
    jsonSensorObject.addProperty("identifier", sensor.getIdentifier());
    jsonSensorObject.addProperty("name", sensor.getName());
    return jsonSensorObject;
  }

}
