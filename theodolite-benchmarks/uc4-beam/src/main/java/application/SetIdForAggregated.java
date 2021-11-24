package application;

import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.KV;
import titan.ccp.model.records.AggregatedActivePowerRecord;

public class SetIdForAggregated extends SimpleFunction<KV<String, AggregatedActivePowerRecord>,
    KV<String, AggregatedActivePowerRecord>> {
  @Override
  public KV<String, AggregatedActivePowerRecord> apply(
      final KV<String, AggregatedActivePowerRecord> kv) {
    final AggregatedActivePowerRecord record = new AggregatedActivePowerRecord(
        kv.getKey(), kv.getValue().getTimestamp(), kv.getValue().getCount(),
        kv.getValue().getSumInW(), kv.getValue().getAverageInW());
    return KV.of(kv.getKey(), record);
  }
}
