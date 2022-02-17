package rocks.theodolite.benchmarks.uc1.commons;

import titan.ccp.model.records.ActivePowerRecord;

/**
 * Converts an {@link ActivePowerRecord} to the type required by a database.
 *
 * @param <T> Type required by the database.
 */
@FunctionalInterface
public interface RecordConverter<T> {

  T convert(ActivePowerRecord record);

}
