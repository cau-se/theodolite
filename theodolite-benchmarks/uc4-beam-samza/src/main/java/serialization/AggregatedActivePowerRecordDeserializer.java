package serialization;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.kafka.common.serialization.Deserializer;

import titan.ccp.model.records.AggregatedActivePowerRecord;

/**
 * Wrapper Class that encapsulates a IMonitoringRecordSerde.serializer in a Deserializer
 */
public class AggregatedActivePowerRecordDeserializer
    implements Deserializer<AggregatedActivePowerRecord> {

  private transient  AvroCoder avroEnCoder = AvroCoder.of(AggregatedActivePowerRecord.class);

  @Override
  public AggregatedActivePowerRecord deserialize(final String topic, final byte[] data) {
    AggregatedActivePowerRecord value = null;
    try {
      value = (AggregatedActivePowerRecord) avroEnCoder.decode(new ByteArrayInputStream(data));
    } catch (IOException e) {
      e.printStackTrace();
    }
    return value;
  }

}
