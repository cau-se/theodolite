package application;

import java.util.Set;
import org.apache.beam.sdk.state.StateSpec;
import org.apache.beam.sdk.state.StateSpecs;
import org.apache.beam.sdk.state.ValueState;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;

/**
 * Forward changes or tombstone values for deleted records.
 */
public class UpdateChildParentPairs extends DoFn<KV<String, Set<String>>, KV<String, Set<String>>> {

  private static final String STATE_STORE_NAME = "UpdateParents";

  private static final long serialVersionUID = 1L;

  @StateId(STATE_STORE_NAME)
  private final StateSpec<ValueState<Set<String>>> parents = StateSpecs.value();

  /**
   * Match the changes accordingly.
   *
   * @param kv the sensor parents set that contains the changes.
   */
  @ProcessElement
  public void processElement(
      @Element final KV<String, Set<String>> kv,
      final OutputReceiver<KV<String, Set<String>>> out,
      @StateId(STATE_STORE_NAME) final ValueState<Set<String>> state) {
    if (kv.getValue() == null || !kv.getValue().equals(state.read())) {
      out.output(kv);
      state.write(kv.getValue());
    }

  }
}
