package rocks.theodolite.benchmarks.uc1.commons.logger;

import com.google.gson.Gson;
import java.io.Serializable;
import rocks.theodolite.benchmarks.commons.model.records.ActivePowerRecord;
import rocks.theodolite.benchmarks.uc1.commons.RecordConverter;

/**
 * {@link RecordConverter} that converts {@link ActivePowerRecord}s to JSON strings.
 */
public class JsonConverter implements RecordConverter<String>, Serializable {

  private static final long serialVersionUID = -5263671231838353748L; // NOPMD

  private static final Gson GSON = new Gson();

  @Override
  public String convert(final ActivePowerRecord activePowerRecord) {
    return GSON.toJson(activePowerRecord);
  }

}
