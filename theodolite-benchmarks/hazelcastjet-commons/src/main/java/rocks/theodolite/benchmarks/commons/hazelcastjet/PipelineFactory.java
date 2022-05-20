package rocks.theodolite.benchmarks.commons.hazelcastjet;

import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.StreamStage;
import java.util.Properties;

public abstract class PipelineFactory {

  final Pipeline pipe;

  public PipelineFactory() {
    this.pipe = Pipeline.create();

  }

  public abstract Pipeline buildPipeline();

  public abstract StreamStage extendTopology();

}
