package serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import titan.ccp.model.records.AggregatedActivePowerRecord;

/**
 * {@link Serializer} for an {@link AggregatedActivePowerRecord}.
 */
public class AggregatedActivePowerRecordSerializer
    implements Serializer<AggregatedActivePowerRecord> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(AggregatedActivePowerRecordSerializer.class);

  private final transient AvroCoder<AggregatedActivePowerRecord> avroEnCoder =
      AvroCoder.of(AggregatedActivePowerRecord.class);

  @Override
  public byte[] serialize(final String topic, final AggregatedActivePowerRecord data) {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      this.avroEnCoder.encode(data, out);
    } catch (final IOException e) {
      LOGGER.error("Could not serialize AggregatedActivePowerRecord.", e);
    }
    final byte[] result = out.toByteArray();
    try {
      out.close();
    } catch (final IOException e) {
      LOGGER.error(
          "Could not close output stream after serialization of AggregatedActivePowerRecord.", e);
    }
    return result;
  }

  @Override
  public void close() {
    Serializer.super.close();
  }
}
