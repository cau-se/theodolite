package rocks.theodolite.commons.model.sensorregistry.serialization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import rocks.theodolite.commons.model.sensorregistry.AggregatedSensor;
import rocks.theodolite.commons.model.sensorregistry.MachineSensor;
import rocks.theodolite.commons.model.sensorregistry.Sensor;
import rocks.theodolite.commons.model.sensorregistry.SensorRegistry;

public class SensorRegistryDeserializerTest {

	private Gson gson;

	@Before
	public void setUp() throws Exception {
		this.gson = new GsonBuilder().registerTypeAdapter(SensorRegistry.class, new SensorRegistryDeserializer()).create();
	}

	@After
	public void tearDown() throws Exception {
		this.gson = null;
	}

	@Test
	public void testEmptyRegistry() {
		final String json = "";
		final SensorRegistry registry = this.gson.fromJson(json, SensorRegistry.class);
		assertNull(registry);
	}

	@Test
	public void testRegistryOfWrongType() {
		final String json = "[{\"identifier\": \"my-id\", \"name\": \"My Name\"}]";
		final SensorRegistry registry = this.gson.fromJson(json, SensorRegistry.class);
		assertEquals(registry.getTopLevelSensor().getIdentifier(), "");
		assertTrue(registry.getTopLevelSensor().getChildren().isEmpty());
	}

	@Test
	public void testRegistryWithMissingIdentifier() {
		final String json = "{\"children\": []}";
		final SensorRegistry registry = this.gson.fromJson(json, SensorRegistry.class);
		assertEquals(registry.getTopLevelSensor().getIdentifier(), "");
		assertTrue(registry.getTopLevelSensor().getChildren().isEmpty());
	}

	@Test
	public void testRegistryWithMissingChildren() {
		final String json = "{\"identifier\": \"my-root-id\", \"name\": \"My Name\"}";
		final SensorRegistry registry = this.gson.fromJson(json, SensorRegistry.class);
		assertEquals(registry.getTopLevelSensor().getIdentifier(), "my-root-id");
		assertTrue(registry.getTopLevelSensor().getChildren().isEmpty());
	}

	@Test
	public void testRegistryWithZeroChildren() {
		final String json = "{\"identifier\": \"my-root-id\", \"name\": \"My Name\", \"children\": []}";
		final SensorRegistry registry = this.gson.fromJson(json, SensorRegistry.class);
		assertEquals(registry.getTopLevelSensor().getIdentifier(), "my-root-id");
		assertTrue(registry.getTopLevelSensor().getChildren().isEmpty());
	}

	@Test
	public void testRegistryWithOneGenerationChildren() {
		final String json = "{\"identifier\": \"my-root-id\", \"name\": \"My Name\", \"children\": [{\"identifier\": \"child-id-1\", \"name\": \"Child 1\"}, {\"identifier\": \"child-id-2\", \"name\": \"Child 2\"}, {\"identifier\": \"child-id-3\", \"name\": \"Child 3\"}]}";
		final List<String> childIdentifiers = ImmutableList.of("child-id-1", "child-id-2", "child-id-3"); // List.of() in Java <= 9

		final SensorRegistry registry = this.gson.fromJson(json, SensorRegistry.class);
		final AggregatedSensor topLevelSensor = registry.getTopLevelSensor();
		assertEquals(topLevelSensor.getIdentifier(), "my-root-id");
		final List<Sensor> childSensors = Lists.newArrayList(topLevelSensor.getChildren());
		assertEquals(childSensors.size(), 3);
		for (final Sensor sensor : childSensors) {
			assertTrue(childIdentifiers.contains(sensor.getIdentifier()));
			assertTrue(sensor instanceof MachineSensor);
		}
		for (final String childIdentifier : childIdentifiers) {
			assertTrue(registry.getSensorForIdentifier(childIdentifier).isPresent());
		}
	}

	@Test
	public void testRegistryWithCorruptedChild() {
		final String json = "{\"identifier\": \"my-root-id\", \"name\": \"My Name\", \"children\": [{\"identifier\": \"child-id-1\", \"name\": \"Child 1\"}, {\"no-identifier\": \"child-id-2\", \"name\": \"Child 2\"}]}";
		final List<String> childIdentifiers = ImmutableList.of("child-id-1"); // List.of() in Java <= 9

		final SensorRegistry registry = this.gson.fromJson(json, SensorRegistry.class);
		final AggregatedSensor topLevelSensor = registry.getTopLevelSensor();
		assertEquals(topLevelSensor.getIdentifier(), "my-root-id");
		final List<Sensor> childSensors = Lists.newArrayList(topLevelSensor.getChildren());
		assertEquals(childSensors.size(), 1);
		for (final Sensor sensor : childSensors) {
			assertTrue(childIdentifiers.contains(sensor.getIdentifier()));
			assertTrue(sensor instanceof MachineSensor);
		}
		for (final String childIdentifier : childIdentifiers) {
			assertTrue(registry.getSensorForIdentifier(childIdentifier).isPresent());
		}
	}

	@Test
	public void testRegistryWithArrayAsChild() {
		final String json = "{\"identifier\": \"my-root-id\", \"name\": \"My Name\", \"children\": [{\"identifier\": \"child-id-1\", \"name\": \"Child 1\"}, [{\"identifier\": \"child-id-2\", \"name\": \"Child 2\"}]]}";
		final List<String> childIdentifiers = ImmutableList.of("child-id-1"); // List.of() in Java <= 9

		final SensorRegistry registry = this.gson.fromJson(json, SensorRegistry.class);
		final AggregatedSensor topLevelSensor = registry.getTopLevelSensor();
		assertEquals(topLevelSensor.getIdentifier(), "my-root-id");
		final List<Sensor> childSensors = Lists.newArrayList(topLevelSensor.getChildren());
		assertEquals(childSensors.size(), 1);
		for (final Sensor sensor : childSensors) {
			assertTrue(childIdentifiers.contains(sensor.getIdentifier()));
			assertTrue(sensor instanceof MachineSensor);
		}
		for (final String childIdentifier : childIdentifiers) {
			assertTrue(registry.getSensorForIdentifier(childIdentifier).isPresent());
		}
	}

	@Test
	public void testRegistryWithTwoGenerationChildren() {
		final String json = "{\"identifier\": \"my-root-id\", \"name\": \"My Name\", \"children\": [{\"identifier\": \"child-id-1\", \"name\": \"Child 1\", \"children\": [{\"identifier\": \"child-id-1-1\", \"name\": \"Child 1a\"}, {\"identifier\": \"child-id-1-2\", \"name\": \"Child 1b\"}, {\"identifier\": \"child-id-1-3\", \"name\": \"Child 1c\"}]}, {\"identifier\": \"child-id-2\", \"name\": \"Child 2\"}]}";
		final List<String> childIdentifiers = ImmutableList.of("child-id-1", "child-id-2"); // List.of() in Java <= 9
		final List<String> grandChildIdentifiers = ImmutableList.of("child-id-1-1", "child-id-1-2", "child-id-1-3"); // List.of() in Java <= 9
		final List<String> machineSensorIdentifiers = ImmutableList.of("child-id-2", "child-id-1-1", "child-id-1-2", // List.of() in Java <= 9
				"child-id-1-3");

		final SensorRegistry registry = this.gson.fromJson(json, SensorRegistry.class);
		final AggregatedSensor topLevelSensor = registry.getTopLevelSensor();
		assertEquals(topLevelSensor.getIdentifier(), "my-root-id");
		final List<Sensor> childSensors = Lists.newArrayList(topLevelSensor.getChildren());
		assertEquals(childSensors.size(), 2);
		for (final Sensor sensor : childSensors) {
			assertTrue(childIdentifiers.contains(sensor.getIdentifier()));
			if (sensor.getIdentifier().equals("child-id-2")) {
				assertTrue(sensor instanceof MachineSensor);
			} else if (sensor.getIdentifier().equals("child-id-1")) {
				assertTrue(sensor instanceof AggregatedSensor);
				if (sensor instanceof AggregatedSensor) {
					final AggregatedSensor aggregatedSensor = (AggregatedSensor) sensor;
					final List<Sensor> grandChildSensors = Lists.newArrayList(aggregatedSensor.getChildren());
					assertEquals(grandChildSensors.size(), 3);
					for (final Sensor grandChildSensor : grandChildSensors) {
						assertTrue(grandChildIdentifiers.contains(grandChildSensor.getIdentifier()));
						assertTrue(grandChildSensor instanceof MachineSensor);
					}
				} else {
					fail(); // Should never happen because of asserTrue check before
				}
			} else {
				fail("Sensor is neither of type MachineSensor nor AggregatedSensor");
			}
		}
		for (final String identifier : machineSensorIdentifiers) {
			assertTrue(registry.getSensorForIdentifier(identifier).isPresent());
		}
	}

}
