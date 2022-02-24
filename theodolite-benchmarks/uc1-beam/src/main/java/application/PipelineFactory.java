package application;

import java.util.function.Function;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.transforms.Values;
import org.apache.commons.configuration2.Configuration;
import theodolite.commons.beam.AbstractPipelineFactory;
import theodolite.commons.beam.kafka.KafkaActivePowerTimestampReader;
import titan.ccp.model.records.ActivePowerRecord;

public class PipelineFactory extends AbstractPipelineFactory {

  public static final String SINK_TYPE_KEY = "sink.type";

  public PipelineFactory(final Configuration configuration) {
    super(configuration);
  }

  @Override
  protected void expandOptions(final PipelineOptions options) {
    // TODO Add for PubSub
    // final String pubSubEmulatorHost = super.config.getString(null);
    // if (pubSubEmulatorHost != null) {
    // final PubsubOptions pubSubOptions = options.as(PubsubOptions.class);
    // pubSubOptions.setPubsubRootUrl("http://" + pubSubEmulatorHost);
    // }
  }

  @Override
  protected void constructPipeline(final Pipeline pipeline) {
    final SinkType sinkType = SinkType.from(this.config.getString(SINK_TYPE_KEY));

    final KafkaActivePowerTimestampReader kafkaReader = super.buildKafkaReader();

    pipeline.apply(kafkaReader)
        .apply(Values.create())
        .apply(sinkType.create(this.config));
  }

  @Override
  protected void registerCoders(final CoderRegistry registry) {
    registry.registerCoderForClass(
        ActivePowerRecord.class,
        AvroCoder.of(ActivePowerRecord.SCHEMA$));
  }

  public static Function<Configuration, AbstractPipelineFactory> factory() {
    return config -> new PipelineFactory(config);
  }

}
