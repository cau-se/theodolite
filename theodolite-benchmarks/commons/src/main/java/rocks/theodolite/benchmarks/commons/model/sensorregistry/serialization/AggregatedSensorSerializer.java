package rocks.theodolite.benchmarks.commons.model.sensorregistry.serialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import rocks.theodolite.benchmarks.commons.model.sensorregistry.AggregatedSensor;

import java.lang.reflect.Type;

/**
 * {@link JsonSerializer} for {@link AggregatedSensor}s.
 */
public final class AggregatedSensorSerializer implements JsonSerializer<AggregatedSensor> {

  @Override
  public JsonElement serialize(final AggregatedSensor sensor, final Type type,
      final JsonSerializationContext context) {
    final JsonObject jsonSensorObject = new JsonObject();
    jsonSensorObject.addProperty("identifier", sensor.getIdentifier());
    jsonSensorObject.addProperty("name", sensor.getName());
    jsonSensorObject.add("children", context.serialize(sensor.getChildren()));
    return jsonSensorObject;
  }

}
