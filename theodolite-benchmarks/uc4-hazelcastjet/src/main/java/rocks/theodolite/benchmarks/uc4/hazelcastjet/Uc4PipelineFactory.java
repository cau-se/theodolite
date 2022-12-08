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
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import rocks.theodolite.benchmarks.commons.configuration.events.Event;
import rocks.theodolite.benchmarks.commons.hazelcastjet.PipelineFactory;
import rocks.theodolite.benchmarks.commons.model.records.ActivePowerRecord;
import rocks.theodolite.benchmarks.commons.model.records.AggregatedActivePowerRecord;
import rocks.theodolite.benchmarks.commons.model.sensorregistry.SensorRegistry;


/**
 * PipelineFactory for use case 4. Allows to build and extend pipelines.
 */
public class Uc4PipelineFactory extends PipelineFactory {

  private static final String SENSOR_PARENT_MAP_NAME = "SensorParentMap";

  private final Properties kafkaConfigPropsForPipeline;
  private final Properties kafkaFeedbackPropsForPipeline;

  private final String kafkaConfigurationTopic;
  private final String kafkaFeedbackTopic;

  private final Duration emitPeriod;

  private final Duration gracePeriod;


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
   * @param emitPeriod The window size of the tumbling window used in this pipeline.
   */
  public Uc4PipelineFactory(final Properties kafkaInputReadPropsForPipeline, // NOPMD
      final Properties kafkaConfigPropsForPipeline,
      final Properties kafkaFeedbackPropsForPipeline,
      final Properties kafkaWritePropsForPipeline,
      final String kafkaInputTopic,
      final String kafkaOutputTopic,
      final String kafkaConfigurationTopic,
      final String kafkaFeedbackTopic,
      final Duration emitPeriod,
      final Duration gracePeriod) {

    super(kafkaInputReadPropsForPipeline, kafkaInputTopic,
        kafkaWritePropsForPipeline, kafkaOutputTopic);
    this.kafkaConfigPropsForPipeline = kafkaConfigPropsForPipeline;
    this.kafkaFeedbackPropsForPipeline = kafkaFeedbackPropsForPipeline;
    this.kafkaConfigurationTopic = kafkaConfigurationTopic;
    this.kafkaFeedbackTopic = kafkaFeedbackTopic;
    this.emitPeriod = emitPeriod;
    this.gracePeriod = gracePeriod;
  }

  /**
   * Builds a pipeline which can be used for stream processing using Hazelcast Jet.
   *
   * @return a pipeline used which can be used in a Hazelcast Jet Instance to process data for UC4.
   */
  @Override
  public Pipeline buildPipeline() {

    // Sources for this use case
    final StreamSource<Entry<Event, String>> configSource =
        KafkaSources.kafka(this.kafkaConfigPropsForPipeline, this.kafkaConfigurationTopic);

    final StreamSource<Entry<String, ActivePowerRecord>> inputSource =
        KafkaSources.kafka(this.kafkaReadPropsForPipeline, this.kafkaInputTopic);

    final StreamSource<Entry<String, AggregatedActivePowerRecord>> aggregationSource =
        KafkaSources.kafka(this.kafkaFeedbackPropsForPipeline, this.kafkaFeedbackTopic);

    // Extend UC4 topology to pipeline
    final StreamStage<Entry<String, AggregatedActivePowerRecord>> uc4Aggregation =
        this.extendUc4Topology(inputSource, aggregationSource, configSource);

    // Add Sink2: Write back to kafka feedback/aggregation topic
    uc4Aggregation.writeTo(KafkaSinks.kafka(
        this.kafkaWritePropsForPipeline, this.kafkaFeedbackTopic));

    // Log aggregation product
    // uc4Aggregation.writeTo(Sinks.logger());

    // Add Sink2: Write back to kafka output topic
    uc4Aggregation.writeTo(KafkaSinks.kafka(
        this.kafkaWritePropsForPipeline, this.kafkaOutputTopic));

    // Return the pipeline
    return this.pipe;
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
   * @param inputSource A streaming source with {@code ActivePowerRecord} data.
   * @param aggregationSource A streaming source with aggregated data.
   * @param configurationSource A streaming source delivering a {@code SensorRegistry}.
   * @return A {@code StreamSource<String,Double>} with sensorKeys or groupKeys mapped to their
   *         according aggregated values. The data can be further modified or directly be linked to
   *         a Hazelcast Jet sink.
   */
  public StreamStage<Map.Entry<String, AggregatedActivePowerRecord>> extendUc4Topology(// NOPMD
      final StreamSource<Map.Entry<String, ActivePowerRecord>> inputSource,
      final StreamSource<Map.Entry<String, AggregatedActivePowerRecord>> aggregationSource,
      final StreamSource<Map.Entry<Event, String>> configurationSource) {

    //////////////////////////////////
    // (1) Configuration Stream
    this.pipe.readFrom(configurationSource)
        .withNativeTimestamps(this.gracePeriod.toMillis())
        .filter(entry -> entry.getKey() == Event.SENSOR_REGISTRY_CHANGED
            || entry.getKey() == Event.SENSOR_REGISTRY_STATUS)
        .map(data -> Util.entry(data.getKey(), SensorRegistry.fromJson(data.getValue())))
        .flatMapStateful(HashMap::new, new ConfigFlatMap())
        .writeTo(Sinks.mapWithUpdating(
            SENSOR_PARENT_MAP_NAME, // The addressed IMap
            Entry::getKey, // The key to look for
            (oldValue, newEntry) -> newEntry.getValue()));

    //////////////////////////////////
    // (1) Sensor Input Stream
    final StreamStage<Entry<String, ActivePowerRecord>> inputStream = this.pipe
        .readFrom(inputSource)
        .withNativeTimestamps(this.gracePeriod.toMillis());

    //////////////////////////////////
    // (1) Aggregation Stream
    final StreamStage<Entry<String, ActivePowerRecord>> aggregations = this.pipe
        .readFrom(aggregationSource)
        .withNativeTimestamps(this.gracePeriod.toMillis())
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
              final ValueGroup valueParentsPair = new ValueGroup(
                  sensorEvent.getValue(),
                  sensorParentsSet == null ? Set.of() : sensorParentsSet);
              return Util.entry(sensorEvent.getKey(), valueParentsPair);
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

          // Return traversable list of new entry elements
          return Traversers.traverseStream(
              groups
                  .stream()
                  .map(group -> Util.entry(new SensorGroupKey(keyGroupId, group), record)));
        });

    //////////////////////////////////
    // (5) UC4 Last Value Map
    // Table with tumbling window differentiation [ (sensorKey,Group) , value ],Time
    final StageWithWindow<Entry<SensorGroupKey, ActivePowerRecord>> windowedLastValues =
        dupliAsFlatmappedStage.window(WindowDefinition.tumbling(this.emitPeriod.toMillis()));

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
        .aggregate(aggrOp)
        .map(agg -> Util.entry(agg.getKey(), agg.getValue()));
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
      final ChildParentsTransformer transformer = new ChildParentsTransformer();
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
