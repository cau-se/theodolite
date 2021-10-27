package serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.CoderException;
import org.apache.kafka.common.serialization.Serde;
import titan.ccp.configuration.events.Event;
import titan.ccp.configuration.events.EventSerde;

/**
 * Wrapper Class that encapsulates a Event Serde in a org.apache.beam.sdk.coders.Coder.
 */
public class EventCoder extends Coder<Event> implements Serializable {
  private static final long serialVersionUID = 8403045343970659100L;
  private static final int VALUE_SIZE = 4;

  private transient Serde<Event> innerSerde = EventSerde.serde();

  @Override
  public void encode(final Event value, final OutputStream outStream)
      throws CoderException, IOException {
    if (this.innerSerde == null) {
      this.innerSerde = EventSerde.serde();
    }
    final byte[] bytes = this.innerSerde.serializer().serialize("ser", value);
    final byte[] sizeinBytes = ByteBuffer.allocate(VALUE_SIZE).putInt(bytes.length).array();
    outStream.write(sizeinBytes);
    outStream.write(bytes);
  }

  @Override
  public Event decode(final InputStream inStream) throws CoderException, IOException {
    if (this.innerSerde == null) {
      this.innerSerde = EventSerde.serde();
    }
    final byte[] sizeinBytes = new byte[VALUE_SIZE];
    inStream.read(sizeinBytes);
    final int size = ByteBuffer.wrap(sizeinBytes).getInt();
    final byte[] bytes = new byte[size];
    inStream.read(bytes);
    return this.innerSerde.deserializer().deserialize("deser", bytes);
  }

  @Override
  public List<? extends Coder<?>> getCoderArguments() {
    return Collections.emptyList();
  }

  @Override
  public void verifyDeterministic() throws NonDeterministicException {

  }


}
