package rocks.theodolite.benchmarks.uc4.hazelcastjet; // NOPMD Excessive imports

import com.hazelcast.function.BiFunctionEx;
import com.hazelcast.jet.Traverser;
import com.hazelcast.jet.Traversers;
import com.hazelcast.jet.Util;
import com.hazelcast.jet.aggregate.AggregateOperation;
import com.hazelcast.jet.aggregate.AggregateOperation1;
import com.hazelcast.jet.kafka.KafkaSinks;
import com.hazelcast.jet.kafka.KafkaSources;
import com.hazelcast.jet.pipeline.Pipeline;
import com.hazelcast.jet.pipeline.Sinks;
import com.hazelcast.jet.pipeline.StageWithWindow;
import com.hazelcast.jet.pipeline.StreamSource;
import com.hazelcast.jet.pipeline.StreamStage;
import com.hazelcast.jet.pipeline.StreamStageWithKey;
import com.hazelcast.jet.pipeline.WindowDefinition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.theodolite.benchmarks.commons.configuration.events.Event;
import rocks.theodolite.benchmarks.commons.model.records.ActivePowerRecord;
import rocks.theodolite.benchmarks.commons.model.records.AggregatedActivePowerRecord;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.SensorRegistry;
import rocks.theodolite.benchmarks.uc4.hazelcastjet.uc4specifics.AggregatedActivePowerRecordAccumulator;
import rocks.theodolite.benchmarks.uc4.hazelcastjet.uc4specifics.ChildParentsTransformer;
import rocks.theodolite.benchmarks.uc4.hazelcastjet.uc4specifics.SensorGroupKey;
import rocks.theodolite.benchmarks.uc4.hazelcastjet.uc4specifics.ValueGroup;

/**
 * Builder to build a HazelcastJet Pipeline for UC4 which can be used for stream processing using
 * Hazelcast Jet.
 */
public class Uc4PipelineBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(Uc4PipelineBuilder.class);
  private static final String SENSOR_PARENT_MAP_NAME = "SensorParentMap";

  /**
   * Builds a pipeline which can be used for stream processing using Hazelcast Jet.
   *
   * @param kafkaInputReadPropsForPipeline Properties Object containing the necessary kafka input
   *        read attributes.
   * @param kafkaConfigPropsForPipeline Properties Object containing the necessary kafka config read
   *        attributes.
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
  public Pipeline build(final Properties kafkaInputReadPropsForPipeline, // NOPMD
      final Properties kafkaConfigPropsForPipeline,
      final Properties kafkaFeedbackPropsForPipeline,
      final Properties kafkaWritePropsForPipeline,
      final String kafkaInputTopic,
      final String kafkaOutputTopic,
      final String kafkaConfigurationTopic,
      final String kafkaFeedbackTopic,
      final int windowSize) {

    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("kafkaConfigProps: " + kafkaConfigPropsForPipeline);
      LOGGER.info("kafkaFeedbackProps: " + kafkaFeedbackPropsForPipeline);
      LOGGER.info("kafkaWriteProps: " + kafkaWritePropsForPipeline);
    }

    // The pipeline for this Use Case
    final Pipeline uc4Pipeline = Pipeline.create();

    // Sources for this use case
    final StreamSource<Entry<Event, String>> configSource =
        KafkaSources.kafka(kafkaConfigPropsForPipeline, kafkaConfigurationTopic);

    final StreamSource<Entry<String, ActivePowerRecord>> inputSource =
        KafkaSources.kafka(kafkaInputReadPropsForPipeline, kafkaInputTopic);

    final StreamSource<Entry<String, AggregatedActivePowerRecord>> aggregationSource =
        KafkaSources.kafka(kafkaFeedbackPropsForPipeline, kafkaFeedbackTopic);

    // Extend UC4 topology to pipeline
    final StreamStage<Entry<String, AggregatedActivePowerRecord>> uc4Aggregation =
        this.extendUc4Topology(uc4Pipeline, inputSource, aggregationSource, configSource,
            windowSize);

    // Add Sink2: Write back to kafka feedback/aggregation topic
    uc4Aggregation.writeTo(KafkaSinks.kafka(
        kafkaWritePropsForPipeline, kafkaFeedbackTopic));

    // Log aggregation product
    uc4Aggregation.writeTo(Sinks.logger());

    // Add Sink2: Write back to kafka output topic
    uc4Aggregation.writeTo(KafkaSinks.kafka(
        kafkaWritePropsForPipeline, kafkaOutputTopic));

    // Return the pipeline
    return uc4Pipeline;
  }

  /**
   * Extends to a blank Hazelcast Jet Pipeline the UC4 topology defines by theodolite.
   *
   * <p>
   * UC4 takes {@code ActivePowerRecord} events from sensors and a {@code SensorRegistry} with maps
   * from keys to groups to map values to their according groups. A feedback stream allows for group
   * keys to be mapped to values and eventually to be mapped to other top level groups defines by
   * the {@code SensorRegistry}.
   * </p>
   *
   * <p>
   * 6 Step topology: <br>
   * (1) Inputs (Config, Values, Aggregations) <br>
   * (2) Merge Input Values and Aggregations <br>
   * (3) Join Configuration with Merged Input Stream <br>
   * (4) Duplicate as flatmap per value and group <br>
   * (5) Window (preparation for possible last values) <br>
   * (6) Aggregate data over the window
   * </p>
   *
   * @param pipe The blank pipeline to extend the logic to.
   * @param inputSource A streaming source with {@code ActivePowerRecord} data.
   * @param aggregationSource A streaming source with aggregated data.
   * @param configurationSource A streaming source delivering a {@code SensorRegistry}.
   * @param windowSize The window size used to aggregate over.
   * @return A {@code StreamSource<String,Double>} with sensorKeys or groupKeys mapped to their
   *         according aggregated values. The data can be further modified or directly be linked to
   *         a Hazelcast Jet sink.
   */
  public StreamStage<Entry<String, AggregatedActivePowerRecord>> extendUc4Topology(// NOPMD
      final Pipeline pipe,
      final StreamSource<Entry<String, ActivePowerRecord>> inputSource,
      final StreamSource<Entry<String, AggregatedActivePowerRecord>> aggregationSource,
      final StreamSource<Entry<Event, String>> configurationSource, final int windowSize) {

    //////////////////////////////////
    // (1) Configuration Stream
    pipe.readFrom(configurationSource)
        .withNativeTimestamps(0)
        .filter(entry -> entry.getKey() == Event.SENSOR_REGISTRY_CHANGED
            || entry.getKey() == Event.SENSOR_REGISTRY_STATUS)
        .map(data -> Util.entry(data.getKey(), SensorRegistry.fromJson(data.getValue())))
        .flatMapStateful(HashMap::new, new ConfigFlatMap())
        .writeTo(Sinks.mapWithUpdating(
            SENSOR_PARENT_MAP_NAME, // The addressed IMAP
            Entry::getKey, // The key to look for
            (oldValue, newEntry) -> newEntry.getValue()));

    //////////////////////////////////
    // (1) Sensor Input Stream
    final StreamStage<Entry<String, ActivePowerRecord>> inputStream = pipe
        .readFrom(inputSource)
        .withNativeTimestamps(0);

    //////////////////////////////////
    // (1) Aggregation Stream
    final StreamStage<Entry<String, ActivePowerRecord>> aggregations = pipe
        .readFrom(aggregationSource)
        .withNativeTimestamps(0)
        .map(entry -> { // Map Aggregated to ActivePowerRecord
          final AggregatedActivePowerRecord agg = entry.getValue();
          final ActivePowerRecord record = new ActivePowerRecord(
              agg.getIdentifier(), agg.getTimestamp(), agg.getSumInW());
          return Util.entry(entry.getKey(), record);
        });

    //////////////////////////////////
    // (2) UC4 Merge Input with aggregation stream
    final StreamStageWithKey<Entry<String, ActivePowerRecord>, String> mergedInputAndAggregations =
        inputStream
            .merge(aggregations)
            .groupingKey(Entry::getKey);

    //////////////////////////////////
    // (3) UC4 Join Configuration and Merges Input/Aggregation Stream
    // [sensorKey , (value,Set<Groups>)]
    final StreamStage<Entry<String, ValueGroup>> joinedStage = mergedInputAndAggregations
        .<Set<String>, Entry<String, ValueGroup>>mapUsingIMap(
            SENSOR_PARENT_MAP_NAME,
            (sensorEvent, sensorParentsSet) -> {
              // Check whether a groupset exists for a key or not
              if (sensorParentsSet == null) {
                // No group set exists for this key: return valuegroup with default null group set.
                final Set<String> nullSet = new HashSet<>();
                nullSet.add("NULL-GROUPSET");
                return Util.entry(sensorEvent.getKey(),
                    new ValueGroup(sensorEvent.getValue(), nullSet));
              } else {
                // Group set exists for this key: return valuegroup with the groupset.
                final ValueGroup valueParentsPair =
                    new ValueGroup(sensorEvent.getValue(), sensorParentsSet);
                // Return solution
                return Util.entry(sensorEvent.getKey(), valueParentsPair);
              }
            });

    //////////////////////////////////
    // (4) UC4 Duplicate as flatmap joined Stream
    // [(sensorKey, Group) , value]
    final StreamStage<Entry<SensorGroupKey, ActivePowerRecord>> dupliAsFlatmappedStage = joinedStage
        .flatMap(entry -> {

          // Supplied data
          final String keyGroupId = entry.getKey();
          final ActivePowerRecord record = entry.getValue().getRecord();
          final Set<String> groups = entry.getValue().getGroups();

          // Transformed Data
          final String[] groupList = groups.toArray(String[]::new);
          final SensorGroupKey[] newKeyList = new SensorGroupKey[groupList.length];
          final List<Entry<SensorGroupKey, ActivePowerRecord>> newEntryList = new ArrayList<>();
          for (int i = 0; i < groupList.length; i++) {
            newKeyList[i] = new SensorGroupKey(keyGroupId, groupList[i]);
            newEntryList.add(Util.entry(newKeyList[i], record));
          }

          // Return traversable list of new entry elements
          return Traversers.traverseIterable(newEntryList);
        });

    //////////////////////////////////
    // (5) UC4 Last Value Map
    // Table with tumbling window differentiation [ (sensorKey,Group) , value ],Time
    final StageWithWindow<Entry<SensorGroupKey, ActivePowerRecord>> windowedLastValues =
        dupliAsFlatmappedStage
            .window(WindowDefinition.tumbling(windowSize));

    final AggregateOperation1<Entry<SensorGroupKey, ActivePowerRecord>, AggregatedActivePowerRecordAccumulator, AggregatedActivePowerRecord> aggrOp = // NOCS
        AggregateOperation
            .withCreate(AggregatedActivePowerRecordAccumulator::new)
            .<Entry<SensorGroupKey, ActivePowerRecord>>andAccumulate((acc, rec) -> {
              acc.setId(rec.getKey().getGroup());
              acc.addInputs(rec.getValue());
            })
            .andCombine((acc, acc2) -> acc.addInputs(acc2.getId(), acc2.getSumInW(),
                acc2.getCount(), acc.getTimestamp()))
            .andDeduct((acc, acc2) -> acc.removeInputs(acc2.getSumInW(), acc2.getCount()))
            .andExportFinish(acc -> new AggregatedActivePowerRecord(acc.getId(),
                acc.getTimestamp(),
                acc.getCount(),
                acc.getSumInW(),
                acc.getAverageInW()));

    // write aggregation back to kafka

    return windowedLastValues
        .groupingKey(entry -> entry.getKey().getGroup())
        .aggregate(aggrOp).map(agg -> Util.entry(agg.getKey(), agg.getValue()));
  }



  /**
   * FlatMap function used to process the configuration input for UC4.
   */
  private static class ConfigFlatMap implements
      BiFunctionEx<Map<String, Set<String>>, Entry<Event, SensorRegistry>, Traverser<Entry<String, Set<String>>>> { // NOCS

    private static final long serialVersionUID = -6769931374907428699L;

    @Override
    public Traverser<Entry<String, Set<String>>> applyEx(
        final Map<String, Set<String>> flatMapStage,
        final Entry<Event, SensorRegistry> eventItem) {
      // Transform new Input
      final ChildParentsTransformer transformer = new ChildParentsTransformer("default-name");
      final Map<String, Set<String>> mapFromRegistry =
          transformer.constructChildParentsPairs(eventItem.getValue());

      // Compare both tables
      final Map<String, Set<String>> updates = new HashMap<>();
      for (final String key : mapFromRegistry.keySet()) {
        if (flatMapStage.containsKey(key)) {
          if (!mapFromRegistry.get(key).equals(flatMapStage.get(key))) {
            updates.put(key, mapFromRegistry.get(key));
          }
        } else {
          updates.put(key, mapFromRegistry.get(key));
        }
      }

      // Create a updates list to pass onto the next pipeline stage-
      final List<Entry<String, Set<String>>> updatesList = new ArrayList<>(updates.entrySet());

      // Return traverser with updates list.
      return Traversers.traverseIterable(updatesList)
          .map(e -> Util.entry(e.getKey(), e.getValue()));
    }

  }

}
