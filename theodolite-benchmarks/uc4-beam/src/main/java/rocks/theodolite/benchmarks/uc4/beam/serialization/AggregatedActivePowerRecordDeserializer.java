package rocks.theodolite.benchmarks.uc4.beam.serialization;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroDeserializer;
import org.apache.kafka.common.serialization.Deserializer;
import rocks.theodolite.benchmarks.commons.model.records.AggregatedActivePowerRecord;

/**
 * {@link Deserializer} for an {@link AggregatedActivePowerRecord}.
 */
public class AggregatedActivePowerRecordDeserializer
    extends SpecificAvroDeserializer<AggregatedActivePowerRecord> {
}
