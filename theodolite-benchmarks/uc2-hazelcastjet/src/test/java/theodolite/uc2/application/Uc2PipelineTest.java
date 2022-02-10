package theodolite.uc2.application;

import com.google.gson.Gson;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.config.JetConfig;
import com.hazelcast.jet.core.JetTestSupport;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.StreamSource;
import com.hazelcast.jet.pipeline.StreamStage;
import com.hazelcast.jet.pipeline.test.AssertionCompletedException;
import com.hazelcast.jet.pipeline.test.Assertions;
import com.hazelcast.jet.pipeline.test.TestSources;
import com.hazelcast.jet.test.SerialTest;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletionException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import titan.ccp.model.records.ActivePowerRecord;

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
    int testItemsPerSecond = 1;
    String testSensorName = "TEST-SENSOR";
    Double testValueInW = 10.0;
    int testWindowInMs = 5000;

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
    Uc2PipelineBuilder pipelineBuilder = new Uc2PipelineBuilder();
    this.testPipeline = Pipeline.create();
    this.uc2Topology =
        pipelineBuilder.extendUc2Topology(this.testPipeline, testSource, testWindowInMs);

  }

  /**
   * Tests if no items reach the end before the first window ends.
   */
  @Test
  public void testOutput() {

    // Assertion Configuration
    int timeout = 14;
    String expectedOutput = "Stats{count=5, mean=10.0, populationStandardDeviation=0.0, min=10.0, max=10.0}";

    // Assertion
    this.uc2Topology.apply(Assertions.assertCollectedEventually(timeout,
        collection -> Assert.assertTrue(
            "Not the right amount items in Stats Object!",
            collection.get(collection.size()-1).getValue().equals(expectedOutput))));

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
