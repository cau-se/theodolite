package serialization;


import application.SensorParentKey;
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
 * Wrapper Class that encapsulates a SensorParentKey Serde in a org.apache.beam.sdk.coders.Coder.
 */
public class SensorParentKeyCoder extends Coder<SensorParentKey> implements Serializable {

  private static final long serialVersionUID = -3480141901035692398L;
  private transient Serde<application.SensorParentKey> innerSerde = SensorParentKeySerde.serde(); 

  @Override
  public void encode(final SensorParentKey value, final OutputStream outStream)
      throws CoderException, IOException {
    if (this.innerSerde == null) {
      this.innerSerde = SensorParentKeySerde.serde();

    }
    final byte[] bytes = this.innerSerde.serializer().serialize("ser", value);
    final byte[] sizeinBytes = ByteBuffer.allocate(4).putInt(bytes.length).array();
    outStream.write(sizeinBytes);
    outStream.write(bytes);

  }

  @Override
  public SensorParentKey decode(final InputStream inStream) throws CoderException, IOException {
    if (this.innerSerde == null) {
      this.innerSerde = SensorParentKeySerde.serde();

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