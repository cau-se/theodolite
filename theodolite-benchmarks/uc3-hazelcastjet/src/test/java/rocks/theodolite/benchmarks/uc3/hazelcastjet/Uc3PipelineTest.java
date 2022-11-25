package rocks.theodolite.benchmarks.uc3.hazelcastjet;

import com.google.common.math.Stats;
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
import java.time.Duration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.CompletionException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.theodolite.benchmarks.commons.model.records.ActivePowerRecord;

/**
 * Test methods for the Hazelcast Jet Implementation of UC3.
 */
@Category(SerialTest.class)
public class Uc3PipelineTest extends JetTestSupport {

  private static final Logger LOGGER = LoggerFactory.getLogger(Uc3PipelineTest.class);

  // Setup Configuration
  private static final int TEST_ITEMS_PER_SECOND = 1;
  private static final String TEST_SENSOR_NAME = "TEST-SENSOR";
  private static final Double TEST_VALUE_IN_W = 10.0;
  private static final Duration TEST_WINDOW_SLIDE = Duration.ofSeconds(1);
  private static final Duration TEST_WINDOW_SIZE = Duration.ofSeconds(50);
  private static final Duration TEST_EMIT_PERIOD = Duration.ofSeconds(0); // Do not emit early
                                                                          // results
  // Used to check hourOfDay
  private static final long MOCK_TIMESTAMP = 1632741651;


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

    // Create mock jet instance with configuration
    final String testClusterName = randomName();
    final JetConfig testJetConfig = new JetConfig();
    testJetConfig.getHazelcastConfig().setClusterName(testClusterName);
    this.testInstance = this.createJetMember(testJetConfig);

    // Create a test source
    final StreamSource<Entry<String, ActivePowerRecord>> testSource =
        TestSources.itemStream(TEST_ITEMS_PER_SECOND, (timestamp, item) -> {
          final ActivePowerRecord testRecord =
              new ActivePowerRecord(TEST_SENSOR_NAME, MOCK_TIMESTAMP, TEST_VALUE_IN_W);
          final Entry<String, ActivePowerRecord> testEntry =
              Map.entry(TEST_SENSOR_NAME, testRecord);
          return testEntry;
        });

    // Create pipeline to test
    final Properties properties = new Properties();
    final Uc3PipelineFactory factory = new Uc3PipelineFactory(
        properties, "", properties, "", TEST_WINDOW_SIZE,
        TEST_WINDOW_SLIDE,
        TEST_EMIT_PERIOD);

    this.uc3Topology = factory.extendUc3Topology(testSource);

    this.testPipeline = factory.getPipe();
  }

  /**
   * Tests if no items reach the end before the first window ends.
   */
  @Test
  public void testOutput() {

    // Assertion Configuration
    final int timeout = 10;
    // final String testSensorName = "TEST-SENSOR";
    // final double testValueInW = 10.0;

    // Assertion
    this.uc3Topology.apply(Assertions.assertCollectedEventually(timeout,
        collection -> {

          // DEBUG
          LOGGER.info("CHECK 1 || Entered Assertion of testOutput()");

          // Check all collected Items
          boolean allOkay = true;
          if (collection != null) {
            LOGGER.info("CHECK 2 || Collection Size: " + collection.size());
            for (final Entry<String, String> entry : collection) {
              // Compare expected output with generated output
              final String expectedKey = TEST_SENSOR_NAME;
              final String expectedValue = Stats.of(TEST_VALUE_IN_W).toString();

              // DEBUG
              LOGGER.info(
                  "CHECK 3 || Expected Output: '" + expectedKey + "=" + expectedValue
                      + "' - Actual Output: '" + entry.getKey() + "="
                      + entry.getValue() + "'");

              if (!(entry.getKey().equals(expectedKey) && entry.getValue().equals(expectedValue))) {
                LOGGER.info("CHECK 5 || Failed assertion!");
                allOkay = false;
              }
            }
          }

          // Assertion
          Assert.assertTrue("Items do not match expected structure!", allOkay);
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
