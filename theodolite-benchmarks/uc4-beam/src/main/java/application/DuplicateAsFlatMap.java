package application;

import com.google.common.base.MoreObjects;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import org.apache.beam.sdk.state.StateSpec;
import org.apache.beam.sdk.state.StateSpecs;
import org.apache.beam.sdk.state.ValueState;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollectionView;
import titan.ccp.model.records.ActivePowerRecord;


/**
 * Duplicates the {@link KV} containing the (children,parents) pairs as flatMap.
 */
public class DuplicateAsFlatMap
    extends DoFn<KV<String, ActivePowerRecord>, KV<SensorParentKey, ActivePowerRecord>> {

  private static final long serialVersionUID = -5132355515723961647L;

  private static final String STATE_STORE_NAME = "DuplicateParents";

  @StateId(STATE_STORE_NAME)
  private final StateSpec<ValueState<Set<String>>> parents = StateSpecs.value();
  private final PCollectionView<Map<String, Set<String>>> childParentPairMap;

  public DuplicateAsFlatMap(final PCollectionView<Map<String, Set<String>>> childParentPairMap) {
    super();
    this.childParentPairMap = childParentPairMap;
  }

  /**
   * Generate a KV-pair for every child-parent match.
   */
  @ProcessElement
  public void processElement(
      @Element final KV<String, ActivePowerRecord> kv,
      final OutputReceiver<KV<SensorParentKey, ActivePowerRecord>> out,
      @StateId(STATE_STORE_NAME) final ValueState<Set<String>> state,
      final ProcessContext c) {

    final ActivePowerRecord record = kv.getValue() == null ? null : kv.getValue();
    final Set<String> newParents =
        c.sideInput(this.childParentPairMap).get(kv.getKey()) == null
            ? Collections.emptySet()
            : c.sideInput(this.childParentPairMap).get(kv.getKey());
    final Set<String> oldParents =
        MoreObjects.firstNonNull(state.read(), Collections.emptySet());
    // Forward new Pairs if they exist
    if (!newParents.isEmpty()) {
      for (final String parent : newParents) {

        // Forward flat mapped record
        final SensorParentKey key = new SensorParentKey(kv.getKey(), parent);
        out.output(KV.of(key, record));
      }
    }
    if (!newParents.equals(oldParents)) {
      for (final String oldParent : oldParents) {
        if (!newParents.contains(oldParent)) {
          // Forward Delete
          final SensorParentKey key = new SensorParentKey(kv.getKey(), oldParent);
          out.output(KV.of(key, null));
        }
      }
      state.write(newParents);
    }
  }
}

