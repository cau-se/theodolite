package application;

import com.google.common.math.Stats;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.KV;

/**
 * Transforms a {@code KV<String, Stats>} into a {@code KV<String, String>}.
 */
public class StatsToString extends SimpleFunction<KV<String, Stats>, KV<String, String>> {
  private static final long serialVersionUID = 4308991244493097240L;

  @Override
  public KV<String, String> apply(final KV<String, Stats> kv) {
    return KV.of(kv.getKey(), kv.getValue().toString());
  }
}
