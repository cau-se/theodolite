package application;

import com.google.common.math.Stats;
import com.google.common.math.StatsAccumulator;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import java.util.HashMap;
import java.util.Map;
import org.apache.beam.runners.flink.FlinkRunner;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.coders.SerializableCoder;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.transforms.windowing.AfterProcessingTime;
import org.apache.beam.sdk.transforms.windowing.AfterWatermark;
import org.apache.beam.sdk.transforms.windowing.SlidingWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.joda.time.Duration;
import theodolite.commons.beam.AbstractBeamService;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * Implementation of the use case Aggregation based on Time Attributes using Apache Beam with the
 * Flink Runner. To run locally in standalone start Kafka, Zookeeper, the schema-registry and the
 * workload generator using the delayed_startup.sh script. And configure the Kafka, Zookeeper and
 * Schema Registry urls accordingly. Start a Flink cluster and pass its REST adress
 * using--flinkMaster as run parameter. To persist logs add
 * ${workspace_loc:/uc4-application-samza/eclipseConsoleLogs.log} as Output File under Standard
 * Input Output in Common in the Run Configuration Start via Eclipse Run.
 */
public final class Uc3BeamFlink extends AbstractBeamService {

  /**
   * Private constructor to avoid instantiation.
   */
  private Uc3BeamFlink(final String[] args) { //NOPMD
    super(args);
    this.options.setRunner(FlinkRunner.class);
  }

  /**
   * Start running this microservice.
   */
  public static void main(final String[] args) {

    Uc3BeamFlink uc3BeamFlink = new Uc3BeamFlink(args);

    Uc3BeamPipeline pipeline = new Uc3BeamPipeline(uc3BeamFlink.options, uc3BeamFlink.getConfig());

    pipeline.run().waitUntilFinish();
  }

}

