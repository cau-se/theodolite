package rocks.theodolite.benchmarks.uc4.beam;

import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.values.KV;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * Filters {@code null} Values.
 */
public class FilterNullValues implements
    SerializableFunction<KV<SensorParentKey, ActivePowerRecord>, Boolean> {
  private static final long serialVersionUID = -6197352369880867482L;

  @Override
  public Boolean apply(final KV<SensorParentKey, ActivePowerRecord> kv) {
    return kv.getValue() != null;
  }
}
