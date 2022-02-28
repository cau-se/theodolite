package rocks.theodolite.benchmarks.uc4.flink;

import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import rocks.theodolite.benchmarks.uc4.flink.util.SensorParentKey;
import titan.ccp.model.records.ActivePowerRecord;
import titan.ccp.model.records.AggregatedActivePowerRecord;

/**
 * A {@link ProcessWindowFunction} which performs the windowed aggregation of all
 * {@link ActivePowerRecord} for the same {@link SensorParentKey}. Result of this aggregation is an
 * {@link AggregatedActivePowerRecord}.
 */
public class RecordAggregationProcessWindowFunction extends
    ProcessWindowFunction<Tuple2<SensorParentKey, ActivePowerRecord>, AggregatedActivePowerRecord, String, TimeWindow> { // NOCS

  private static final long serialVersionUID = 6030159552332624435L; // NOPMD

  private transient MapState<SensorParentKey, ActivePowerRecord> lastValueState;
  private transient ValueState<AggregatedActivePowerRecord> aggregateState;

  @Override
  public void open(final Configuration parameters) {
    final MapStateDescriptor<SensorParentKey, ActivePowerRecord> lastValueStateDescriptor =
        new MapStateDescriptor<>(
            "last-value-state",
            TypeInformation.of(new TypeHint<SensorParentKey>() {}),
            TypeInformation.of(new TypeHint<ActivePowerRecord>() {}));
    this.lastValueState = this.getRuntimeContext().getMapState(lastValueStateDescriptor);

    final ValueStateDescriptor<AggregatedActivePowerRecord> aggregateStateDescriptor =
        new ValueStateDescriptor<>(
            "aggregation-state",
            TypeInformation.of(new TypeHint<AggregatedActivePowerRecord>() {}));
    this.aggregateState = this.getRuntimeContext().getState(aggregateStateDescriptor);
  }

  @Override
  public void process(
      final String key,
      final Context context,
      final Iterable<Tuple2<SensorParentKey, ActivePowerRecord>> elements,
      final Collector<AggregatedActivePowerRecord> out) throws Exception {
    for (final Tuple2<SensorParentKey, ActivePowerRecord> t : elements) {
      AggregatedActivePowerRecord currentAggregate = this.aggregateState.value();
      if (currentAggregate == null) {
        currentAggregate = new AggregatedActivePowerRecord(key, 0L, 0L, 0.0, 0.0);
        this.aggregateState.update(currentAggregate);
      }
      long count = currentAggregate.getCount();

      final SensorParentKey sensorParentKey = t.f0;
      ActivePowerRecord newRecord = t.f1;
      if (newRecord == null) { // sensor was deleted -> decrease count, set newRecord to zero
        count--;
        newRecord = new ActivePowerRecord(sensorParentKey.getSensor(), 0L, 0.0);
      }

      // get last value of this record from state or create 0 valued record
      ActivePowerRecord previousRecord = this.lastValueState.get(sensorParentKey);
      if (previousRecord == null) { // sensor was added -> increase count
        count++;
        previousRecord = new ActivePowerRecord(sensorParentKey.getSensor(), 0L, 0.0);
      }

      // if incoming record is older than the last saved record, skip the record
      if (newRecord.getTimestamp() < previousRecord.getTimestamp()) {
        continue;
      }

      // prefer newer timestamp, but use previous if 0 -> sensor was deleted
      final long timestamp =
          newRecord.getTimestamp() == 0 ? previousRecord.getTimestamp() : newRecord.getTimestamp();
      final double sumInW =
          currentAggregate.getSumInW() - previousRecord.getValueInW() + newRecord.getValueInW();
      final double avgInW = count == 0 ? 0 : sumInW / count;

      final AggregatedActivePowerRecord newAggregate = new AggregatedActivePowerRecord(
          sensorParentKey.getParent(),
          timestamp,
          count,
          sumInW,
          avgInW);

      // update state and aggregateState
      this.lastValueState.put(sensorParentKey, newRecord);
      this.aggregateState.update(newAggregate);
    }

    // emit aggregated record
    out.collect(this.aggregateState.value());
  }
}
