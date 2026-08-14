package rocks.theodolite.benchmarks.uc4.beam;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import rocks.theodolite.benchmarks.commons.configuration.events.Event;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.SensorRegistry;
import rocks.theodolite.benchmarks.uc4.commons.ChildParentPairBuilder;

/**
 * DoFn class to generate a child-parent pair for every sensor in the hierarchy.
 */
public class GenerateParentsFn extends DoFn<KV<Event, String>, KV<String, Set<String>>> {

  private static final long serialVersionUID = 958270648688932091L;

  /**
   * Transforms a parent [children] map of sensors to a child [parents] map.
   *
   * @param kv input map.
   * @param out outputstream.
   */
  @ProcessElement
  public void processElement(@Element final KV<Event, String> kv,
      final OutputReceiver<KV<String, Set<String>>> out) {
    final Map<String, Set<String>> childParentsPairs =
        ChildParentPairBuilder.build(SensorRegistry.fromJson(kv.getValue()));
    final Iterator<Map.Entry<String, Set<String>>> it = childParentsPairs.entrySet().iterator();
    while (it.hasNext()) {
      final Map.Entry<String, Set<String>> pair = it.next();
      out.output(KV.of(pair.getKey(), pair.getValue()));
    }

  }

}
