package rocks.theodolite.benchmarks.uc3.hazelcastjet;

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
import java.util.ArrayList;
import java.util.Map;
import java.util.TimeZone;
import java.util.Map.Entry;
import java.util.concurrent.CompletionException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import rocks.theodolite.benchmarks.uc3.hazelcastjet.Uc3PipelineBuilder;
import rocks.theodolite.benchmarks.uc3.hazelcastjet.uc3specifics.HourOfDayKey;
import rocks.theodolite.benchmarks.uc3.hazelcastjet.uc3specifics.HourOfDayKeySerializer;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * Test methods for the Hazelcast Jet Implementation of UC3.
 */
@Category(SerialTest.class)
public class Uc3PipelineTest extends JetTestSupport {

  // Test Machinery
  private JetInstance testInstance = null;
  private Pipeline testPipeline = null;
  private StreamStage<Entry<String, String>> uc3Topology = null;


  /**
   * Creates the JetInstance, defines a new Hazelcast Jet Pipeline and extends the UC3 topology.
   * Allows for quick extension of tests.
   */
  @Before
  public void buildUc3Pipeline() {

    // Setup Configuration
    int testItemsPerSecond = 1;
    String testSensorName = "TEST-SENSOR";
    Double testValueInW = 10.0;
    int testHopSizeInSec = 1;
    int testWindowSizeInSec = 50;
    // Used to check hourOfDay
    long mockTimestamp = 1632741651;


    // Create mock jet instance with configuration
    final String testClusterName = randomName();
    final JetConfig testJetConfig = new JetConfig();
    testJetConfig.getHazelcastConfig().setClusterName(testClusterName);
    this.testInstance = this.createJetMember(testJetConfig);

    // Create a test source
    final StreamSource<Entry<String, ActivePowerRecord>> testSource =
        TestSources.itemStream(testItemsPerSecond, (timestamp, item) -> {
          final ActivePowerRecord testRecord =
              new ActivePowerRecord(testSensorName, mockTimestamp, testValueInW);
          final Entry<String, ActivePowerRecord> testEntry =
              Map.entry(testSensorName, testRecord);
          return testEntry;
        });

    // Create pipeline to test
    Uc3PipelineBuilder pipelineBuilder = new Uc3PipelineBuilder();
    this.testPipeline = Pipeline.create();
    this.uc3Topology = pipelineBuilder.extendUc3Topology(testPipeline, testSource,
        testHopSizeInSec, testWindowSizeInSec);
  }

  /**
   * Tests if no items reach the end before the first window ends.
   */
  @Test
  public void testOutput() {

    // Assertion Configuration
    int timeout = 10;
    String testSensorName = "TEST-SENSOR";
    Double testValueInW = 10.0;
    // Used to check hourOfDay
    long mockTimestamp = 1632741651;

    // Assertion
    this.uc3Topology.apply(Assertions.assertCollectedEventually(timeout,
        collection -> {

          // DEBUG
          System.out.println("DEBUG: CHECK 1 || Entered Assertion of testOutput()");

          // Check all collected Items
          boolean allOkay = true;
          if (collection != null) {
            System.out.println("DEBUG: CHECK 2 || Collection Size: " + collection.size());
            for (int i = 0; i < collection.size(); i++) {

              // Build hour of day
              long timestamp = mockTimestamp;
              int expectedHour = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
                  TimeZone.getDefault().toZoneId()).getHour();

              // Compare expected output with generated output
              Entry<String, String> currentEntry = collection.get(i);
              String expectedKey = testSensorName + ";" + expectedHour;
              String expectedValue = testValueInW.toString();

              // DEBUG
              System.out.println(
                  "DEBUG: CHECK 3 || Expected Output: '" + expectedKey + "=" + expectedValue
                      + "' - Actual Output: '" + currentEntry.getKey() + "="
                      + currentEntry.getValue().toString() + "'");

              if (!(currentEntry.getKey().equals(expectedKey)
                  && currentEntry.getValue().toString().equals(expectedValue))) {
                System.out.println("DEBUG: CHECK 5 || Failed assertion!");
                allOkay = false;
              }
            }
          }

          // Assertion
          Assert.assertTrue(
              "Items do not match expected structure!", allOkay);
        }));

    // Run the test!
    try {
      final JobConfig jobConfig = new JobConfig()
          .registerSerializer(HourOfDayKey.class, HourOfDayKeySerializer.class);
      this.testInstance.newJob(this.testPipeline, jobConfig).join();
      Assert.fail("Job should have completed with an AssertionCompletedException, "
          + "but completed normally!");
    } catch (final CompletionException e) {
      final String errorMsg = e.getCause().getMessage();
      Assert.assertTrue(
          "Job was expected to complete with AssertionCompletedException, but completed with: "
              + e.getCause(),
          errorMsg.contains(AssertionCompletedException.class.getName()));
    }

  }

  @After
  public void after() {
    // Shuts down all running Jet Instances
    Jet.shutdownAll();
  }

}
