package rocks.theodolite.benchmarks.uc1.beam.firestore.custom;

import java.io.Serializable;
import java.util.Map;
import rocks.theodolite.benchmarks.uc1.commons.RecordConverter;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * {@link RecordConverter} that converts {@link ActivePowerRecord}s to maps of strings to objects.
 */
public class MapConverter implements RecordConverter<Map<String, Object>>, Serializable {

  private static final long serialVersionUID = -5263671231838353748L; // NOPMD

  @Override
  public Map<String, Object> convert(final ActivePowerRecord record) {
    return Map.of(
        "identifier", record.getIdentifier(),
        "timestamp", record.getTimestamp(),
        "valueInW", record.getValueInW());
  }

}
