package rocks.theodolite.benchmarks.uc3.hazelcastjet;

import java.time.ZoneId;

/**
 * Stores a configuration consisting of a {@link StatsKeyFactory} and a {@link ZoneId}.
 */
public class MapTimeKeyConfiguration {

  private final StatsKeyFactory<HourOfDayKey> keyFactory;
  private final ZoneId zone;

  /**
   * Create a {@link MapTimeKeyConfiguration} for the supplied {@link StatsKeyFactory} and
   * {@link ZoneId}.
   */
  public MapTimeKeyConfiguration(
      final StatsKeyFactory<HourOfDayKey> keyFactory,
      final ZoneId zone) {
    super();
    this.keyFactory = keyFactory;
    this.zone = zone;
  }

  public StatsKeyFactory<HourOfDayKey> getKeyFactory() {
    return this.keyFactory;
  }

  public ZoneId getZone() {
    return this.zone;
  }

}
