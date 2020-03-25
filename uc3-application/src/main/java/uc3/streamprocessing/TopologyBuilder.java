package uc3.streamprocessing;

import com.google.gson.Gson;
import java.time.Duration;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import titan.ccp.common.kieker.kafka.IMonitoringRecordSerde;
import titan.ccp.models.records.ActivePowerRecordFactory;

/**
 * Builds Kafka Stream Topology for the History microservice.
 */
public class TopologyBuilder {

	private static final Logger LOGGER = LoggerFactory.getLogger(TopologyBuilder.class);

	private final String inputTopic;
	private final String outputTopic;
	private final Duration duration;
	private final Gson gson;

	private final StreamsBuilder builder = new StreamsBuilder();

	/**
	 * Create a new {@link TopologyBuilder} using the given topics.
	 */
	public TopologyBuilder(final String inputTopic, final String outputTopic, final Duration duration) {
		this.inputTopic = inputTopic;
		this.outputTopic = outputTopic;
		this.duration = duration;
		this.gson = new Gson();
	}

	/**
	 * Build the {@link Topology} for the History microservice.
	 */
	public Topology build() {
		this.builder
				.stream(this.inputTopic,
						Consumed.with(Serdes.String(), IMonitoringRecordSerde.serde(new ActivePowerRecordFactory())))
				.groupByKey().windowedBy(TimeWindows.of(this.duration))
				.aggregate(() -> 0.0, (key, activePowerRecord, agg) -> agg + activePowerRecord.getValueInW(),
						Materialized.with(Serdes.String(), Serdes.Double()))
				.toStream().peek((k, v) -> System.out.printf("key %s, value %f \n", k, v)).to(this.outputTopic);

		return this.builder.build();
	}
}
