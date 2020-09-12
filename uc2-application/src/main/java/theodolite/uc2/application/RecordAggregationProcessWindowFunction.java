package theodolite.uc2.application;

import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;
import theodolite.uc2.application.util.SensorParentKey;
import titan.ccp.models.records.ActivePowerRecord;
import titan.ccp.models.records.AggregatedActivePowerRecord;

public class RecordAggregationProcessWindowFunction extends ProcessWindowFunction<Tuple2<SensorParentKey, ActivePowerRecord>, AggregatedActivePowerRecord, String, TimeWindow> {

  private static final long serialVersionUID = 6030159552332624435L;

  private transient MapState<SensorParentKey, ActivePowerRecord> lastValueState;
  private transient ValueState<AggregatedActivePowerRecord> aggregateState;

  @Override
  public void open(org.apache.flink.configuration.Configuration parameters) {
    final MapStateDescriptor<SensorParentKey, ActivePowerRecord> lastValueStateDescriptor =
        new MapStateDescriptor<SensorParentKey, ActivePowerRecord>(
            "last-value-state",
            TypeInformation.of(new TypeHint<SensorParentKey>() {
            }),
            TypeInformation.of(new TypeHint<ActivePowerRecord>() {
            }));
    this.lastValueState = getRuntimeContext().getMapState(lastValueStateDescriptor);

    final ValueStateDescriptor<AggregatedActivePowerRecord> aggregateStateDescriptor =
        new ValueStateDescriptor<AggregatedActivePowerRecord>(
            "aggregation-state",
            TypeInformation.of(new TypeHint<AggregatedActivePowerRecord>() {
            }));
    this.aggregateState = getRuntimeContext().getState(aggregateStateDescriptor);
  }

  @Override
  public void process(String key, Context context, Iterable<Tuple2<SensorParentKey, ActivePowerRecord>> elements, Collector<AggregatedActivePowerRecord> out) throws Exception {
    for (Tuple2<SensorParentKey, ActivePowerRecord> t : elements) {
      AggregatedActivePowerRecord currentAggregate = this.aggregateState.value();
      if (currentAggregate == null) {
        currentAggregate = new AggregatedActivePowerRecord(key, 0, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 0, 0, 0);
        this.aggregateState.update(currentAggregate);
      }
      long count = currentAggregate.getCount();

      final SensorParentKey sensorParentKey = t.f0;
      ActivePowerRecord newRecord = t.f1;
      if (newRecord == null) { // sensor was deleted -> decrease count, set newRecord to zero
        count--;
        newRecord = new ActivePowerRecord(sensorParentKey.getSensor(), 0, 0.0);
      }

      // get last value of this record from state or create 0 valued record
      ActivePowerRecord previousRecord = this.lastValueState.get(sensorParentKey);
      if (previousRecord == null) { // sensor was added -> increase count
        count++;
        previousRecord = new ActivePowerRecord(sensorParentKey.getSensor(), 0, 0.0);
      }

      // if incoming record is older than the last saved record, skip the record
      if (newRecord.getTimestamp() < previousRecord.getTimestamp()) {
        continue;
      }

      // prefer newer timestamp, but use previous if 0 -> sensor was deleted
      long timestamp = newRecord.getTimestamp() == 0 ? previousRecord.getTimestamp() : newRecord.getTimestamp();
      double sumInW = currentAggregate.getSumInW() - previousRecord.getValueInW() + newRecord.getValueInW();
      double avgInW = count == 0 ? 0 : sumInW / count;

      AggregatedActivePowerRecord newAggregate = new AggregatedActivePowerRecord(
          sensorParentKey.getParent(),
          timestamp,
          Math.min(currentAggregate.getMinInW(), newRecord.getValueInW()),
          Math.max(currentAggregate.getMaxInW(), newRecord.getValueInW()),
          count,
          sumInW,
          avgInW
      );

      // update state and aggregateState
      this.lastValueState.put(sensorParentKey, newRecord);
      this.aggregateState.update(newAggregate);
    }

    // emit aggregated record
    out.collect(this.aggregateState.value());
  }
}
