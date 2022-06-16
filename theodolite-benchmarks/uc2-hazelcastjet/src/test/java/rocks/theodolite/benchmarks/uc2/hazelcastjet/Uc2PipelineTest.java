package rocks.theodolite.benchmarks.uc2.hazelcastjet;

import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.config.JetConfig;
import com.hazelcast.jet.core.JetTestSupport;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
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
import rocks.theodolite.benchmarks.commons.model.records.ActivePowerRecord;

/**
 * Test methods for the Hazelcast Jet Implementation of UC2.
 */
@Category(SerialTest.class)
public class Uc2PipelineTest extends JetTestSupport {

  JetInstance testInstance = null;
  Pipeline testPipeline = null;
  StreamStage<Entry<String, String>> uc2Topology = null;

  /*
   * Creates the JetInstance, defines a new Hazelcast Jet Pipeline and extends the UC2 topology.
   * Allows for quick extension of tests.
   */
  @Before
  public void buildUc2Pipeline() {

    // Setup Configuration
    final int testItemsPerSecond = 1;
    final String testSensorName = "TEST-SENSOR";
    final Double testValueInW = 10.0;
    final Duration testWindow = Duration.ofSeconds(5);

    // Create mock jet instance with configuration
    final String testClusterName = randomName();
    final JetConfig testJetConfig = new JetConfig();
    testJetConfig.getHazelcastConfig().setClusterName(testClusterName);
    this.testInstance = this.createJetMember(testJetConfig);

    // Create a test source
    final StreamSource<Entry<String, ActivePowerRecord>> testSource =
        TestSources.itemStream(testItemsPerSecond, (timestamp, item) -> {
          final ActivePowerRecord testRecord =
              new ActivePowerRecord(testSensorName, timestamp, testValueInW);
          final Entry<String, ActivePowerRecord> testEntry =
              Map.entry(testSensorName, testRecord);
          return testEntry;
        });

    // Create pipeline to test
    final Properties properties = new Properties();
    final Uc2PipelineFactory factory = new Uc2PipelineFactory(
        properties,"",properties,"", testWindow);

    this.uc2Topology = factory.extendUc2Topology(testSource);
    this.testPipeline = factory.getPipe();
  }

  /**
   * Tests if no items reach the end before the first window ends.
   */
  @Test
  public void testOutput() {

    // Assertion Configuration
    final int timeout = 14;
    final String expectedOutput =
        "Stats{count=5, mean=10.0, populationStandardDeviation=0.0, min=10.0, max=10.0}";

    // Assertion
    this.uc2Topology.apply(Assertions.assertCollectedEventually(timeout,
        collection -> Assert.assertEquals("Not the right amount items in Stats Object!",
            expectedOutput, collection.get(collection.size() - 1).getValue())));

    // Run the test!
    try {
      this.testInstance.newJob(this.testPipeline).join();
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
