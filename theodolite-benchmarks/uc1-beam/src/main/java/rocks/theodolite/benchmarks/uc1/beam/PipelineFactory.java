package rocks.theodolite.benchmarks.uc1.beam;

import java.util.function.Function;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.Values;
import org.apache.commons.configuration2.Configuration;
import rocks.theodolite.benchmarks.commons.beam.AbstractPipelineFactory;
import rocks.theodolite.benchmarks.commons.beam.kafka.KafkaActivePowerTimestampReader;
import rocks.theodolite.benchmarks.uc1.beam.firestore.FirestoreOptionsExpander;
import rocks.theodolite.commons.model.records.ActivePowerRecord;

/**
 * {@link AbstractPipelineFactory} for UC1.
 */
public class PipelineFactory extends AbstractPipelineFactory {

  public static final String SINK_TYPE_KEY = "sink.type";

  private final SinkType sinkType = SinkType.from(this.config.getString(SINK_TYPE_KEY));

  public PipelineFactory(final Configuration configuration) {
    super(configuration);
  }

  @Override
  protected void expandOptions(final PipelineOptions options) {
    // No options to set
    // TODO Add for PubSub
    // final String pubSubEmulatorHost = super.config.getString(null);
    // if (pubSubEmulatorHost != null) {
    // final PubsubOptions pubSubOptions = options.as(PubsubOptions.class);
    // pubSubOptions.setPubsubRootUrl("http://" + pubSubEmulatorHost);
    // }
    if (this.sinkType == SinkType.FIRESTORE) {
      FirestoreOptionsExpander.expandOptions(options);
    }
  }

  @Override
  protected void constructPipeline(final Pipeline pipeline) {
    final KafkaActivePowerTimestampReader kafkaReader = super.buildKafkaReader();

    pipeline.apply(kafkaReader)
        .apply(Values.create())
        .apply(this.sinkType.create(this.config));
  }

  @Override
  protected void registerCoders(final CoderRegistry registry) {
    registry.registerCoderForClass(
        ActivePowerRecord.class,
        // AvroCoder.of(ActivePowerRecord.SCHEMA$));
        AvroCoder.of(ActivePowerRecord.class, false));
  }

  public static Function<Configuration, AbstractPipelineFactory> factory() {
    return config -> new PipelineFactory(config);
  }

}
