package rocks.theodolite.benchmarks.commons.beam;

import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import java.util.HashMap;
import java.util.Map;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.commons.configuration2.Configuration;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import rocks.theodolite.benchmarks.commons.beam.kafka.KafkaActivePowerTimestampReader;

/**
 * Abstract factory class for creating Beam pipelines from a {@link Configuration} and
 * {@link PipelineOptions}. Implementations may expand the {@link PipelineOptions}, construct a
 * {@link Pipeline} and register coders.
 */
public abstract class AbstractPipelineFactory {

  protected final Configuration config;

  public AbstractPipelineFactory(final Configuration configuration) {
    this.config = configuration;
  }

  /**
   * Create a Pipeline with the configured {@link PipelineOptions}.
   */
  public final Pipeline create(final PipelineOptions options) {
    this.expandOptions(options);
    final Pipeline pipeline = Pipeline.create(options);
    this.registerCoders(pipeline.getCoderRegistry());
    this.constructPipeline(pipeline);
    return pipeline;
  }

  protected abstract void expandOptions(final PipelineOptions options);

  protected abstract void constructPipeline(Pipeline pipeline);

  protected abstract void registerCoders(CoderRegistry registry);

  protected KafkaActivePowerTimestampReader buildKafkaReader() {
    final String inputTopic = this.config.getString(ConfigurationKeys.KAFKA_INPUT_TOPIC);
    final String bootstrapServer = this.config.getString(ConfigurationKeys.KAFKA_BOOTSTRAP_SERVERS);

    final Map<String, Object> consumerConfig = new HashMap<>();
    consumerConfig.put(
        ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
        this.config.getString(ConfigurationKeys.ENABLE_AUTO_COMMIT));
    consumerConfig.put(
        ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
        this.config.getString(ConfigurationKeys.AUTO_OFFSET_RESET));
    consumerConfig.put(
        AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
        this.config.getString(ConfigurationKeys.SCHEMA_REGISTRY_URL));
    consumerConfig.put(
        KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG,
        this.config.getString(ConfigurationKeys.SPECIFIC_AVRO_READER));
    consumerConfig.put(
        ConsumerConfig.GROUP_ID_CONFIG,
        this.config.getString(ConfigurationKeys.APPLICATION_NAME));

    return new KafkaActivePowerTimestampReader(
        bootstrapServer,
        inputTopic,
        consumerConfig);
  }

}
