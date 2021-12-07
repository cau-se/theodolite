package theodolite.uc1.application;

import com.google.gson.Gson;
import com.hazelcast.jet.kafka.KafkaSources;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sink;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.StreamSource;
import java.util.Map.Entry;
import java.util.Properties;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * Builder to build a HazelcastJet Pipeline for UC1 which can be used for stream processing using
 * Hazelcast Jet.
 */
public class Uc1PipelineBuilder {

  private static final Gson GSON = new Gson();

  /**
   * Builds a pipeline which can be used for stream processing using Hazelcast Jet.
   *
   * @param inputSource A hazelcast jet stream-source for Entry<String,ActivePowerRecord> input
   *        values.
   * @return A hazelcast jet pipeline which processes data for Uc1.
   */
  public Pipeline build(final StreamSource<Entry<String, ActivePowerRecord>> inputSource,
      final Sink<String> outputSink) {
    // Build Pipeline
    final Pipeline pipe = Pipeline.create();
    pipe.readFrom(inputSource)
        .withNativeTimestamps(0)
        .setLocalParallelism(1)
        .setName("Log content")
        .map(record -> {
          return GSON.toJson(record);
        })
        .writeTo(outputSink);

    return pipe;
  }

  /**
   * Builds a pipeline which can be used for stream processing using Hazelcast Jet.
   *
   * @param kafkaPropertiesForPipeline Properties Object containing the necessary kafka attributes.
   * @param kafkaInputTopic The name of the input topic used for the pipeline.
   * @return A hazelcast jet pipeline which processes data for Uc1.
   */
  public Pipeline build(final Properties kafkaPropertiesForPipeline, final String kafkaInputTopic) {

    final StreamSource<Entry<String, ActivePowerRecord>> kafkaInputSource =
        KafkaSources.<String, ActivePowerRecord>kafka(kafkaPropertiesForPipeline, kafkaInputTopic);

    final Sink<String> loggerSink = Sinks.logger();
    return this.build(kafkaInputSource, loggerSink);

  }

}
