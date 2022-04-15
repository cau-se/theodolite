package rocks.theodolite.benchmarks.uc4.beam;

import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.KV;
import rocks.theodolite.commons.model.records.AggregatedActivePowerRecord;

/**
 * Sets the identifier for new {@link AggregatedActivePowerRecord}.
 */
public class SetIdForAggregated extends
    SimpleFunction<KV<String, AggregatedActivePowerRecord>, KV<String, AggregatedActivePowerRecord>> { // NOCS
  private static final long serialVersionUID = 2148522605294086982L;

  @Override
  public KV<String, AggregatedActivePowerRecord> apply(
      final KV<String, AggregatedActivePowerRecord> kv) {
    final AggregatedActivePowerRecord record = new AggregatedActivePowerRecord(
        kv.getKey(), kv.getValue().getTimestamp(), kv.getValue().getCount(),
        kv.getValue().getSumInW(), kv.getValue().getAverageInW());
    return KV.of(kv.getKey(), record);
  }
}
