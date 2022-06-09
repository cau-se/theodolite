package rocks.theodolite.benchmarks.uc1.beam;

import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PCollection;
import org.apache.commons.configuration2.Configuration;
import rocks.theodolite.benchmarks.commons.model.records.ActivePowerRecord;

/**
 * Interface for a class that creates sinks (i.e., {@link PTransform}s that map and store
 * {@link ActivePowerRecord}s, optionally, using a {@link Configuration}.
 */
public interface SinkFactory {

  PTransform<PCollection<ActivePowerRecord>, PCollection<?>> create(Configuration configuration);

}
