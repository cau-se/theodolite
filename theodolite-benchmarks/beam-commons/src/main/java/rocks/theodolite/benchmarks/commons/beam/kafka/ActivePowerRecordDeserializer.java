package rocks.theodolite.benchmarks.commons.beam.kafka;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroDeserializer;
import org.apache.kafka.common.serialization.Deserializer;
import rocks.theodolite.benchmarks.commons.model.records.ActivePowerRecord;


/**
 * A Kafka {@link Deserializer} for typed Schema Registry {@link ActivePowerRecord}.
 */
public class ActivePowerRecordDeserializer extends SpecificAvroDeserializer<ActivePowerRecord> {
}
