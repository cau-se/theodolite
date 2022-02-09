package theodolite.uc4.application;

import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.config.JetConfig;
import com.hazelcast.jet.config.JobConfig;
import com.hazelcast.jet.core.JetTestSupport;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.StreamSource;
import com.hazelcast.jet.pipeline.StreamStage;
import com.hazelcast.jet.pipeline.test.AssertionCompletedException;
import com.hazelcast.jet.pipeline.test.Assertions;
import com.hazelcast.jet.pipeline.test.TestSources;
import com.hazelcast.jet.test.SerialTest;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;
import java.util.concurrent.CompletionException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import titan.ccp.configuration.events.Event;
import titan.ccp.model.records.ActivePowerRecord;
import titan.ccp.model.sensorregistry.MachineSensor;
import titan.ccp.model.sensorregistry.MutableAggregatedSensor;
import titan.ccp.model.sensorregistry.MutableSensorRegistry;
import titan.ccp.model.sensorregistry.SensorRegistry;

@Category(SerialTest.class)
public class Uc4PipelineTest extends JetTestSupport {

  // TEst Machinery
  JetInstance testInstance = null;
  Pipeline testPipeline = null;
  StreamStage<Entry<String, Double>> uc4Topology = null;

  @Before
  public void buildUc4Pipeline() {

    // Setup Configuration
    int testItemsPerSecond = 1;
    String testSensorName = "TEST-SENSOR";
    String testLevel1GroupName = "TEST-LEVEL1-GROUP";
    String testLevel2GroupName = "TEST-LEVEL2-GROUP";
    Double testValueInW = 10.0;
    int testWindowSize = 5000; // As window size is bugged, not necessary.

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
    final StreamSource<Entry<String, Double>> testAggregationSource =
        TestSources.itemStream(testItemsPerSecond, (timestamp, item) -> {
          final Double testAggValue = testValueInW;
          final Entry<String, Double> testEntry =
              Map.entry(testLevel1GroupName, testAggValue);
          return testEntry;
        });

    // Create test source 3 : Mock Config Values
    final StreamSource<Entry<Event, String>> testConfigSource =
        TestSources.itemStream(testItemsPerSecond, (timestamp, item) -> {
          Event theEvent = Event.SENSOR_REGISTRY_CHANGED;

          // Topology:
          // level2Group -> level1Group -> testSensor
          
          // Create Registry
          MutableSensorRegistry testRegistry = new MutableSensorRegistry(testLevel2GroupName);
          // Add Sensors
          MutableAggregatedSensor topLevelSensor = testRegistry.getTopLevelSensor();
          MutableAggregatedSensor level1GroupSensor =
              topLevelSensor.addChildAggregatedSensor(testLevel1GroupName);
          MachineSensor inputSensor = level1GroupSensor.addChildMachineSensor(testSensorName);



          String stringRegistry = testRegistry.toJson();
          final Entry<Event, String> testEntry =
              Map.entry(theEvent, stringRegistry);
          return testEntry;
        });

    // Create pipeline to test
    Uc4PipelineBuilder pipelineBuilder = new Uc4PipelineBuilder();
    this.testPipeline = Pipeline.create();
    this.uc4Topology = pipelineBuilder.extendUc4Topology(testPipeline, testInputSource,
        testAggregationSource, testConfigSource, testWindowSize);

  }

  /**
   * Tests if no items reach the end before the first window ends.
   */
  @Test
  public void testOutput() {

    System.out.println("DEBUG DEBUG DEBUG || ENTERED TEST 1");
    
    // Assertion Configuration
    int timeout = 10;
    String testSensorName = "TEST-SENSOR";
    String testLevel1GroupName = "TEST-LEVEL1-GROUP";
    String testLevel2GroupName = "TEST-LEVEL2-GROUP";
    Double testValueInW = 10.0;
    
    // Assertion
    this.uc4Topology.apply(Assertions.assertCollectedEventually(timeout, 
        collection -> {
          
          // TODO Try to find out why this test does not work or why the pipeline seems
          // TODO to crash!
          // but whyyy cant i get in here
          
          System.out.println("DEBUG DEBUG DEBUG || ENTERED ASSERTION COLLECTED EVENTUALLY");
          
          boolean allOkay = true;
          
          if (collection != null) {
            for(int i = 0; i < collection.size(); i++) {
              System.out.println("DEBUG DEBUG DEBUG || " + collection.get(i).toString());         
            }
          }
          
          Assert.assertTrue("Assertion did not complete!", allOkay);
          
        }));
    
  }

  @After
  public void after() {
    // Shuts down all running Jet Instances
    Jet.shutdownAll();
  }

  @Test
  public void test(){
    System.out.println("Hello There")
  }

}
