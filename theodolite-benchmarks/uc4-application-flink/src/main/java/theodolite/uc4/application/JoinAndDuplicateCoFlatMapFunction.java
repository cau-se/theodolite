package theodolite.uc4.application;

import java.util.Set;
import org.apache.flink.api.common.state.MapState;
import org.apache.flink.api.common.state.MapStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeHint;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.RichCoFlatMapFunction;
import org.apache.flink.util.Collector;
import theodolite.uc4.application.util.SensorParentKey;
import titan.ccp.model.records.ActivePowerRecord;

public class JoinAndDuplicateCoFlatMapFunction extends
    RichCoFlatMapFunction<ActivePowerRecord, Tuple2<String, Set<String>>, Tuple2<SensorParentKey, ActivePowerRecord>> { // NOCS

  private static final long serialVersionUID = -6992783644887835979L; // NOPMD

  private transient MapState<String, Set<String>> state;

  @Override
  public void open(final Configuration parameters) throws Exception {
    final MapStateDescriptor<String, Set<String>> descriptor =
        new MapStateDescriptor<>(
            "join-and-duplicate-state",
            TypeInformation.of(new TypeHint<String>() {}),
            TypeInformation.of(new TypeHint<Set<String>>() {}));
    this.state = this.getRuntimeContext().getMapState(descriptor);
  }

  @Override
  public void flatMap1(final ActivePowerRecord value,
      final Collector<Tuple2<SensorParentKey, ActivePowerRecord>> out) throws Exception {
    final Set<String> parents = this.state.get(value.getIdentifier());
    if (parents == null) {
      return;
    }
    for (final String parent : parents) {
      out.collect(new Tuple2<>(new SensorParentKey(value.getIdentifier(), parent), value));
    }
  }

  @Override
  public void flatMap2(final Tuple2<String, Set<String>> value,
      final Collector<Tuple2<SensorParentKey, ActivePowerRecord>> out) throws Exception {
    final Set<String> oldParents = this.state.get(value.f0);
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
    this.state.put(value.f0, value.f1);
  }
}
