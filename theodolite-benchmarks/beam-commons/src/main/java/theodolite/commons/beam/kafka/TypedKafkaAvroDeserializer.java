package theodolite.commons.beam.kafka;

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import java.util.Map;
import org.apache.avro.Schema;
import org.apache.kafka.common.serialization.Deserializer;

public class TypedKafkaAvroDeserializer<T> implements Deserializer<T> {

	private final KafkaAvroDeserializer deserializer;

	public TypedKafkaAvroDeserializer() {
		this.deserializer = new KafkaAvroDeserializer();
	}

	public TypedKafkaAvroDeserializer(SchemaRegistryClient client) {
		this.deserializer = new KafkaAvroDeserializer(client);
	}

	public TypedKafkaAvroDeserializer(SchemaRegistryClient client, Map<String, ?> props) {
		this.deserializer = new KafkaAvroDeserializer(client, props);
	}

	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {

	}

	@SuppressWarnings("unchecked")
	@Override
	public T deserialize(String s, byte[] bytes) {
		return (T) this.deserializer.deserialize(s, bytes);
	}

	/**
	 * Pass a reader schema to get an Avro projection
	 */
	@SuppressWarnings("unchecked")
	public T deserialize(String topic, byte[] bytes, Schema readerSchema) {
		return (T) this.deserializer.deserialize(topic, bytes, readerSchema);
	}

	@Override
	public void close() {
		this.deserializer.close();
	}

}
