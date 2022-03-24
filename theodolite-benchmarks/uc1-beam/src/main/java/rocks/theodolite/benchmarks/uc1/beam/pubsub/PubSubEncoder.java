package rocks.theodolite.benchmarks.uc1.beam.pubsub;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.transforms.SimpleFunction;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * A {@link SimpleFunction}, extracting and decoding {@link ActivePowerRecord}s from
 * {@link PubsubMessage}s.
 */
public final class PubSubEncoder extends SimpleFunction<PubsubMessage, ActivePowerRecord> {

  private static final long serialVersionUID = -8872981416931508879L;

  @Override
  public ActivePowerRecord apply(final PubsubMessage message) {
    try {
      return ActivePowerRecord.fromByteBuffer(ByteBuffer.wrap(message.getPayload()));
    } catch (final IOException e) {
      throw new IllegalStateException(e);
    }
  }

}
