package rocks.theodolite.benchmarks.commons.model.sensorregistry.serialization;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.lang.reflect.Type;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.AggregatedSensor;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.ImmutableSensorRegistry;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.MutableAggregatedSensor;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.MutableSensorRegistry;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.SensorRegistry;

/**
 * {@link JsonDeserializer} for {@link AggregatedSensor}s.
 */
public final class SensorRegistryDeserializer implements JsonDeserializer<SensorRegistry> {

  private static final String IDENTIFIER_KEY = "identifier";

  private static final String NAME_KEY = "name";

  private static final String CHILDREN_KEY = "children";

  @Override
  public SensorRegistry deserialize(final JsonElement jsonElement, final Type type,
      final JsonDeserializationContext context) {
    final MutableSensorRegistry sensorRegistry = this.transformTopLevelSensor(jsonElement);

    return ImmutableSensorRegistry.copyOf(sensorRegistry);
  }

  private MutableSensorRegistry transformTopLevelSensor(final JsonElement jsonElement) {
    final SensorParseResult parseResult = this.parseSensor(jsonElement); //
    if (parseResult == null) {
      // create empty registry
      return new MutableSensorRegistry("", "");
    } else {
      // create registry from result
      final MutableSensorRegistry sensorRegistry =
          new MutableSensorRegistry(parseResult.identifier, parseResult.name);
      if (parseResult.children != null) {
        for (final JsonElement childJsonElement : parseResult.children) {
          this.addSensor(childJsonElement, sensorRegistry.getTopLevelSensor());
        }
      }
      return sensorRegistry;
    }
  }

  private void addSensor(final JsonElement jsonElement,
      final MutableAggregatedSensor parentSensor) {
    final SensorParseResult parseResult = this.parseSensor(jsonElement);
    if (parseResult != null) {
      // create child sensor from result
      if (parseResult.children == null) {
        // create MachineSensor
        parentSensor.addChildMachineSensor(parseResult.identifier, parseResult.name);
      } else {
        // create Aggregated Sensor
        final MutableAggregatedSensor sensor =
            parentSensor.addChildAggregatedSensor(parseResult.identifier, parseResult.name);
        for (final JsonElement childJsonElement : parseResult.children) {
          this.addSensor(childJsonElement, sensor);
        }
      }
    }
  }

  // returns null if invalid JsonElement
  private SensorParseResult parseSensor(final JsonElement jsonElement) {
    if (jsonElement.isJsonObject()) {
      final JsonObject jsonObject = jsonElement.getAsJsonObject();
      if (jsonObject.has(IDENTIFIER_KEY)) {
        final JsonElement identifierJsonElement = jsonObject.get(IDENTIFIER_KEY);
        final JsonElement nameJsonElement = jsonObject.get(NAME_KEY);
        if (identifierJsonElement.isJsonPrimitive() && nameJsonElement.isJsonPrimitive()) {
          final JsonPrimitive identifierJsonPrimitive = identifierJsonElement.getAsJsonPrimitive();
          final JsonPrimitive nameJsonPrimitive = nameJsonElement.getAsJsonPrimitive();
          if (identifierJsonPrimitive.isString()) {
            final String identifierString = identifierJsonPrimitive.getAsString();
            final String nameString = nameJsonPrimitive.getAsString();
            final JsonArray childrenJsonArray = this.parseChildren(jsonObject);
            return new SensorParseResult(identifierString, nameString, childrenJsonArray);
          }
        }
      }
    }
    return null;
    // TODO throw exception
  }

  // returns null if JsonObject does not have children or children is not an array
  private JsonArray parseChildren(final JsonObject parentJsonObject) {
    if (parentJsonObject.has(CHILDREN_KEY)) {
      final JsonElement childrenJsonElement = parentJsonObject.get(CHILDREN_KEY);
      if (childrenJsonElement.isJsonArray()) {
        return childrenJsonElement.getAsJsonArray();
      }
    }
    return null;
    // TODO throw exception
  }

  private static class SensorParseResult {
    private final String identifier;
    private final String name;
    private final JsonArray children;

    private SensorParseResult(final String identifier, final String name,
        final JsonArray children) {
      this.identifier = identifier;
      this.children = children;
      this.name = name;
    }
  }

}
