package serialization;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import titan.ccp.model.records.AggregatedActivePowerRecord;

/**
 * Wrapper Class that encapsulates a IMonitoringRecordSerde.serializer in a Deserializer
 */
public class AggregatedActivePowerRecordDeserializer
    implements Deserializer<AggregatedActivePowerRecord> {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(AggregatedActivePowerRecordDeserializer.class);

  private final transient AvroCoder<AggregatedActivePowerRecord> avroEnCoder =
      AvroCoder.of(AggregatedActivePowerRecord.class);

  @Override
  public AggregatedActivePowerRecord deserialize(final String topic, final byte[] data) {
    AggregatedActivePowerRecord value = null;
    try {
      value = this.avroEnCoder.decode(new ByteArrayInputStream(data));
    } catch (final IOException e) {
      LOGGER.error("Could not deserialize AggregatedActivePowerRecord", e);
    }
    return value;
  }

}
