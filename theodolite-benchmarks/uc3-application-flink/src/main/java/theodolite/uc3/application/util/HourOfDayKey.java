package theodolite.uc3.application.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

/**
 * Composed key of an hour of the day and a sensor id.
 */
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

  @Override
  public int hashCode() {
    return Objects.hash(this.hourOfDay, this.sensorId);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof HourOfDayKey)) {
      return false;
    }
    final HourOfDayKey k = (HourOfDayKey) obj;
    return this.hourOfDay == k.hourOfDay && this.sensorId.equals(k.sensorId);
  }

  /**
   * Convert this {@link HourOfDayKey} into a byte array. This method is the inverse to
   * {@code HourOfDayKey#fromByteArray()}.
   */
  public byte[] toByteArray() {
    final int numBytes = (2 * Integer.SIZE + this.sensorId.length() * Character.SIZE) / Byte.SIZE;
    final ByteBuffer buffer = ByteBuffer.allocate(numBytes).order(ByteOrder.LITTLE_ENDIAN);
    buffer.putInt(this.hourOfDay);
    buffer.putInt(this.sensorId.length());
    for (final char c : this.sensorId.toCharArray()) {
      buffer.putChar(c);
    }
    return buffer.array();
  }

  /**
   * Construct a new {@link HourOfDayKey} from a byte array. This method is the inverse to
   * {@code HourOfDayKey#toByteArray()}.
   */
  public static HourOfDayKey fromByteArray(final byte[] bytes) {
    final ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
    final int hourOfDay = buffer.getInt();
    final int strLen = buffer.getInt();
    final char[] sensorId = new char[strLen];
    for (int i = 0; i < strLen; i++) {
      sensorId[i] = buffer.getChar();
    }
    return new HourOfDayKey(hourOfDay, new String(sensorId));
  }
}
