package rocks.theodolite.benchmarks.uc4.beam;

import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.KV;
import rocks.theodolite.benchmarks.commons.model.records.ActivePowerRecord;
import rocks.theodolite.benchmarks.commons.model.records.AggregatedActivePowerRecord;

/**
 * Converts AggregatedActivePowerRecord to ActivePowerRecord.
 */
public class AggregatedToActive
    extends SimpleFunction<KV<String, AggregatedActivePowerRecord>, KV<String, ActivePowerRecord>> {

  private static final long serialVersionUID = -8275252527964065889L;

  @Override
  public KV<String, ActivePowerRecord> apply(
      final KV<String, AggregatedActivePowerRecord> kv) {
    return KV.of(kv.getKey(), new ActivePowerRecord(kv.getValue().getIdentifier(),
        kv.getValue().getTimestamp(), kv.getValue().getSumInW()));
  }
}
