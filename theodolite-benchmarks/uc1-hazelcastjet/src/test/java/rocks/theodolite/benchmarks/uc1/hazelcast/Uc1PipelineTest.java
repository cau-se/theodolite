package rocks.theodolite.benchmarks.uc1.hazelcast;

import com.hazelcast.jet.Jet;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.config.JetConfig;
import com.hazelcast.jet.core.JetTestSupport;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sink;
import com.hazelcast.jet.pipeline.StreamSource;
import com.hazelcast.jet.pipeline.StreamStage;
import com.hazelcast.jet.pipeline.test.AssertionCompletedException;
import com.hazelcast.jet.pipeline.test.Assertions;
import com.hazelcast.jet.pipeline.test.TestSources;
import com.hazelcast.jet.test.SerialTest;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.CompletionException;
import com.hazelcast.logging.ILogger;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.theodolite.benchmarks.uc1.commons.DatabaseAdapter;
import rocks.theodolite.benchmarks.uc1.commons.DatabaseWriter;
import rocks.theodolite.benchmarks.uc1.commons.logger.LogWriterFactory;
import rocks.theodolite.benchmarks.uc1.hazelcastjet.Uc1PipelineFactory;
import titan.ccp.model.records.ActivePowerRecord;

import static com.hazelcast.jet.pipeline.SinkBuilder.sinkBuilder;
import static com.hazelcast.logging.Logger.getLogger;

/**
 * Test methods for the Hazelcast Jet Implementation of UC1.
 */
@Category(SerialTest.class)
public class Uc1PipelineTest extends JetTestSupport {

  private JetInstance testInstance = null;
  private Pipeline testPipeline = null;
  private StreamStage<String> uc1Topology = null;

  // Standard Logger
  private static final Logger LOGGER = LoggerFactory.getLogger(Uc1PipelineTest.class);
  // HazelcastJet Logger
  private static final ILogger logger = getLogger(Uc1PipelineTest.class);

  private final DatabaseAdapter<String> databaseAdapter = LogWriterFactory.forJson();

  /**
   * Creates the JetInstance, defines a new Hazelcast Jet Pipeline and extends the UC1 topology.
   * Allows for quick extension of tests.
   */
  @Before
  public void buildUc1Pipeline() {

    this.logger.info("Hazelcast Logger");
    LOGGER.info("Standard Logger");


    // Setup Configuration
    final int testItemsPerSecond = 1;
    final String testSensorName = "TEST_SENSOR";
    final Double testValueInW = 10.0;

    // Create mock jet instance with configuration
    final String testClusterName = randomName();
    final JetConfig testJetConfig = new JetConfig();
//    testJetConfig.setProperty( "hazelcast.logging.type", "slf4j" );
    testJetConfig.getHazelcastConfig().setClusterName(testClusterName);
    this.testInstance = createJetMember(testJetConfig);


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
    final Uc1PipelineFactory factory = new Uc1PipelineFactory(properties,"");
    uc1Topology = factory.extendUc1Topology(testSource);
    this.testPipeline = factory.getPipe();

    // Create DatabaseWriter sink
    final DatabaseWriter<String> adapter = this.databaseAdapter.getDatabaseWriter();
    final Sink<String> sink = sinkBuilder(
        "database-sink", x -> adapter)
        .<String>receiveFn(DatabaseWriter::write)
        .build();

//    Map Stage, can be used instead of sink
//    StreamStage<String> log = uc1Topology.map(s -> {
//        LOGGER.info(s);
//        return s;
//    });
//    log.writeTo(sink);

    //apply sink
    this.uc1Topology.writeTo(sink);
  }

  /**
   * UC1 Pipeline test to check if items are passed through at an acceptable rate.
   */
  @Test
  public void test1Uc1PipelineElements() {

    // Assertion Configuration
    final int assertTimeoutSeconds = 6;
    final int assertCollectedItems = 5;

    LOGGER.info("Pipeline build successfully, starting test");

    // Assertion
    this.uc1Topology.apply(Assertions.assertCollectedEventually(assertTimeoutSeconds,
        collection -> {
      //print the newest Record
//      LOGGER.info(collection.get(collection.size()-1));

          // Run pipeline until 5th item
          Assert.assertTrue("Not enough data arrived in the end",
              collection.size() >= assertCollectedItems);
        }));

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
