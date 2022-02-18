package application;

import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.values.PCollection;
import org.apache.commons.configuration2.Configuration;
import titan.ccp.model.records.ActivePowerRecord;

public interface SinkFactory {

  PTransform<PCollection<ActivePowerRecord>, PCollection<?>> create(Configuration configuration);

}