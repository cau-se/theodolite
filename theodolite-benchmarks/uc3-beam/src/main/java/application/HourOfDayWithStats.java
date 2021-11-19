package application;

import com.google.common.math.Stats;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.KV;

/**
 *
 */
public class HourOfDayWithStats extends SimpleFunction<KV<HourOfDayKey, Stats>, KV<String, String>> {
  private final HourOfDayKeyFactory keyFactory = new HourOfDayKeyFactory();

  @Override
  public KV<String, String> apply(final KV<HourOfDayKey, Stats> kv) {
    return KV.of(keyFactory.getSensorId(kv.getKey()), kv.getValue().toString());
  }
}
