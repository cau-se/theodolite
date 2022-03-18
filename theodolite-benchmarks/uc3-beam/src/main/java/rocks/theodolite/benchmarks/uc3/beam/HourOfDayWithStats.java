package rocks.theodolite.benchmarks.uc3.beam;

import com.google.common.math.Stats;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.KV;

/**
 * {@link SimpleFunction} that transforms into the sensorId and the Value.
 */
public class HourOfDayWithStats extends
    SimpleFunction<KV<HourOfDayKey, Stats>, KV<String, String>> {
  private static final long serialVersionUID = -7411154345437422919L;
  private final HourOfDayKeyFactory keyFactory = new HourOfDayKeyFactory();

  @Override
  public KV<String, String> apply(final KV<HourOfDayKey, Stats> kv) {
    return KV.of(this.keyFactory.getSensorId(kv.getKey()), kv.getValue().toString());
  }
}
