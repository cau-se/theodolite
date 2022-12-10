package rocks.theodolite.benchmarks.uc4.kstreams;


import java.time.Duration;
import java.time.Instant;
import java.util.Properties;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import rocks.theodolite.benchmarks.commons.configuration.events.Event;
import rocks.theodolite.benchmarks.commons.configuration.events.EventSerde;
import rocks.theodolite.benchmarks.commons.kafka.avro.SchemaRegistryAvroSerdeFactory;
import rocks.theodolite.benchmarks.commons.model.records.ActivePowerRecord;
import rocks.theodolite.benchmarks.commons.model.records.AggregatedActivePowerRecord;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.MutableAggregatedSensor;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.MutableSensorRegistry;


public class TopologyTest {

  private static final String INPUT_TOPIC = "input";
  private static final String OUTPUT_TOPIC = "output";
  private static final String FEEDBACK_TOPIC = "feedback";
  private static final String CONFIGURATION_TOPIC = "configuration";

  private TopologyTestDriver testDriver;
  private TestInputTopic<Event, String> configurationTopic;
  private TestInputTopic<String, ActivePowerRecord> inputTopic;
  private TestOutputTopic<String, AggregatedActivePowerRecord> outputTopic;
  // private TestOutputTopic<String, AggregatedActivePowerRecord> feedbackTopic;
  private SchemaRegistryAvroSerdeFactory serdes;

  @Before
  public void setup() {
    this.serdes = new MockedSchemaRegistrySerdes();

    final Properties props = new Properties();
    props.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "dummy-aggregation");
    props.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");

    final Topology topology = new TopologyBuilder(
        INPUT_TOPIC,
        OUTPUT_TOPIC,
        // FEEDBACK_TOPIC,
        CONFIGURATION_TOPIC,
        Duration.ofSeconds(2),
        Duration.ofSeconds(2),
        this.serdes).build(props);

    // setup test driver
    this.testDriver = new TopologyTestDriver(topology, props);
    this.inputTopic = this.testDriver.createInputTopic(
        INPUT_TOPIC,
        Serdes.String().serializer(),
        this.serdes.<ActivePowerRecord>forValues().serializer());
    this.configurationTopic = this.testDriver.createInputTopic(
        CONFIGURATION_TOPIC,
        EventSerde.serde().serializer(),
        Serdes.String().serializer());
    this.testDriver.createInputTopic(
        OUTPUT_TOPIC,
        Serdes.String().serializer(),
        this.serdes.forValues().serializer());
    this.testDriver.createInputTopic(
        FEEDBACK_TOPIC,
        Serdes.String().serializer(),
        this.serdes.<AggregatedActivePowerRecord>forValues().serializer());
    this.outputTopic = this.testDriver.createOutputTopic(
        OUTPUT_TOPIC,
        Serdes.String().deserializer(),
        this.serdes.<AggregatedActivePowerRecord>forValues().deserializer());
    // this.feedbackTopic = this.testDriver.createOutputTopic(
    // FEEDBACK_TOPIC,
    // this.serdes.string().deserializer(),
    // this.serdes.aggregatedActivePowerRecordValues().deserializer());

  }

  @After
  public void tearDown() {
    this.testDriver.close();
  }

  @Test
  @Ignore
  public void testOneLevelRegistry() {
    // Publish sensor registry
    final MutableSensorRegistry registry = new MutableSensorRegistry("root");
    final MutableAggregatedSensor root = registry.getTopLevelSensor();
    root.addChildMachineSensor("child1");
    root.addChildMachineSensor("child2");
    this.configurationTopic.pipeInput(
        Event.SENSOR_REGISTRY_CHANGED,
        registry.toJson(),
        Instant.ofEpochSecond(0));

    // Publish input records
    this.pipeInput("child1", Instant.ofEpochSecond(0), 100.0);
    this.pipeInput("child2", Instant.ofEpochSecond(1), 100.0);

    // Advance time to obtain outputs
    this.pipeInput("child2", Instant.ofEpochSecond(2), 123.0);

    // Check aggregation results
    Assert.assertEquals(1, this.outputTopic.getQueueSize());

    final AggregatedActivePowerRecord result = this.outputTopic.readValue();
    Assert.assertEquals("root", result.getIdentifier());
    Assert.assertEquals(2, result.getCount());
    Assert.assertEquals(200.0, result.getSumInW(), 0.1);
  }

  @Test
  @Ignore
  public void shouldOnlyConsiderLatestValue() {
    // Publish sensor registry
    final MutableSensorRegistry registry = new MutableSensorRegistry("root");
    final MutableAggregatedSensor root = registry.getTopLevelSensor();
    root.addChildMachineSensor("child1");
    root.addChildMachineSensor("child2");
    this.configurationTopic.pipeInput(
        Event.SENSOR_REGISTRY_CHANGED,
        registry.toJson(),
        Instant.ofEpochSecond(0));

    // Publish input records
    this.pipeInput("child1", Instant.ofEpochMilli(500), 50.0);
    this.pipeInput("child2", Instant.ofEpochMilli(1000), 100.0);
    this.pipeInput("child1", Instant.ofEpochMilli(1500), 400.0);

    // Advance time to obtain outputs
    this.pipeInput("child2", Instant.ofEpochSecond(3), 123.0);

    // Check aggregation results
    Assert.assertEquals(1, this.outputTopic.getQueueSize());
    final AggregatedActivePowerRecord result = this.outputTopic.readValue();
    Assert.assertEquals("root", result.getIdentifier());
    Assert.assertEquals(2, result.getCount());
    Assert.assertEquals(500.0, result.getSumInW(), 0.1);
  }

  @Test
  @Ignore
  public void shouldOnlyConsiderLatestValueWhenOutOfOrder() {
    // Publish sensor registry
    final MutableSensorRegistry registry = new MutableSensorRegistry("root");
    final MutableAggregatedSensor root = registry.getTopLevelSensor();
    root.addChildMachineSensor("child1");
    root.addChildMachineSensor("child2");
    this.configurationTopic.pipeInput(
        Event.SENSOR_REGISTRY_CHANGED,
        registry.toJson(),
        Instant.ofEpochSecond(0));

    // Publish input records
    this.pipeInput("child2", Instant.ofEpochMilli(1000), 100.0);
    this.pipeInput("child1", Instant.ofEpochMilli(1500), 400.0);
    this.pipeInput("child1", Instant.ofEpochMilli(500), 50.0);

    // Advance time to obtain outputs
    this.pipeInput("child2", Instant.ofEpochSecond(3), 123.0);

    // Check aggregation results
    Assert.assertEquals(1, this.outputTopic.getQueueSize());
    final AggregatedActivePowerRecord result = this.outputTopic.readValue();
    Assert.assertEquals("root", result.getIdentifier());
    Assert.assertEquals(2, result.getCount());
    Assert.assertEquals(500.0, result.getSumInW(), 0.1);
  }

  @Test
  @Ignore
  public void shouldHandleUpdateDuringGracePeriod() {
    // Publish sensor registry
    final MutableSensorRegistry registry = new MutableSensorRegistry("root");
    final MutableAggregatedSensor root = registry.getTopLevelSensor();
    root.addChildMachineSensor("child1");
    root.addChildMachineSensor("child2");
    this.configurationTopic.pipeInput(
        Event.SENSOR_REGISTRY_CHANGED,
        registry.toJson(),
        Instant.ofEpochSecond(0));

    // Publish input records
    this.pipeInput("child1", Instant.ofEpochMilli(500), 50.0);
    this.pipeInput("child2", Instant.ofEpochMilli(1000), 100.0);
    // Advance time to obtain outputs
    this.pipeInput("child2", Instant.ofEpochSecond(3), 123.0);
    // This record arrives out-of-order but withing the grace period
    this.pipeInput("child1", Instant.ofEpochMilli(1500), 400.0);
    // Advance time to obtain outputs
    this.pipeInput("child2", Instant.ofEpochSecond(5), 123.0);

    // Check aggregation results
    Assert.assertEquals(3, this.outputTopic.getQueueSize());

    final AggregatedActivePowerRecord firstResult = this.outputTopic.readValue();
    Assert.assertEquals("root", firstResult.getIdentifier());
    Assert.assertEquals(2, firstResult.getCount());
    Assert.assertEquals(150.0, firstResult.getSumInW(), 0.1);

    final AggregatedActivePowerRecord secondResult = this.outputTopic.readValue();
    Assert.assertEquals("root", secondResult.getIdentifier());
    Assert.assertEquals(2, secondResult.getCount());
    Assert.assertEquals(500.0, secondResult.getSumInW(), 0.1);

    final AggregatedActivePowerRecord thirdResult = this.outputTopic.readValue();
    Assert.assertEquals("root", thirdResult.getIdentifier());
    Assert.assertEquals(1, thirdResult.getCount());
    Assert.assertEquals(123.0, thirdResult.getSumInW(), 0.1);
  }

  @Test
  @Ignore
  public void shouldNotHandleUpdateAfterGracePeriod() {
    // Publish sensor registry
    final MutableSensorRegistry registry = new MutableSensorRegistry("root");
    final MutableAggregatedSensor root = registry.getTopLevelSensor();
    root.addChildMachineSensor("child1");
    root.addChildMachineSensor("child2");
    this.configurationTopic.pipeInput(
        Event.SENSOR_REGISTRY_CHANGED,
        registry.toJson(),
        Instant.ofEpochSecond(0));

    // Publish input records
    this.pipeInput("child1", Instant.ofEpochMilli(500), 50.0);
    this.pipeInput("child2", Instant.ofEpochMilli(1000), 100.0);
    // Advance time to obtain outputs
    this.pipeInput("child2", Instant.ofEpochSecond(3), 123.0);
    // Advance time to obtain outputs
    this.pipeInput("child2", Instant.ofEpochSecond(5), 123.0);
    // This record arrives out-of-order and after the grace period has expired
    this.pipeInput("child1", Instant.ofEpochMilli(1500), 400.0);
    // Advance time to obtain outputs
    this.pipeInput("child2", Instant.ofEpochSecond(7), 123.0);

    // Check aggregation results
    Assert.assertEquals(3, this.outputTopic.getQueueSize());

    final AggregatedActivePowerRecord firstResult = this.outputTopic.readValue();
    Assert.assertEquals("root", firstResult.getIdentifier());
    Assert.assertEquals(2, firstResult.getCount());
    Assert.assertEquals(150.0, firstResult.getSumInW(), 0.1);

    final AggregatedActivePowerRecord secondResult = this.outputTopic.readValue();
    Assert.assertEquals("root", secondResult.getIdentifier());
    Assert.assertEquals(1, secondResult.getCount());
    Assert.assertEquals(123.0, secondResult.getSumInW(), 0.1);

    final AggregatedActivePowerRecord thirdResult = this.outputTopic.readValue();
    Assert.assertEquals("root", thirdResult.getIdentifier());
    Assert.assertEquals(1, thirdResult.getCount());
    Assert.assertEquals(123.0, thirdResult.getSumInW(), 0.1);
  }

  @Test
  @Ignore
  public void testTwoLevelRegistry() {
    // Publish sensor registry
    final MutableSensorRegistry registry = new MutableSensorRegistry("root");
    final MutableAggregatedSensor root = registry.getTopLevelSensor();
    final MutableAggregatedSensor sensor1 = root.addChildAggregatedSensor("sensor-1");
    sensor1.addChildAggregatedSensor("sensor-1-1");
    sensor1.addChildAggregatedSensor("sensor-1-2");
    final MutableAggregatedSensor sensor2 = root.addChildAggregatedSensor("sensor-2");
    sensor2.addChildAggregatedSensor("sensor-2-1");
    sensor2.addChildAggregatedSensor("sensor-2-2");
    this.configurationTopic.pipeInput(
        Event.SENSOR_REGISTRY_CHANGED,
        registry.toJson(),
        Instant.ofEpochSecond(0));

    // Publish input records
    this.pipeInput("sensor-1-1", Instant.ofEpochSecond(0), 100.0);
    this.pipeInput("sensor-2-1", Instant.ofEpochSecond(0), 100.0);
    this.pipeInput("sensor-1-2", Instant.ofEpochSecond(1), 100.0);
    this.pipeInput("sensor-2-2", Instant.ofEpochSecond(1), 100.0);

    // Advance time to obtain outputs
    this.pipeInput("sensor-2-2", Instant.ofEpochSecond(2), 123.0);

    // Check aggregation results
    Assert.assertEquals(3, this.outputTopic.getQueueSize());

    final AggregatedActivePowerRecord rootResult = this.outputTopic.readValue();
    Assert.assertEquals("root", rootResult.getIdentifier());
    Assert.assertEquals(2, rootResult.getCount());
    Assert.assertEquals(400.0, rootResult.getSumInW(), 0.1);

    final AggregatedActivePowerRecord sensor1Result = this.outputTopic.readValue();
    Assert.assertEquals("sensor-1", sensor1Result.getIdentifier());
    Assert.assertEquals(2, sensor1Result.getCount());
    Assert.assertEquals(200.0, sensor1Result.getSumInW(), 0.1);

    final AggregatedActivePowerRecord sensor2Result = this.outputTopic.readValue();
    Assert.assertEquals("sensor-2", sensor2Result.getIdentifier());
    Assert.assertEquals(2, sensor2Result.getCount());
    Assert.assertEquals(200.0, sensor2Result.getSumInW(), 0.1);
  }

  @Test
  @Ignore
  public void testUnbalancedRegistry() {
    // Publish sensor registry
    final MutableSensorRegistry registry = new MutableSensorRegistry("root");
    final MutableAggregatedSensor root = registry.getTopLevelSensor();
    final MutableAggregatedSensor sensor1 = root.addChildAggregatedSensor("sensor-1");
    sensor1.addChildMachineSensor("sensor-1-1");
    sensor1.addChildMachineSensor("sensor-1-2");
    final MutableAggregatedSensor sensor2 = root.addChildAggregatedSensor("sensor-2");
    final MutableAggregatedSensor sensor21 = sensor2.addChildAggregatedSensor("sensor-2-1");
    sensor21.addChildMachineSensor("sensor-2-1-1");
    sensor21.addChildMachineSensor("sensor-2-1-2");
    final MutableAggregatedSensor sensor22 = sensor2.addChildAggregatedSensor("sensor-2-2");
    sensor22.addChildMachineSensor("sensor-2-2-1");
    sensor22.addChildMachineSensor("sensor-2-2-2");
    this.configurationTopic.pipeInput(
        Event.SENSOR_REGISTRY_CHANGED,
        registry.toJson(),
        Instant.ofEpochSecond(0));

    // Publish input records
    this.pipeInput("sensor-1-1", Instant.ofEpochSecond(0), 100.0);
    this.pipeInput("sensor-2-1-1", Instant.ofEpochSecond(0), 10.0);
    this.pipeInput("sensor-2-2-1", Instant.ofEpochSecond(0), 20.0);
    this.pipeInput("sensor-1-2", Instant.ofEpochSecond(0), 200.0);
    this.pipeInput("sensor-2-1-2", Instant.ofEpochSecond(1), 1.0);
    this.pipeInput("sensor-2-2-2", Instant.ofEpochSecond(1), 2.0);

    // Advance time to obtain outputs
    this.pipeInput("sensor-1-1", Instant.ofEpochSecond(2), 999.0);

    // Check aggregation results
    Assert.assertEquals(5, this.outputTopic.getQueueSize());

    final AggregatedActivePowerRecord rootResult = this.outputTopic.readValue();
    Assert.assertEquals("root", rootResult.getIdentifier());
    Assert.assertEquals(2, rootResult.getCount());
    Assert.assertEquals(333.0, rootResult.getSumInW(), 0.1);

    final AggregatedActivePowerRecord sensor1Result = this.outputTopic.readValue();
    Assert.assertEquals("sensor-1", sensor1Result.getIdentifier());
    Assert.assertEquals(2, sensor1Result.getCount());
    Assert.assertEquals(300.0, sensor1Result.getSumInW(), 0.1);

    final AggregatedActivePowerRecord sensor2Result = this.outputTopic.readValue();
    Assert.assertEquals("sensor-2", sensor2Result.getIdentifier());
    Assert.assertEquals(2, sensor2Result.getCount());
    Assert.assertEquals(33.0, sensor2Result.getSumInW(), 0.1);

    final AggregatedActivePowerRecord sensor21Result = this.outputTopic.readValue();
    Assert.assertEquals("sensor-2-1", sensor21Result.getIdentifier());
    Assert.assertEquals(2, sensor21Result.getCount());
    Assert.assertEquals(11.0, sensor21Result.getSumInW(), 0.1);

    final AggregatedActivePowerRecord sensor22Result = this.outputTopic.readValue();
    Assert.assertEquals("sensor-2-2", sensor22Result.getIdentifier());
    Assert.assertEquals(2, sensor22Result.getCount());
    Assert.assertEquals(22.0, sensor22Result.getSumInW(), 0.1);
  }



  private void pipeInput(final String identifier, final Instant timestamp, final double value) {
    this.inputTopic.pipeInput(
        identifier,
        new ActivePowerRecord(identifier, timestamp.toEpochMilli(), value),
        timestamp);
  }

}
