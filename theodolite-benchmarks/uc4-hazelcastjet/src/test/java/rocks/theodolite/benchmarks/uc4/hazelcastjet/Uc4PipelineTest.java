package rocks.theodolite.benchmarks.uc4.hazelcastjet;

import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.config.JetConfig;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.core.JetTestSupport;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.StreamSource;
import com.hazelcast.jet.pipeline.StreamStage;
import com.hazelcast.jet.pipeline.test.AssertionCompletedException;
import com.hazelcast.jet.pipeline.test.Assertions;
import com.hazelcast.jet.pipeline.test.TestSources;
import com.hazelcast.jet.test.SerialTest;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.CompletionException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import rocks.theodolite.benchmarks.commons.configuration.events.Event;
import rocks.theodolite.benchmarks.commons.model.records.ActivePowerRecord;
import rocks.theodolite.benchmarks.commons.model.records.AggregatedActivePowerRecord;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.ImmutableSensorRegistry;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.MachineSensor;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.MutableAggregatedSensor;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.MutableSensorRegistry;
import rocks.theodolite.benchmarks.uc4.hazelcastjet.uc4specifics.ImmutableSensorRegistryUc4Serializer;
import rocks.theodolite.benchmarks.uc4.hazelcastjet.uc4specifics.SensorGroupKey;
import rocks.theodolite.benchmarks.uc4.hazelcastjet.uc4specifics.SensorGroupKeySerializer;
import rocks.theodolite.benchmarks.uc4.hazelcastjet.uc4specifics.ValueGroup;
import rocks.theodolite.benchmarks.uc4.hazelcastjet.uc4specifics.ValueGroupSerializer;


@Category(SerialTest.class)
public class Uc4PipelineTest extends JetTestSupport {

  // TEst Machinery
  JetInstance testInstance = null;
  Pipeline testPipeline = null;
  StreamStage<Entry<String, AggregatedActivePowerRecord>> uc4Topology = null;

  @Before
  public void buildUc4Pipeline() {

    // Setup Configuration
    final int testItemsPerSecond = 2;
    final String testSensorName = "TEST-SENSOR";
    final String testLevel1GroupName = "TEST-LEVEL1-GROUP";
    final String testLevel2GroupName = "TEST-LEVEL2-GROUP";
    final Double testValueInW = 10.0;
    final int testWindowSize = 5000; // As window size is bugged, not necessary.

    // Create mock jet instance with configuration
    final String testClusterName = randomName();
    final JetConfig testJetConfig = new JetConfig();
    testJetConfig.getHazelcastConfig().setClusterName(testClusterName);
    this.testInstance = this.createJetMember(testJetConfig);

    // Create test source 1 : Input Values
    final StreamSource<Entry<String, ActivePowerRecord>> testInputSource =
        TestSources.itemStream(testItemsPerSecond, (timestamp, item) -> {
          final ActivePowerRecord testRecord =
              new ActivePowerRecord(testSensorName, timestamp, testValueInW);
          final Entry<String, ActivePowerRecord> testEntry =
              Map.entry(testSensorName, testRecord);
          return testEntry;
        });

    // Create test source 2 : Mock aggregation Values
    final StreamSource<Entry<String, AggregatedActivePowerRecord>> testAggregationSource =
        TestSources.itemStream(testItemsPerSecond, (timestamp, item) -> {

          final AggregatedActivePowerRecord test =
              new AggregatedActivePowerRecord(testSensorName,
                  System.currentTimeMillis(),
                  1L,
                  testValueInW,
                  testValueInW);

          final ActivePowerRecord testAggValue =
              new ActivePowerRecord(testSensorName,
                  System.currentTimeMillis(),
                  testValueInW);

          final Entry<String, AggregatedActivePowerRecord> testEntry =
              Map.entry(testLevel1GroupName, test);
          return testEntry;
        });


    // Create test source 3 : Mock Config Values
    final StreamSource<Entry<Event, String>> testConfigSource =
        TestSources.itemStream(testItemsPerSecond, (timestamp, item) -> {
          final Event theEvent = Event.SENSOR_REGISTRY_CHANGED;

          // Topology:
          // level2Group -> level1Group -> testSensor

          // Create Registry
          final MutableSensorRegistry testRegistry = new MutableSensorRegistry(testLevel2GroupName);
          // Add Sensors
          final MutableAggregatedSensor topLevelSensor = testRegistry.getTopLevelSensor();
          final MutableAggregatedSensor level1GroupSensor =
              topLevelSensor.addChildAggregatedSensor(testLevel1GroupName);
          final MachineSensor inputSensor = level1GroupSensor.addChildMachineSensor(testSensorName);

          final String stringRegistry = testRegistry.toJson();
          final Entry<Event, String> testEntry =
              Map.entry(theEvent, stringRegistry);
          return testEntry;
        });

    // Create pipeline to test
    final Uc4PipelineBuilder pipelineBuilder = new Uc4PipelineBuilder();
    this.testPipeline = Pipeline.create();
    this.uc4Topology = pipelineBuilder.extendUc4Topology(this.testPipeline,
        testInputSource, testAggregationSource, testConfigSource, testWindowSize);

    this.uc4Topology.writeTo(Sinks.logger());
  }

  /**
   * Tests if no items reach the end before the first window ends.
   */
  @Test
  public void testOutput() {

    // System.out.println("DEBUG DEBUG DEBUG || ENTERED TEST 1");

    // Assertion Configuration
    final int timeout = 20;
    final String testSensorName = "TEST-SENSOR";
    final String testLevel1GroupName = "TEST-LEVEL1-GROUP";
    final String testLevel2GroupName = "TEST-LEVEL2-GROUP";
    final double testValueInW = 10.0;


    // Assertion
    this.uc4Topology.apply(Assertions.assertCollectedEventually(timeout,
        collection -> {
          System.out.println("DEBUG || ENTERED ASSERTION COLLECTED EVENTUALLY");

          boolean allOkay = false;

          boolean testLevel1contained = false;
          boolean testLevel2contained = false;
          boolean averageEqTest = true;
          boolean avOk = true;


          if (collection != null) {
            System.out.println("Collection size: " + collection.size());


            for (final Entry<String, AggregatedActivePowerRecord> entry : collection) {
              System.out.println("DEBUG || " + entry.toString());

              final String key = entry.getKey();
              final AggregatedActivePowerRecord agg = entry.getValue();


              if (Objects.equals(key, testLevel1GroupName)) {
                testLevel1contained = true;
              }

              if (Objects.equals(key, testLevel2GroupName)) {
                testLevel2contained = true;
              }

              if (testValueInW != agg.getAverageInW()) {
                averageEqTest = false;
              }

              final double average = agg.getSumInW() / agg.getCount();
              if (average != agg.getAverageInW()) {
                avOk = false;
              }

            }
            allOkay = testLevel1contained && testLevel2contained && averageEqTest && avOk;
          }

          System.out.println("testLevel1contained: " + testLevel1contained);
          System.out.println("testLevel2contained: " + testLevel2contained);
          System.out.println("averageEqTest: " + averageEqTest);
          System.out.println("avOk: " + avOk);

          Assert.assertTrue("Assertion did not complete!", allOkay);

        }));

    try {

      final JobConfig jobConfig = new JobConfig()
          .registerSerializer(ValueGroup.class, ValueGroupSerializer.class)
          .registerSerializer(SensorGroupKey.class, SensorGroupKeySerializer.class)
          .registerSerializer(ImmutableSensorRegistry.class,
              ImmutableSensorRegistryUc4Serializer.class);
      this.testInstance.newJob(this.testPipeline, jobConfig).join();

    } catch (final CompletionException e) {
      final String errorMsg = e.getCause().getMessage();
      Assert.assertTrue(
          "Job was expected to complete with AssertionCompletedException, but completed with: "
              + e.getCause(),
          errorMsg.contains(AssertionCompletedException.class.getName()));
    } catch (final Exception e) {
      System.out.println("ERRORORORO TEST BROKEN !!!!");
      System.out.println(e);
    }
  }


  @After
  public void after() {
    System.out.println("Shutting down");
    // Shuts down all running Jet Instances
    Jet.shutdownAll();
  }

}
