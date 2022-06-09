package rocks.theodolite.benchmarks.commons.kafka.simpleserdes;

import java.nio.ByteBuffer;

/**
 * A buffer that is constructed from a byte array and data can be sequentially read to different
 * kinds of data types. It is the counterpart to {@link WriteBuffer}.
 */
public class ReadBuffer {

  private final ByteBuffer buffer;

  /**
   * Create this buffer by a byte array.
   */
  public ReadBuffer(final byte[] bytes) {
    this.buffer = ByteBuffer.wrap(bytes);
  }

  public byte getByte() {
    return this.buffer.get();
  }

  /**
   * Get a byte array.
   */
  public byte[] getBytes() {
    final int bytesLength = this.buffer.getInt();
    final byte[] bytes = new byte[bytesLength];
    this.buffer.get(bytes);
    return bytes;
  }

  public boolean getBoolean() { // NOPMD
    return this.getByte() == BufferConstants.BOOLEAN_TRUE;
  }

  public short getShort() { // NOPMD
    return this.buffer.getShort();
  }

  public int getInt() {
    return this.buffer.getInt();
  }

  public long getLong() {
    return this.buffer.getLong();
  }

  public float getFloat() {
    return this.buffer.getFloat();
  }

  public double getDouble() {
    return this.buffer.getDouble();
  }

  public int getChar() {
    return this.buffer.getChar();
  }

  public String getString() {
    final byte[] bytes = this.getBytes();
    return new String(bytes, BufferConstants.CHARSET);
  }

}
