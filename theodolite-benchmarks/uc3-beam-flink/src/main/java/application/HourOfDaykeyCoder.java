package application;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.List;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.CoderException;
import org.apache.kafka.common.serialization.Serde;

/**
 * Wrapper Class that encapsulates a HourOfDayKeySerde in a org.apache.beam.sdk.coders.Coder.
 */
@SuppressWarnings("serial")
public class HourOfDaykeyCoder extends Coder<HourOfDayKey> implements Serializable {
  private transient Serde<HourOfDayKey> innerSerde = HourOfDayKeySerde.create(); 

  @Override
  public void encode(final HourOfDayKey value, final OutputStream outStream)
      throws CoderException, IOException {
    if (this.innerSerde == null) {
      this.innerSerde = HourOfDayKeySerde.create();
    }
    final byte[] bytes = this.innerSerde.serializer().serialize("ser", value);
    final byte[] sizeinBytes = ByteBuffer.allocate(4).putInt(bytes.length).array();
    outStream.write(sizeinBytes);
    outStream.write(bytes);
  }

  @Override
  public HourOfDayKey decode(final InputStream inStream) throws CoderException, IOException {
    if (this.innerSerde == null) {
      this.innerSerde = HourOfDayKeySerde.create();
    }
    final byte[] sizeinBytes = new byte[4];
    inStream.read(sizeinBytes);
    final int size = ByteBuffer.wrap(sizeinBytes).getInt();
    final byte[] bytes = new byte[size];
    inStream.read(bytes);
    return this.innerSerde.deserializer().deserialize("deser", bytes);
  }

  @Override
  public List<? extends Coder<?>> getCoderArguments() {
    return null;
  }

  @Override
  public void verifyDeterministic() throws NonDeterministicException {

  }

}
