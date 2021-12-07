package theodolite.uc4.application;

import com.hazelcast.function.FunctionEx;
import com.hazelcast.jet.JetInstance;
import com.hazelcast.jet.Traverser;
import com.hazelcast.jet.Traversers;
import com.hazelcast.jet.aggregate.AggregateOperations;
import com.hazelcast.jet.datamodel.WindowResult;
import com.hazelcast.jet.kafka.KafkaSinks;
import com.hazelcast.jet.kafka.KafkaSources;
import com.hazelcast.jet.pipeline.JoinClause;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.StreamSourceStage;
import com.hazelcast.jet.pipeline.StreamStage;
import com.hazelcast.jet.pipeline.StreamStageWithKey;
import com.hazelcast.jet.pipeline.WindowDefinition;
import com.hazelcast.map.IMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import org.apache.kafka.streams.kstream.KTable;
import theodolite.uc4.application.uc4specifics.ValueGroup;
import titan.ccp.configuration.events.Event;
import titan.ccp.model.records.ActivePowerRecord;
import titan.ccp.model.records.AggregatedActivePowerRecord;
import titan.ccp.model.sensorregistry.SensorRegistry;

public class Uc4PipelineBuilder {

  private Pipeline pipe = Pipeline.create();

  // Data
  private String kafkaInputTopic;
  private String kafkaConfigurationTopic;
  private Properties kafkaReadPropsForPipeline;
  private JetInstance uc4JetInstance;



  /**
   * Builds a pipeline which can be used for stream processing using Hazelcast Jet.
   *
   * @param kafkaReadPropsForPipeline Properties Object containing the necessary kafka reads
   *        attributes.
   * @param kafkaWritePropsForPipeline Properties Object containing the necessary kafka write
   *        attributes.
   * @param kafkaInputTopic The name of the input topic used for the pipeline.
   * @param kafkaOutputTopic The name of the output topic used for the pipeline.
   * @param windowSize The window size in milliseconds of the tumbling window used in the "last
   *        values" aggregation of this pipeline.
   * @return returns a Pipeline used which can be used in a Hazelcast Jet Instance to process data
   *         for UC4.
   */
  public Pipeline build(final Properties kafkaReadPropsForPipeline,
      final Properties kafkaWritePropsForPipeline, final String kafkaInputTopic,
      final String kafkaOutputTopic,
      final String kafkaConfigurationTopic,
      final int windowSize,
      JetInstance jet) {


    this.uc4JetInstance = jet;
    
    ///////////////////////
    // 1. Configuration Map
    // this.kafkaConfigurationTopic = kafkaConfigurationTopic;
    // this.kafkaReadPropsForPipeline = kafkaReadPropsForPipeline;
    // final IMap<String, Set<String>> parentSensorTable = this.buildParentSensorMap();
    ///////////////////////
    StreamStage<Entry<String, Set<String>>> configurationStream = null;
    
    ////////////////
    // 2. Input Map   
    // this.kafkaInputTopic = kafkaInputTopic;
    // final IMap<String, ActivePowerRecord> inputTable = this.buildInputTable();  
    ////////////////
    StreamStage<Entry<String, ActivePowerRecord>> inputStream = this.pipe
        .readFrom(KafkaSources.<String, ActivePowerRecord>kafka(
            kafkaReadPropsForPipeline, kafkaInputTopic))
        .withNativeTimestamps(0);
        
    StreamStage<Entry<String, Double>> reducedInputStream = inputStream
        .map(inputEntry -> {
          return Map.entry(inputEntry.getValue().getIdentifier(), 
              inputEntry.getValue().getValueInW());
        });
    
    //////////////////////////////////////////////////////////
    // 3. Last Value Table from Input and Parent Sensor Table
    // final IMap<WindowResult<SensorParentKey>, ActivePowerRecord> lastValueTable =
    //     this.buildLastValueTable(parentSensorTable, inputTable);
    ////////////////////////////////////////////////////////////
    //StreamStage<Entry<String,ValueGroup>> jointStream =
    //    inputStream.hashJoin(configurationStream, 
    //        JoinClause.joinMapEntries(leftKeyFn), 
    //        mapToOutputFn); // TODO hmm, how to join?
    
    // 4. Aggregation Stream
    //final IMap<WindowResult<String>, AggregatedActivePowerRecord> aggregations =
    //    this.buildAggregationStream(lastValueTable);

    return pipe;
  }

  /**
   * Uses a given configuration topic of kafka to get data which represents a table of sensor or
   * group identifiers which are mapped to a set of groups and returns an IMap containing these
   * entries.
   * 
   * TODO WORK IN PROGRESS - QUESTIONS REGARDING THE LAST STEPS
   * 
   * @return Returns a IMap<String, Set<String>> Object containing sensor/group identifiers and
   *         their corresponsing groups/parents.
   */
  private IMap<String, Set<String>> buildParentSensorMap() {
    // Read the raw configuration stream
    StreamStage<Entry<Event, String>> configurationStream = this.pipe
        .readFrom(KafkaSources.<Event, String>kafka(
            kafkaReadPropsForPipeline, kafkaConfigurationTopic))
        .withNativeTimestamps(0);

    // Filter certain values out
    StreamStage<Entry<Event, String>> filteredConfigurationStream = configurationStream
        .filter(entry -> entry.getKey() == Event.SENSOR_REGISTRY_CHANGED
            || entry.getKey() == Event.SENSOR_REGISTRY_STATUS);

    // Map configuration String to Sensor Registry
    StreamStage<Entry<Event, SensorRegistry>> mapped = filteredConfigurationStream
        .map(inputEntry -> Map.entry(inputEntry.getKey(),
            SensorRegistry.fromJson(inputEntry.getValue())));


    // Flat Transform TODO Needs Traversers thingy?
    StreamStage<Entry<String, Optional<Set<String>>>> flatMapped = mapped.flatMap(null);

    // Group by Key TODO
    StreamStageWithKey<Entry<String, Optional<Set<String>>>, Object> grouped =
        flatMapped.groupingKey(entry -> entry.getKey());

    // Aggregate TODO
    IMap<String, Set<String>> aggregated =
        (IMap<String, Set<String>>) grouped.rollingAggregate(null);


    // Return
    return aggregated;
  }

  /**
   * Receives an input stream with sensor ID's and values and returns a filled IMap with such
   * values.
   * 
   * TODO WORK IN PROGRESS - QUESTIONS
   * 
   * @return An IMap<String,ActivePowerRecord> Object with entries
   */
  private IMap<String, ActivePowerRecord> buildInputTable() {

    final IMap<String, ActivePowerRecord> inputTable = uc4JetInstance.getMap("inputTable");

    // Read Input Stream
    // TODO MERGE STEP WITH AGGREGATION RESULTS SKIPPED AT THE MOMENT
    StreamStage<Entry<String, ActivePowerRecord>> inputStream = this.pipe
        .readFrom(KafkaSources.<String, ActivePowerRecord>kafka(
            kafkaReadPropsForPipeline, kafkaInputTopic))
        .withNativeTimestamps(0)
        .map(entry -> {
          inputTable.put(entry.getKey(), entry.getValue());
          return entry;
        });

    return inputTable;

  }


}
