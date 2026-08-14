package rocks.theodolite.benchmarks.uc3.commons;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

/**
 * Composed key of an hour of the day and a sensor ID.
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

  /**
   * Converts this key into its compact byte representation.
   *
   * @return the encoded key
   */
  public byte[] toByteArray() {
    final int numBytes = (2 * Integer.SIZE + this.sensorId.length() * Character.SIZE) / Byte.SIZE;
    final ByteBuffer buffer = ByteBuffer.allocate(numBytes).order(ByteOrder.LITTLE_ENDIAN);
    buffer.putInt(this.hourOfDay);
    buffer.putInt(this.sensorId.length());
    for (final char character : this.sensorId.toCharArray()) {
      buffer.putChar(character);
    }
    return buffer.array();
  }

  /**
   * Reconstructs a key from its compact byte representation.
   *
   * @param bytes the encoded key
   * @return the decoded key
   */
  public static HourOfDayKey fromByteArray(final byte[] bytes) {
    final ByteBuffer buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
    final int hourOfDay = buffer.getInt();
    final int stringLength = buffer.getInt();
    final char[] sensorId = new char[stringLength];
    for (int index = 0; index < stringLength; index++) {
      sensorId[index] = buffer.getChar();
    }
    return new HourOfDayKey(hourOfDay, new String(sensorId));
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
    if (obj instanceof HourOfDayKey) {
      final HourOfDayKey other = (HourOfDayKey) obj;
      return this.hourOfDay == other.hourOfDay
          && Objects.equals(this.sensorId, other.sensorId);
    }
    return false;
  }

}
