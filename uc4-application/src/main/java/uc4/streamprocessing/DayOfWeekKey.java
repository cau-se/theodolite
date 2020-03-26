package uc4.streamprocessing;

import java.time.DayOfWeek;

/**
 * Composed key of a {@link DayOfWeek} and a sensor id.
 */
public class DayOfWeekKey {

  private final DayOfWeek dayOfWeek;
  private final String sensorId;

  public DayOfWeekKey(final DayOfWeek dayOfWeek, final String sensorId) {
    this.dayOfWeek = dayOfWeek;
    this.sensorId = sensorId;
  }

  public DayOfWeek getDayOfWeek() {
    return this.dayOfWeek;
  }

  public String getSensorId() {
    return this.sensorId;
  }

  @Override
  public String toString() {
    return this.sensorId + ";" + this.dayOfWeek.toString();
  }

}
