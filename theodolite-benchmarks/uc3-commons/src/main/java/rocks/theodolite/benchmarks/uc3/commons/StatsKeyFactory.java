package rocks.theodolite.benchmarks.uc3.commons;

import java.time.LocalDateTime;

/**
 * Factory interface for creating a statistics key from a sensor ID and local date-time.
 *
 * @param <T> type of the key
 */
public interface StatsKeyFactory<T> {

  T createKey(String sensorId, LocalDateTime dateTime);

  String getSensorId(T key);

}
