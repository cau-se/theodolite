package theodolite.commons.model.sensorregistry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import titan.ccp.model.sensorregistry.serialization.AggregatedSensorSerializer;
import titan.ccp.model.sensorregistry.serialization.MachineSensorSerializer;
import titan.ccp.model.sensorregistry.serialization.SensorRegistryDeserializer;
import titan.ccp.model.sensorregistry.serialization.SensorRegistrySerializer;

/**
 * Implementation of a {@link SensorRegistry} that is immutable.
 */
public final class ImmutableSensorRegistry implements SensorRegistry {

  private static final Gson GSON = new GsonBuilder()
      .registerTypeAdapter(ImmutableSensorRegistry.class, new SensorRegistrySerializer())
      .registerTypeAdapter(ImmutableSensorRegistry.ImmutableAggregatatedSensor.class,
          new AggregatedSensorSerializer())
      .registerTypeAdapter(MachineSensorImpl.class, new MachineSensorSerializer())
      .registerTypeAdapter(SensorRegistry.class, new SensorRegistryDeserializer()).create();

  private final ImmutableMap<String, MachineSensor> machineSensors;
  private final AggregatedSensor topLevelSensor;

  private ImmutableSensorRegistry(final SensorRegistry sensorRegistry) {
    final ImmutableMap.Builder<String, MachineSensor> mapBuilder = ImmutableMap.builder();
    this.topLevelSensor =
        new ImmutableAggregatatedSensor(null, sensorRegistry.getTopLevelSensor(), mapBuilder);
    this.machineSensors = mapBuilder.build();
  }

  @Override
  public Optional<MachineSensor> getSensorForIdentifier(final String identifier) {
    return Optional.ofNullable(this.machineSensors.get(identifier));
  }

  @Override
  public AggregatedSensor getTopLevelSensor() {
    return this.topLevelSensor;
  }

  @Override
  public Collection<MachineSensor> getMachineSensors() {
    return this.machineSensors.values();
  }

  @Override
  public String toJson() {
    // Necessary method. Deletion would cause SensorRegistry.toJson() to fail.
    return GSON.toJson(this);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this.topLevelSensor);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof SensorRegistry) {
      final SensorRegistry other = (SensorRegistry) obj;
      return Objects.equals(this.getTopLevelSensor(), other.getTopLevelSensor());
    }
    return false;
  }

  public static ImmutableSensorRegistry copyOf(final SensorRegistry sensorRegistry) {
    return new ImmutableSensorRegistry(sensorRegistry);
  }

  public static SensorRegistry fromJson(final String json) {
    return GSON.fromJson(json, SensorRegistry.class);
  }

  private static final class ImmutableAggregatatedSensor extends AbstractSensor
      implements AggregatedSensor {

    private final ImmutableList<Sensor> children;

    private ImmutableAggregatatedSensor(final AggregatedSensor newParent,
        final AggregatedSensor sensorToCopy,
        final ImmutableMap.Builder<String, MachineSensor> sensorRegistryMapBuilder) {
      super(newParent, sensorToCopy.getIdentifier(), sensorToCopy.getName());
      final Builder<Sensor> childrenBuilder = ImmutableList.builder();
      for (final Sensor child : sensorToCopy.getChildren()) {
        if (child instanceof MachineSensor) {
          final MachineSensor newChild =
              new MachineSensorImpl(this, child.getIdentifier(), child.getName());
          childrenBuilder.add(newChild);
          sensorRegistryMapBuilder.put(newChild.getIdentifier(), newChild);
        } else if (child instanceof AggregatedSensor) {
          final AggregatedSensor newChild = new ImmutableAggregatatedSensor(this,
              (AggregatedSensor) child, sensorRegistryMapBuilder);
          childrenBuilder.add(newChild);
        } else {
          throw new IllegalStateException(
              "Sensor " + child + " is neither of type '"
                  + MachineSensor.class.getSimpleName() + "' nor "
                  + AggregatedSensor.class.getSimpleName() + "' and thus not supported.");
        }
      }
      this.children = childrenBuilder.build();
    }

    @Override
    public Collection<Sensor> getChildren() {
      return this.children;
    }

    @Override
    public int hashCode() {
      return Objects.hash(this.getIdentifier(), this.children);
    }

    @Override
    public boolean equals(final Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj instanceof AggregatedSensor) {
        final AggregatedSensor other = (AggregatedSensor) obj;
        return Objects.equals(this.getIdentifier(), other.getIdentifier())
            && Objects.equals(this.children, other.getChildren());
      }
      return false;
    }

    @Override
    public String toString() {
      return this.getName() + '[' + this.getIdentifier() + "] (" + this.children.size()
          + " children)";
    }

  }

}
