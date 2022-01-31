package theodolite.commons.workloadgeneration;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.protobuf.util.Timestamps;
import com.google.pubsub.v1.PubsubMessage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import titan.ccp.common.kafka.avro.SchemaRegistryAvroSerdeFactory;

/**
 * Sends monitoring records to Kafka.
 *
 * @param <T> {@link SpecificRecord} to send
 */
public class PubSubRecordSender<T extends SpecificRecord> implements RecordSender<T> {

  private static final int SHUTDOWN_TIMEOUT_SEC = 5;

  private static final Logger LOGGER = LoggerFactory.getLogger(KafkaRecordSender.class);

  private final String topic;

  private final Function<T, String> keyAccessor;

  private final Function<T, Long> timestampAccessor;

  private final Function<T, ByteBuffer> recordSerializer;

  private final Producer<String, T> producer; // TODO remove

  private final Publisher publisher;

  /**
   * Create a new {@link KafkaRecordSender}.
   */
  private PubSubRecordSender(final Builder<T> builder) {
    this.topic = builder.topic;
    this.keyAccessor = builder.keyAccessor;
    this.timestampAccessor = builder.timestampAccessor;
    this.recordSerializer = builder.recordSerializer;

    final Properties properties = new Properties();
    properties.putAll(builder.defaultProperties);
    properties.put("bootstrap.servers", builder.bootstrapServers);
    // properties.put("acks", this.acknowledges);
    // properties.put("batch.size", this.batchSize);
    // properties.put("linger.ms", this.lingerMs);
    // properties.put("buffer.memory", this.bufferMemory);

    final SchemaRegistryAvroSerdeFactory avroSerdeFactory =
        new SchemaRegistryAvroSerdeFactory(builder.schemaRegistryUrl);
    this.producer = new KafkaProducer<>(
        properties,
        new StringSerializer(),
        avroSerdeFactory.<T>forKeys().serializer());

    try {
      this.publisher = Publisher.newBuilder(this.topic).build();
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      // e.printStackTrace();
      throw new IllegalStateException(e);
    }
  }

  /**
   * Write the passed monitoring record to Kafka.
   */
  public void write(final T record) {

    // TODO fix this
    final ByteBuffer byteBuffer = this.recordSerializer.apply(record);
    // try {
    // byteBuffer = ((ActivePowerRecord) monitoringRecord).toByteBuffer();
    // } catch (final IOException e1) {
    // // TODO Auto-generated catch block
    // e1.printStackTrace();
    // throw new IllegalStateException(e1);
    // }
    final ByteString data = ByteString.copyFrom(byteBuffer);

    final PubsubMessage message = PubsubMessage.newBuilder()
        .setOrderingKey(this.keyAccessor.apply(record))
        .setPublishTime(Timestamps.fromMillis(this.timestampAccessor.apply(record)))
        .setData(data)
        .build();
    this.publisher.publish(message);
    LOGGER.debug("Send message to PubSub topic {}: {}", this.topic, message);
  }

  public void terminate() {
    this.publisher.shutdown();
    try {
      this.publisher.awaitTermination(SHUTDOWN_TIMEOUT_SEC, TimeUnit.SECONDS);
    } catch (final InterruptedException e) {
      throw new IllegalStateException(e);
    }
    this.producer.close();
  }

  @Override
  public void send(final T message) {
    this.write(message);
  }

  public static <T extends SpecificRecord> Builder<T> builder(
      final String bootstrapServers,
      final String topic,
      final String schemaRegistryUrl) {
    return new Builder<>(bootstrapServers, topic, schemaRegistryUrl);
  }

  /**
   * Builder class to build a new {@link KafkaRecordSender}.
   *
   * @param <T> Type of the records that should later be send.
   */
  public static class Builder<T extends SpecificRecord> {

    private final String bootstrapServers;
    private final String topic;
    private final String schemaRegistryUrl;
    private Function<T, String> keyAccessor = x -> ""; // NOPMD
    private Function<T, Long> timestampAccessor = x -> null; // NOPMD
    // TODO
    private Function<T, ByteBuffer> recordSerializer = null; // NOPMD
    private Properties defaultProperties = new Properties(); // NOPMD

    /**
     * Creates a Builder object for a {@link KafkaRecordSender}.
     *
     * @param bootstrapServers The Server to for accessing Kafka.
     * @param topic The topic where to write.
     * @param schemaRegistryUrl URL to the schema registry for avro.
     */
    private Builder(final String bootstrapServers, final String topic,
        final String schemaRegistryUrl) {
      this.bootstrapServers = bootstrapServers;
      this.topic = topic;
      this.schemaRegistryUrl = schemaRegistryUrl;
    }

    public Builder<T> keyAccessor(final Function<T, String> keyAccessor) {
      this.keyAccessor = keyAccessor;
      return this;
    }

    public Builder<T> timestampAccessor(final Function<T, Long> timestampAccessor) {
      this.timestampAccessor = timestampAccessor;
      return this;
    }

    public Builder<T> recordSerializer(final Function<T, ByteBuffer> recordSerializer) {
      this.recordSerializer = recordSerializer;
      return this;
    }

    public Builder<T> defaultProperties(final Properties defaultProperties) {
      this.defaultProperties = defaultProperties;
      return this;
    }

    public PubSubRecordSender<T> build() {
      return new PubSubRecordSender<>(this);
    }
  }

}
