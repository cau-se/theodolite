package uc4.streamprocessing;

import com.google.common.math.Stats;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.common.serialization.Serde;
import titan.ccp.common.kafka.GenericSerde;
import titan.ccp.common.kafka.avro.SchemaRegistryAvroSerdeFactory;
import titan.ccp.model.records.ActivePowerRecord;
import titan.ccp.model.records.AggregatedActivePowerRecord;
import titan.ccp.model.records.WindowedActivePowerRecord;

final class Serdes {

  private final SchemaRegistryAvroSerdeFactory avroSerdeFactory;

  public Serdes(final String schemaRegistryUrl) {
    this.avroSerdeFactory = new SchemaRegistryAvroSerdeFactory(schemaRegistryUrl);
  }

  public Serde<String> string() {
    return org.apache.kafka.common.serialization.Serdes.String();
  }

  public Serde<WindowedActivePowerRecord> windowedActivePowerValues() {
    return this.avroSerdeFactory.forKeys();
  }

  public Serde<ActivePowerRecord> activePowerRecordValues() {
    return this.avroSerdeFactory.forValues();
  }

  public Serde<AggregatedActivePowerRecord> aggregatedActivePowerRecordValues() {
    return this.avroSerdeFactory.forValues();
  }

  public <T extends SpecificRecord> Serde<T> avroValues() {
    return this.avroSerdeFactory.forValues();
  }

  public Serde<Stats> stats() {
    return GenericSerde.from(Stats::toByteArray, Stats::fromByteArray);
  }


}
