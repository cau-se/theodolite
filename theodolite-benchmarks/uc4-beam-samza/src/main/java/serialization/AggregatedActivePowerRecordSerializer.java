package serialization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.kafka.common.serialization.Serializer;
import titan.ccp.model.records.AggregatedActivePowerRecord;

/**
 * Wrapper Class that encapsulates a IMonitoringRecordSerde.serializer in a Serializer
 */
public class AggregatedActivePowerRecordSerializer
    implements Serializer<AggregatedActivePowerRecord> {

  private final transient AvroCoder avroEnCoder = AvroCoder.of(AggregatedActivePowerRecord.class);

  // Gab
  // Fehler:/home/jan/jan-bensien-bsc/uc2-application-samza/src/main
  // /java/serialization/AggregatedActivePowerRecordSerializer.java:9:
  // error: AggregatedActivePowerRecordSerializer is not abstract and does not override abstract
  // method close() in Serializer
  // public class AggregatedActivePowerRecordSerializer implements Serializer
  // <AggregatedActivePowerRecord>{

  @Override
  public byte[] serialize(final String topic, final AggregatedActivePowerRecord data) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    try {
      this.avroEnCoder.encode(data, out);
    } catch (IOException e) {
      e.printStackTrace();
    }
    byte[] result = out.toByteArray();
    try {
      out.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return result;
  }

  @Override
  public void close() {
    Serializer.super.close();
  }
}
