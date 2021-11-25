package serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import titan.ccp.model.records.AggregatedActivePowerRecord;

/**
 * Wrapper Class that encapsulates a IMonitoringRecordSerde.serializer in a Serializer
 */
public class AggregatedActivePowerRecordSerializer
    implements Serializer<AggregatedActivePowerRecord> {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(AggregatedActivePowerRecordSerializer.class);

  private final transient AvroCoder<AggregatedActivePowerRecord>
      avroEnCoder = AvroCoder.of(AggregatedActivePowerRecord.class);

  @Override
  public byte[] serialize(final String topic, final AggregatedActivePowerRecord data) {
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      this.avroEnCoder.encode(data, out);
    } catch (IOException e) {
      LOGGER.error("Could not serialize AggregatedActivePowerRecord", e);
    }
    final byte[] result = out.toByteArray();
    try {
      out.close();
    } catch (IOException e) {
      LOGGER.error(
          "Could not close output stream after serialization of AggregatedActivePowerRecord", e);
    }
    return result;
  }

  @Override
  public void close() {
    Serializer.super.close();
  }
}
