package theodolite.uc1.application;

import com.google.gson.Gson;
import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.config.JetConfig;
import com.hazelcast.jet.core.JetTestSupport;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.StreamSource;
import com.hazelcast.jet.pipeline.test.AssertionCompletedException;
import com.hazelcast.jet.pipeline.test.Assertions;
import com.hazelcast.jet.pipeline.test.TestSources;
import com.hazelcast.jet.test.SerialTest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletionException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * Test methods for the Hazelcast Jet Implementation of UC1.
 */
@Category(SerialTest.class)
public class Uc1PipelineTest extends JetTestSupport {

  private static final Gson GSON = new Gson();

  /**
   * UC1 Pipeline test to check if items are passed through at an acceptable rate.
   */
  @Test
  public void test1Uc1PipelineElements() {

    // Test Configuration
    final int testItemsPerSecond = 1;
    final String testSensorName = "id_test1";
    final Double testValueInW = 10.0;
    // Assertion Configuration
    final int assertTimeoutSeconds = 6;
    final int assertCollectedItems = 5;

    // Create mock jet instance with configuration
    final String testClusterName = randomName();
    final JetConfig testJetConfig = new JetConfig();
    testJetConfig.getHazelcastConfig().setClusterName(testClusterName);
    final JetInstance testInstance = this.createJetMember(testJetConfig);

    // Test Pipeline definition
    final List<String> sourceRecord =
        new ArrayList<>();
    final StreamSource<Entry<String, ActivePowerRecord>> testSource =
        TestSources.itemStream(testItemsPerSecond, (timestamp, item) -> {
          final ActivePowerRecord testRecord =
              new ActivePowerRecord(testSensorName, timestamp, testValueInW);
          final Entry<String, ActivePowerRecord> testEntry =
              Map.entry(testSensorName, testRecord);
          sourceRecord.add(GSON.toJson(testEntry));
          return testEntry;
        });

    // Recreation of the UC1 Pipeline - adjusted for a Hazelcast Jet Pipeline Test
    final Pipeline testPipeline = Pipeline.create();
    testPipeline
        .readFrom(testSource)
        .withNativeTimestamps(0)
        .setLocalParallelism(1)
        .setName("Log content")
        .map(data -> {
          return new Gson().toJson(data);
        })
        .apply(Assertions.assertCollectedEventually(assertTimeoutSeconds,
            collection -> Assert.assertTrue("Not enough data arrived in the end",
                collection.size() >= assertCollectedItems)));

    // Test the UC1 Pipeline Recreation
    try {
      testInstance.newJob(testPipeline).join();
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
