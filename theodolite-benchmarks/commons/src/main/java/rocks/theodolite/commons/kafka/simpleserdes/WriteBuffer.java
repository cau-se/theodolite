package rocks.theodolite.commons.kafka.simpleserdes;

import java.nio.ByteBuffer;

/**
 * A buffer to which different kinds of data types can be written and its content can be returned as
 * bytes. Its counterpart is a {@link ReadBuffer} which allows to read the data in the order it is
 * written.
 */
public class WriteBuffer {

  private static final int BYTE_BUFFER_CAPACITY = 65_536; // Is only virtual memory

  private final ByteBuffer buffer = ByteBuffer.allocateDirect(BYTE_BUFFER_CAPACITY);

  public void putByte(final byte value) {
    this.buffer.put(value);
  }

  public void putBytes(final byte[] value) {
    this.buffer.putInt(value.length);
    this.buffer.put(value);
  }

  public void putBoolean(final boolean value) {
    this.putByte(
        value ? BufferConstants.BOOLEAN_TRUE : BufferConstants.BOOLEAN_FALSE);
  }

  public void putShort(final short value) { // NOPMD
    this.buffer.putShort(value);
  }

  public void putInt(final int value) {
    this.buffer.putInt(value);
  }

  public void putLong(final long value) {
    this.buffer.putLong(value);
  }

  public void putFloat(final float value) {
    this.buffer.putFloat(value);
  }

  public void putDouble(final double value) {
    this.buffer.putDouble(value);
  }

  public void putChar(final char value) {
    this.buffer.putChar(value);
  }

  public void putString(final String value) {
    final byte[] bytes = value.getBytes(BufferConstants.CHARSET);
    this.putBytes(bytes);
  }

  /**
   * Get the content of this buffer as bytes.
   */
  public byte[] getBytes() {
    this.buffer.flip();
    final byte[] bytes = new byte[this.buffer.remaining()];
    this.buffer.get(bytes, 0, bytes.length);
    return bytes;
  }

}
