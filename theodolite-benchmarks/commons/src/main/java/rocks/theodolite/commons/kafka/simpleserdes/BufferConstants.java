package rocks.theodolite.commons.kafka.simpleserdes;

import java.nio.charset.Charset;

/**
 * Shared constants between {@link WriteBuffer} and {@link ReadBuffer}.
 */
public final class BufferConstants {

  public static final Charset CHARSET = Charset.forName("UTF-8");

  public static final byte BOOLEAN_FALSE = 0;

  public static final byte BOOLEAN_TRUE = 1;

  private BufferConstants() {}

}
