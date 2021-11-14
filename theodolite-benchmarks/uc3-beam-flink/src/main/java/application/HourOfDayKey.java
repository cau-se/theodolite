package application;

import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;


/**
 * Composed key of an hour of the day and a sensor id.
 */

@DefaultCoder(AvroCoder.class)
public class HourOfDayKey {

  private final int hourOfDay;
  private final String sensorId;

  public HourOfDayKey(final int hourOfDay, final String sensorId) {
    this.hourOfDay = hourOfDay;
    this.sensorId = sensorId;
  }

  public int getHourOfDay() {
    return this.hourOfDay;
  }

  public String getSensorId() {
    return this.sensorId;
  }

  @Override
  public String toString() {
    return this.sensorId + ";" + this.hourOfDay;
  }

}
