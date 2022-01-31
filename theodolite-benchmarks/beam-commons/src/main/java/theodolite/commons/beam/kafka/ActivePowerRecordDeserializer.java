package theodolite.commons.beam.kafka;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroDeserializer;
import org.apache.kafka.common.serialization.Deserializer;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * A Kafka {@link Deserializer} for typed Schema Registry {@link ActivePowerRecord}.
 */
public class ActivePowerRecordDeserializer extends SpecificAvroDeserializer<ActivePowerRecord> {
}
