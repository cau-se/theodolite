package theodolite.uc2.application;

import org.apache.flink.api.common.state.BroadcastState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.BasicTypeInfo;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.co.KeyedBroadcastProcessFunction;
import org.apache.flink.util.Collector;
import theodolite.uc2.application.util.SensorParentKey;
import titan.ccp.models.records.ActivePowerRecord;

import java.util.Set;

public class JoinAndDuplicateKeyedBroadcastProcessFunction extends KeyedBroadcastProcessFunction<String, ActivePowerRecord, Tuple2<String, Set<String>>, Tuple2<SensorParentKey, ActivePowerRecord>> {

  private static final long serialVersionUID = -4525438547262992821L;

  private final MapStateDescriptor<String, Set<String>> sensorConfigStateDescriptor =
      new MapStateDescriptor<>(
          "join-and-duplicate-state",
          BasicTypeInfo.STRING_TYPE_INFO,
          TypeInformation.of(new TypeHint<Set<String>>() {}));

  @Override
  public void processElement(ActivePowerRecord value, ReadOnlyContext ctx, Collector<Tuple2<SensorParentKey, ActivePowerRecord>> out) throws Exception {
    Set<String> parents = ctx.getBroadcastState(sensorConfigStateDescriptor).get(value.getIdentifier());
    if (parents == null) return;
    for (String parent : parents) {
      out.collect(new Tuple2<>(new SensorParentKey(value.getIdentifier(), parent), value));
    }
  }

  @Override
  public void processBroadcastElement(Tuple2<String, Set<String>> value, Context ctx, Collector<Tuple2<SensorParentKey, ActivePowerRecord>> out) throws Exception {
    BroadcastState<String, Set<String>> state = ctx.getBroadcastState(sensorConfigStateDescriptor);
    Set<String> oldParents = state.get(value.f0);
    if (oldParents != null) {
      Set<String> newParents = value.f1;
      if (!newParents.equals(oldParents)) {
        for (String oldParent : oldParents) {
          if (!newParents.contains(oldParent)) {
            out.collect(new Tuple2<>(new SensorParentKey(value.f0, oldParent), null));
          }
        }
      }
    }
    state.put(value.f0, value.f1);
  }

}
