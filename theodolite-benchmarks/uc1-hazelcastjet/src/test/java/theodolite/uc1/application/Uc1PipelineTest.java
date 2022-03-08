package theodolite.uc1.application;

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
import rocks.theodolite.benchmarks.uc1.hazelcastjet.Uc1PipelineBuilder;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * Test methods for the Hazelcast Jet Implementation of UC1.
 */
@Category(SerialTest.class)
public class Uc1PipelineTest extends JetTestSupport {

  private JetInstance testInstance = null;
  private Pipeline testPipeline = null;
  private StreamStage<String> uc1Topology = null;

  /**
   * Creates the JetInstance, defines a new Hazelcast Jet Pipeline and extends the UC2 topology.
   * Allows for quick extension of tests.
   */
  @Before
  public void buildUc1Pipeline() {

    // Setup Configuration
    final int testItemsPerSecond = 1;
    final String testSensorName = "TEST_SENSOR";
    final Double testValueInW = 10.0;

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
    final Uc1PipelineBuilder pipelineBuilder = new Uc1PipelineBuilder();
    this.testPipeline = Pipeline.create();
    this.uc1Topology =
        pipelineBuilder.extendUc1Topology(this.testPipeline, testSource);

  }

  /**
   * UC1 Pipeline test to check if items are passed through at an acceptable rate.
   */
  @Test
  public void test1Uc1PipelineElements() {

    // Assertion Configuration
    final int assertTimeoutSeconds = 6;
    final int assertCollectedItems = 5;

    // Assertion
    this.uc1Topology.apply(Assertions.assertCollectedEventually(assertTimeoutSeconds,
        collection -> Assert.assertTrue("Not enough data arrived in the end",
            collection.size() >= assertCollectedItems)));

    // Test the UC1 Pipeline Recreation
    try {
      this.testInstance.newJob(this.testPipeline).join();
      Assert.fail("Job should have completed with an AssertionCompletedException, "
          + "but completed normally");
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
