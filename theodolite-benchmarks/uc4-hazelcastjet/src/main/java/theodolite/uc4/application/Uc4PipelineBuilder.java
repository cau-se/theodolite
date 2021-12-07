package theodolite.uc4.application;

import com.hazelcast.function.BiFunctionEx;
import com.hazelcast.function.SupplierEx;
import com.hazelcast.jet.Traverser;
import com.hazelcast.jet.Traversers;
import com.hazelcast.jet.Util;
import com.hazelcast.jet.aggregate.AggregateOperations;
import com.hazelcast.jet.kafka.KafkaSinks;
import com.hazelcast.jet.kafka.KafkaSources;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.StageWithWindow;
import com.hazelcast.jet.pipeline.StreamStage;
import com.hazelcast.jet.pipeline.StreamStageWithKey;
import com.hazelcast.jet.pipeline.WindowDefinition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import theodolite.uc4.application.uc4specifics.ChildParentsTransformer;
import theodolite.uc4.application.uc4specifics.SensorGroupKey;
import theodolite.uc4.application.uc4specifics.ValueGroup;
import titan.ccp.configuration.events.Event;
import titan.ccp.model.records.ActivePowerRecord;
import titan.ccp.model.sensorregistry.SensorRegistry;

/**
 * Builder to build a HazelcastJet Pipeline for UC4 which can be used for stream processing using
 * Hazelcast Jet.
 */
public class Uc4PipelineBuilder {

  private static final String SENSOR_PARENT_MAP_NAME = "SensorParentMap";

  /**
   * Builds a pipeline which can be used for stream processing using Hazelcast Jet.
   *
   * @param kafkaInputReadPropsForPipeline Properties Object containing the necessary kafka input
   *        read attributes.
   * @param kafkaConfigPropsForPipeline Properties Object containing the necessary kafka config
   *        read attributes.
   * @param kafkaFeedbackPropsForPipeline Properties Object containing the necessary kafka
   *        aggregation read attributes.
   * @param kafkaWritePropsForPipeline Properties Object containing the necessary kafka write
   *        attributes.
   * @param kafkaInputTopic The name of the input topic used for the pipeline.
   * @param kafkaOutputTopic The name of the output topic used for the pipeline.
   * @param kafkaConfigurationTopic The name of the configuration topic used for the pipeline.
   * @param kafkaFeedbackTopic The name of the feedback topic used for the pipeline.
   * @param windowSize The window size of the tumbling window used in this pipeline.
   * @return returns a Pipeline used which can be used in a Hazelcast Jet Instance to process data
   *         for UC3.
   */
  @SuppressWarnings("unchecked")
  public Pipeline build(final Properties kafkaInputReadPropsForPipeline, // NOPMD
      final Properties kafkaConfigPropsForPipeline,
      final Properties kafkaFeedbackPropsForPipeline,
      final Properties kafkaWritePropsForPipeline,
      final String kafkaInputTopic,
      final String kafkaOutputTopic,
      final String kafkaConfigurationTopic,
      final String kafkaFeedbackTopic,
      final int windowSize) {

    //////////////////////////////////
    // The pipeline for this Use Case
    final Pipeline uc4Pipeline = Pipeline.create();

    // System.out.println("DEBUG: Window Size: " + windowSize);

    //////////////////////////////////
    // (1) Configuration Stream
    final StreamStage<Entry<Event, SensorRegistry>> configurationStream = uc4Pipeline
        .readFrom(KafkaSources.<Event, String>kafka(
            kafkaConfigPropsForPipeline, kafkaConfigurationTopic))
        .withNativeTimestamps(0)
        .map(data -> {

          // DEBUG
          // System.out.println("D E B U G: Got a configuration Stream Element!");
          // System.out.println("Event: " + data.getKey().toString() + "; Sensor Registry: " +
          // data.getValue().toString());

          return data;

        })
        .filter(entry -> entry.getKey() == Event.SENSOR_REGISTRY_CHANGED
            || entry.getKey() == Event.SENSOR_REGISTRY_STATUS)
        .map(data -> {

          // DEBUG
          // System.out.println("D E B U G: It passed through the filter");

          return Util.entry(data.getKey(), SensorRegistry.fromJson(data.getValue()));
        });

    // Builds a new HashMap //
    final SupplierEx<? extends HashMap<String, Set<String>>> hashMapSupplier =
        () -> new HashMap<String, Set<String>>();

    // FlatMapFunction //
    final BiFunctionEx<? super HashMap<String, Set<String>>, ? super Entry<Event, SensorRegistry>,
        ? extends Traverser<Entry<String, Set<String>>>> flatMapFn =
            (flatMapStage, eventItem) -> {
              // Get Data
              HashMap<String, Set<String>> oldParents =
                  (HashMap<String, Set<String>>) flatMapStage.clone();
              SensorRegistry newSensorRegistry = (SensorRegistry) eventItem.getValue();
    
              // Transform new Input
              ChildParentsTransformer transformer = new ChildParentsTransformer("default-name");
              Map<String, Set<String>> mapFromRegistry =
                  transformer.constructChildParentsPairs(newSensorRegistry);
    
              // Compare both tables
              HashMap<String, Set<String>> updates = new HashMap<String, Set<String>>();
              for (String key : mapFromRegistry.keySet()) {
                if (oldParents.containsKey(key)) {
                  if (!mapFromRegistry.get(key).equals(oldParents.get(key))) {
                    updates.put(key, mapFromRegistry.get(key));
                  }
                } else {
                  updates.put(key, mapFromRegistry.get(key));
                }
              }
    
              ArrayList<Entry<String, Set<String>>> updatesList =
                  new ArrayList<Entry<String, Set<String>>>(updates.entrySet());
    
              // Return traverser with differences
              return Traversers.traverseIterable(updatesList)
                  .map(e -> Util.entry(e.getKey(), e.getValue()));
    
            };

    // Write into table sink
    configurationStream
        .flatMapStateful(hashMapSupplier, flatMapFn)
        .writeTo(Sinks.mapWithUpdating(
            SENSOR_PARENT_MAP_NAME, // The addressed IMAP
            event -> event.getKey(), // The key to look for
            (oldValue, newEntry) -> { // the new entry returned (null automatically results in
                                      // deletion of entry) //NOCS

              // DEBUG
              /*
               * String debugFlatmapString = "["; for (String group : newEntry.getValue()) {
               * debugFlatmapString = debugFlatmapString + group + ","; } debugFlatmapString =
               * debugFlatmapString + "]"; System.out.println( "Flatmap Writes for key '" +
               * newEntry.getKey() + "': " + debugFlatmapString);
               */

              // Write new set of groups
              return newEntry.getValue();
            }));

    //////////////////////////////////
    // (1) Sensor Input Stream
    final StreamStage<Entry<String, Double>> inputStream = uc4Pipeline
        .readFrom(KafkaSources.<String, ActivePowerRecord>kafka(
            kafkaInputReadPropsForPipeline, kafkaInputTopic))
        .withNativeTimestamps(0)
        .map(stream -> {

          String sensorId = stream.getValue().getIdentifier();
          Double valueInW = stream.getValue().getValueInW();

          // DEBUG
          // System.out.println("INPUT D E B U G: Got an input Stream Element!");
          // System.out.println("[SensorId=" + sensorId + "//valueinW=" + valueInW.toString());

          return Util.entry(sensorId, valueInW);
        });

    // (1) Aggregation Stream
    final StreamStage<Entry<String, Double>> aggregations = uc4Pipeline
        .readFrom(KafkaSources.<String, Double>kafka(
            kafkaFeedbackPropsForPipeline, kafkaFeedbackTopic))
        .withNativeTimestamps(0)
        .map(stream -> {

          // DEBUG
          // System.out.println("AGGREGATION D E B U G: Got an aggregation Stream Element!");
          // System.out.println(
          // "[SensorId=" + stream.getKey() + "//valueinW=" + stream.getValue().toString());

          return stream;

        });

    // (2) UC4 Merge Input with aggregation stream
    final StreamStageWithKey<Entry<String, Double>, String> mergedInputAndAggregations = inputStream
        .merge(aggregations)
        .groupingKey(event -> event.getKey());

    // (3) UC4 Join Configuration and Merges Input/Aggregation Stream
    // [sensorKey , (value,Set<Groups>)]
    final StreamStage<Entry<String, ValueGroup>> joinedStage = mergedInputAndAggregations
        .mapUsingIMap(
            SENSOR_PARENT_MAP_NAME,
            (sensorEvent, sensorParentsSet) -> {

              // Get Data
              Set<String> sensorParentsCasted = (Set<String>) sensorParentsSet;

              if (sensorParentsCasted == null) {
                Set<String> nullSet = new HashSet<String>();
                nullSet.add("NULL-GROUPSET");
                return Util.entry(sensorEvent.getKey(),
                    new ValueGroup(sensorEvent.getValue(), nullSet));
              } else {
                ValueGroup valueParentsPair =
                    new ValueGroup(sensorEvent.getValue(), sensorParentsCasted);
                // Return solution
                return Util.entry(sensorEvent.getKey(), valueParentsPair);
              }


            });

    // (4) UC4 Duplicate as flatmap joined Stream
    // [(sensorKey, Group) , value]
    final StreamStage<Entry<SensorGroupKey, Double>> dupliAsFlatmappedStage = joinedStage
        .flatMap(entry -> {

          // DEBUG
          // System.out.println("D E B G U G Stage 4");

          // Supplied data
          String keyGroupId = entry.getKey();
          Double valueInW = entry.getValue().getValueInW();
          Set<String> groups = entry.getValue().getGroups();

          // Transformed Data
          String[] groupList = groups.toArray(String[]::new);
          SensorGroupKey[] newKeyList = new SensorGroupKey[groupList.length];
          ArrayList<Entry<SensorGroupKey, Double>> newEntryList =
              new ArrayList<Entry<SensorGroupKey, Double>>();
          for (int i = 0; i < groupList.length; i++) {
            newKeyList[i] = new SensorGroupKey(keyGroupId, groupList[i]);
            newEntryList.add(Util.entry(newKeyList[i], valueInW));
            // DEBUG
            // System.out.println("Added new Entry to list: [(" + newKeyList[i].getSensorId() + ","
            // + newKeyList[i].getGroup() + ")," + valueInW.toString());
          }



          // Return traversable list of new entry elements
          return Traversers.traverseIterable(newEntryList);

        });

    // (5) UC4 Last Value Map
    // Table with tumbling window differentiation [ (sensorKey,Group) , value ],Time
    // TODO: Implementation of static table to fill values out of the past!
    final StageWithWindow<Entry<SensorGroupKey, Double>> windowedLastValues = dupliAsFlatmappedStage
        .window(WindowDefinition.tumbling(windowSize));

    // (6) UC4 GroupBy and aggregate and map
    // Group using the group out of the sensorGroupKey keys
    final StreamStage<Entry<String, Double>> groupedAggregatedMapped = windowedLastValues
        .groupingKey(entry -> entry.getKey().getGroup())
        .aggregate(AggregateOperations.summingDouble(entry -> entry.getValue()))
        .map(agg -> {
          String theGroup = agg.getKey();
          Double summedValueInW = agg.getValue();

          // System.out.println("DEBUG - We have a grouped Aggregation Stage at the end!");

          return Util.entry(theGroup, summedValueInW);
        });

    // (7) Sink - Results back to Kafka
    groupedAggregatedMapped.writeTo(KafkaSinks.<String, Double>kafka(
        kafkaWritePropsForPipeline, kafkaOutputTopic));

    // (7) Sink - Results back to Kafka
    groupedAggregatedMapped.writeTo(KafkaSinks.<String, Double>kafka(
        kafkaWritePropsForPipeline, kafkaFeedbackTopic));

    // (7) Sink - Write to logger/console for debug puposes
    groupedAggregatedMapped.writeTo(Sinks.logger());

    // Return the pipeline
    return uc4Pipeline;
  }

}
