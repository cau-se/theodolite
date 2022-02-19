package application;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.CoderRegistry;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.commons.configuration2.Configuration;
import titan.ccp.model.records.ActivePowerRecord;

public abstract class AbstractPipelineFactory {

  protected final Configuration configuration;

  public AbstractPipelineFactory(final Configuration configuration) {
    this.configuration = configuration;
  }

  public final Pipeline create(final PipelineOptions options) {
    final Pipeline pipeline = Pipeline.create(options);
    this.constructPipeline(pipeline);
    this.registerCoders(pipeline.getCoderRegistry());
    return pipeline;
  }

  private void constructPipeline(Pipeline pipeline) {
    // pipeline.apply(kafka)
    // .apply(Values.create())
    // .apply(sinkType.create(config));
  }

  private void registerCoders(CoderRegistry registry) {
    registry.registerCoderForClass(
        ActivePowerRecord.class,
        AvroCoder.of(ActivePowerRecord.SCHEMA$));
  }

}
