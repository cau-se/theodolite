package rocks.theodolite.benchmarks.uc4.beam.serialization;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer;
import org.apache.kafka.common.serialization.Serializer;
import rocks.theodolite.benchmarks.commons.model.records.AggregatedActivePowerRecord;

/**
 * {@link Serializer} for an {@link AggregatedActivePowerRecord}.
 */
public class AggregatedActivePowerRecordSerializer
    extends SpecificAvroSerializer<AggregatedActivePowerRecord> {
}
