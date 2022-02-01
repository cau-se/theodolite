package serialization;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerializer;
import org.apache.kafka.common.serialization.Serializer;
import titan.ccp.model.records.AggregatedActivePowerRecord;

/**
 * {@link Serializer} for an {@link AggregatedActivePowerRecord}.
 */
public class AggregatedActivePowerRecordSerializer
    extends SpecificAvroSerializer<AggregatedActivePowerRecord> {
}
