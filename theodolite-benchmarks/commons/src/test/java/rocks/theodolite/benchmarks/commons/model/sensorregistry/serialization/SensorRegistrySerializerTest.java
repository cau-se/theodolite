package rocks.theodolite.benchmarks.commons.model.sensorregistry.serialization;

import static org.junit.Assert.assertEquals;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import rocks.theodolite.benchmarks.commons.model.sensorregistry.MaschineSensorImplExposer;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.MutableAggregatedSensor;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.MutableSensorRegistry;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.serialization.AggregatedSensorSerializer;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.serialization.MachineSensorSerializer;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.serialization.SensorRegistrySerializer;

public class SensorRegistrySerializerTest {

  private Gson gson;

  @Before
  public void setUp() throws Exception {
    this.gson = new GsonBuilder()
        .registerTypeAdapter(MutableSensorRegistry.class, new SensorRegistrySerializer())
        .registerTypeAdapter(MutableAggregatedSensor.class, new AggregatedSensorSerializer())
        .registerTypeAdapter(MaschineSensorImplExposer.MACHINE_SENSOR_IMPL_CLASS,
            new MachineSensorSerializer())
        .create();
  }

  @After
  public void tearDown() throws Exception {
    this.gson = null;
  }

  @Test
  public void testEmptySensorRegistry() {
    final MutableSensorRegistry sensorRegistry = new MutableSensorRegistry("root", "Root");

    final String json = this.gson.toJson(sensorRegistry);
    System.out.println(json);
    assertEquals(json, "{\"identifier\":\"root\",\"name\":\"Root\",\"children\":[]}");
  }

  @Test
  public void testEmptySensorRegistryWithChildren() {
    final MutableSensorRegistry sensorRegistry = new MutableSensorRegistry("root", "Root");
    final MutableAggregatedSensor topLevel = sensorRegistry.getTopLevelSensor();
    topLevel.addChildMachineSensor("child-1", "Child 1");
    topLevel.addChildMachineSensor("child-2");

    final String json = this.gson.toJson(sensorRegistry);
    System.out.println(json);
    assertEquals(json,
        "{\"identifier\":\"root\",\"name\":\"Root\",\"children\":[{\"identifier\":\"child-1\",\"name\":\"Child 1\"},{\"identifier\":\"child-2\",\"name\":\"\"}]}");
  }

  @Test
  public void testEmptySensorRegistryWithGrandChildren() {
    final MutableSensorRegistry sensorRegistry = new MutableSensorRegistry("root", "Root");
    final MutableAggregatedSensor topLevel = sensorRegistry.getTopLevelSensor();
    final MutableAggregatedSensor aggregatedSensor =
        topLevel.addChildAggregatedSensor("child-1", "Child 1");
    aggregatedSensor.addChildMachineSensor("child-1-1", "Child 1a");
    aggregatedSensor.addChildMachineSensor("child-1-2", "Child 1b");
    topLevel.addChildMachineSensor("child-2", "Child 2");

    final String json = this.gson.toJson(sensorRegistry);
    System.out.println(json);
    assertEquals(json,
        "{\"identifier\":\"root\",\"name\":\"Root\",\"children\":[{\"identifier\":\"child-1\",\"name\":\"Child 1\",\"children\":[{\"identifier\":\"child-1-1\",\"name\":\"Child 1a\"},{\"identifier\":\"child-1-2\",\"name\":\"Child 1b\"}]},{\"identifier\":\"child-2\",\"name\":\"Child 2\"}]}");
  }

}
