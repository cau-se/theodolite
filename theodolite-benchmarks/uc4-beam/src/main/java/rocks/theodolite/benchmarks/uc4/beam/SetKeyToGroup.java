package rocks.theodolite.benchmarks.uc4.beam;

import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.KV;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * Set the Key for a group of {@code ActivePowerRecords} to their Parent.
 */
public class SetKeyToGroup
    extends SimpleFunction<KV<SensorParentKey, ActivePowerRecord>, KV<String, ActivePowerRecord>> {

  private static final long serialVersionUID = 790215050768527L;

  @Override
  public KV<String, ActivePowerRecord> apply(
      final KV<SensorParentKey, ActivePowerRecord> kv) {
    return KV.of(kv.getKey().getParent(), kv.getValue());
  }
}
