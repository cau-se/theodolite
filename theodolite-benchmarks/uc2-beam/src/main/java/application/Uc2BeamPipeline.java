package application;

import com.google.common.math.Stats;
import com.google.common.math.StatsAccumulator;
import java.util.Map;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.coders.SerializableCoder;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.commons.configuration2.Configuration;
import org.apache.kafka.common.serialization.StringSerializer;
import org.joda.time.Duration;
import theodolite.commons.beam.AbstractPipeline;
import theodolite.commons.beam.ConfigurationKeys;
import theodolite.commons.beam.kafka.KafkaActivePowerTimestampReader;
import theodolite.commons.beam.kafka.KafkaWriterTransformation;
import titan.ccp.model.records.ActivePowerRecord;


/**
 * Implementation of the use case Database Storage using Apache Beam with the Flink Runner. To
 * execute locally in standalone start Kafka, Zookeeper, the schema-registry and the workload
 * generator using the delayed_startup.sh script. Start a Flink cluster and pass its REST adress
 * using--flinkMaster as run parameter. To persist logs add
 * ${workspace_loc:/uc1-application-samza/eclipseConsoleLogs.log} as Output File under Standard
 * Input Output in Common in the Run Configuration Start via Eclipse Run.
 */
public final class Uc2BeamPipeline extends AbstractPipeline {

  protected Uc2BeamPipeline(final PipelineOptions options, final Configuration config) {
    super(options, config);
    // Additional needed variables
    final String outputTopic = config.getString(ConfigurationKeys.KAFKA_OUTPUT_TOPIC);

    final int windowDurationMinutes = Integer.parseInt(
        config.getString(ConfigurationKeys.KAFKA_WINDOW_DURATION_MINUTES));
    final Duration duration = Duration.standardMinutes(windowDurationMinutes);

    // Build kafka configuration
    final Map<String, Object> consumerConfig = buildConsumerConfig();

    // Set Coders for Classes that will be distributed
    final CoderRegistry cr = getCoderRegistry();
    cr.registerCoderForClass(ActivePowerRecord.class, AvroCoder.of(ActivePowerRecord.SCHEMA$));
    cr.registerCoderForClass(StatsAggregation.class, SerializableCoder.of(StatsAggregation.class));
    cr.registerCoderForClass(StatsAccumulator.class, AvroCoder.of(StatsAccumulator.class));

    // Read from Kafka
    final KafkaActivePowerTimestampReader kafkaActivePowerRecordReader =
        new KafkaActivePowerTimestampReader(bootstrapServer, inputTopic, consumerConfig);

    // Transform into String
    final StatsToString statsToString = new StatsToString();

    // Write to Kafka
    final KafkaWriterTransformation<String> kafkaWriter =
        new KafkaWriterTransformation<>(bootstrapServer, outputTopic, StringSerializer.class);

    // Apply pipeline transformations
    this.apply(kafkaActivePowerRecordReader)
        // Apply a fixed window
        .apply(Window.<KV<String, ActivePowerRecord>>into(FixedWindows.of(duration)))
        // Aggregate per window for every key
        .apply(Combine.<String, ActivePowerRecord, Stats>perKey(new StatsAggregation()))
        .setCoder(KvCoder.of(StringUtf8Coder.of(), SerializableCoder.of(Stats.class)))
        // Map into correct output format
        .apply(MapElements.via(statsToString))
        // Write to Kafka
        .apply(kafkaWriter);
  }
}

