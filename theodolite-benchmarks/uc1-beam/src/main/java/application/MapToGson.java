package application;

import com.google.gson.Gson;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.KV;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * Converts a Map into a json String.
 */
public class MapToGson extends SimpleFunction<KV<String, ActivePowerRecord>, KV<String, String>> {
  private static final long serialVersionUID = 7168356203579050214L;
  private transient Gson gsonObj = new Gson();

  @Override
  public KV<String, String> apply(
      final KV<String, ActivePowerRecord> kv) {

    if (this.gsonObj == null) {
      this.gsonObj = new Gson();
    }

    final String gson = this.gsonObj.toJson(kv.getValue());
    return KV.of(kv.getKey(), gson);
  }
}
