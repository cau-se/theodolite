package theodolite.uc2.application;

import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.RichCoFlatMapFunction;
import org.apache.flink.util.Collector;
import theodolite.uc2.application.util.SensorParentKey;
import titan.ccp.models.records.ActivePowerRecord;

import java.util.Set;

public class JoinAndDuplicateCoFlatMapFunction extends RichCoFlatMapFunction<ActivePowerRecord, Tuple2<String, Set<String>>, Tuple2<SensorParentKey, ActivePowerRecord>> {

  private static final long serialVersionUID = -6992783644887835979L;

  private transient MapState<String, Set<String>> state;

  @Override
  public void open(Configuration parameters) throws Exception {
    MapStateDescriptor<String, Set<String>> descriptor =
        new MapStateDescriptor<String, Set<String>>(
            "join-and-duplicate-state",
            TypeInformation.of(new TypeHint<String>(){}),
            TypeInformation.of(new TypeHint<Set<String>>(){}));
    this.state = getRuntimeContext().getMapState(descriptor);
  }

  @Override
  public void flatMap1(ActivePowerRecord value, Collector<Tuple2<SensorParentKey, ActivePowerRecord>> out) throws Exception {
    Set<String> parents = this.state.get(value.getIdentifier());
    if (parents == null) return;
    for (String parent : parents) {
      out.collect(new Tuple2<>(new SensorParentKey(value.getIdentifier(), parent), value));
    }
  }

  @Override
  public void flatMap2(Tuple2<String, Set<String>> value, Collector<Tuple2<SensorParentKey, ActivePowerRecord>> out) throws Exception {
    Set<String> oldParents = this.state.get(value.f0);
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
    this.state.put(value.f0, value.f1);
  }
}
