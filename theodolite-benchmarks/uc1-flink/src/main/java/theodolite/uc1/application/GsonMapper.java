package theodolite.uc1.application;

import com.google.gson.Gson;
import org.apache.flink.api.common.functions.MapFunction;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * {@link MapFunction} which maps {@link ActivePowerRecord}s to their representation as JSON
 * strings.
 */
public class GsonMapper implements MapFunction<ActivePowerRecord, String> {

  private static final long serialVersionUID = -5263671231838353747L; // NOPMD

  private static final Gson GSON = new Gson();

  @Override
  public String map(final ActivePowerRecord value) throws Exception {
    return GSON.toJson(value);
  }

}
