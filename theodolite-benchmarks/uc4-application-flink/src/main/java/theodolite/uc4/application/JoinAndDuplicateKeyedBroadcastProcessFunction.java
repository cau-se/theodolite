package theodolite.uc4.application;

import java.util.Set;
import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;
import theodolite.uc4.application.util.SensorParentKey;
import titan.ccp.model.records.ActivePowerRecord;

public class JoinAndDuplicateKeyedBroadcastProcessFunction extends
    KeyedBroadcastProcessFunction<String, ActivePowerRecord, Tuple2<String, Set<String>>, Tuple2<SensorParentKey, ActivePowerRecord>> { // NOCS

  private static final long serialVersionUID = -4525438547262992821L; // NOPMD

  private final MapStateDescriptor<String, Set<String>> sensorConfigStateDescriptor =
      new MapStateDescriptor<>(
          "join-and-duplicate-state",
          BasicTypeInfo.STRING_TYPE_INFO,
          TypeInformation.of(new TypeHint<Set<String>>() {}));

  @Override
  public void processElement(final ActivePowerRecord value, final ReadOnlyContext ctx,
      final Collector<Tuple2<SensorParentKey, ActivePowerRecord>> out) throws Exception {
    final Set<String> parents =
        ctx.getBroadcastState(this.sensorConfigStateDescriptor).get(value.getIdentifier());
    if (parents == null) {
      return;
    }
    for (final String parent : parents) {
      out.collect(new Tuple2<>(new SensorParentKey(value.getIdentifier(), parent), value));
    }
  }

  @Override
  public void processBroadcastElement(final Tuple2<String, Set<String>> value, final Context ctx,
      final Collector<Tuple2<SensorParentKey, ActivePowerRecord>> out) throws Exception {
    final BroadcastState<String, Set<String>> state =
        ctx.getBroadcastState(this.sensorConfigStateDescriptor);
    final Set<String> oldParents = state.get(value.f0);
    if (oldParents != null) {
      final Set<String> newParents = value.f1;
      if (!newParents.equals(oldParents)) {
        for (final String oldParent : oldParents) {
          if (!newParents.contains(oldParent)) {
            out.collect(new Tuple2<>(new SensorParentKey(value.f0, oldParent), null));
          }
        }
      }
    }
    state.put(value.f0, value.f1);
  }

}
