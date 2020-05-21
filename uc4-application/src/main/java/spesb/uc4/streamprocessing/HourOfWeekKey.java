package spesb.uc4.streamprocessing;

import java.time.DayOfWeek;

/**
 * Composed key of a {@link DayOfWeek}, an hour of the day and a sensor id.
 */
public class HourOfWeekKey {

  private final DayOfWeek dayOfWeek;
  private final int hourOfDay;
  private final String sensorId;

  /**
   * Create a new {@link HourOfDayKey} using its components.
   */
  public HourOfWeekKey(final DayOfWeek dayOfWeek, final int hourOfDay, final String sensorId) {
    this.dayOfWeek = dayOfWeek;
    this.hourOfDay = hourOfDay;
    this.sensorId = sensorId;
  }

  public DayOfWeek getDayOfWeek() {
    return this.dayOfWeek;
  }

  public int getHourOfDay() {
    return this.hourOfDay;
  }

  public String getSensorId() {
    return this.sensorId;
  }

  @Override
  public String toString() {
    return this.sensorId + ";" + this.dayOfWeek.toString() + ";" + this.hourOfDay;
  }

}
