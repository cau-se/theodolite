package theodolite.commons.workloadgeneration;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.protobuf.util.Timestamps;
import com.google.pubsub.v1.PubsubMessage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.apache.avro.specific.SpecificRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends monitoring records to Kafka.
 *
 * @param <T> {@link SpecificRecord} to send
 */
public class PubSubRecordSender<T> implements RecordSender<T> {

  private static final int SHUTDOWN_TIMEOUT_SEC = 5;

  private static final Logger LOGGER = LoggerFactory.getLogger(KafkaRecordSender.class);

  private final String topic;

  private final Function<T, ByteBuffer> recordSerializer;

  private final Function<T, Long> timestampAccessor;

  private final Function<T, String> orderingKeyAccessor;

  private final Publisher publisher;

  /**
   * Create a new {@link KafkaRecordSender}.
   */
  private PubSubRecordSender(final Builder<T> builder) {
    this.topic = builder.topic;
    this.orderingKeyAccessor = builder.orderingKeyAccessor;
    this.timestampAccessor = builder.timestampAccessor;
    this.recordSerializer = builder.recordSerializer;

    try {
      this.publisher = Publisher.newBuilder(this.topic).build();
    } catch (final IOException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Terminate this {@link PubSubRecordSender} and shutdown the underlying {@link Publisher}.
   */
  public void terminate() {
    this.publisher.shutdown();
    try {
      this.publisher.awaitTermination(SHUTDOWN_TIMEOUT_SEC, TimeUnit.SECONDS);
    } catch (final InterruptedException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public void send(final T record) {
    final ByteBuffer byteBuffer = this.recordSerializer.apply(record);
    final ByteString data = ByteString.copyFrom(byteBuffer);

    final PubsubMessage.Builder messageBuilder = PubsubMessage.newBuilder().setData(data);
    if (this.orderingKeyAccessor != null) {
      messageBuilder.setOrderingKey(this.orderingKeyAccessor.apply(record));
    }
    if (this.timestampAccessor != null) {
      messageBuilder.setPublishTime(Timestamps.fromMillis(this.timestampAccessor.apply(record)));
    }
    this.publisher.publish(messageBuilder.build());
    LOGGER.debug("Send message to PubSub topic {}: {}", this.topic, messageBuilder);
  }

  public static <T> Builder<T> builder(
      final String topic,
      final Function<T, ByteBuffer> recordSerializer) {
    return new Builder<>(topic, recordSerializer);
  }

  /**
   * Builder class to build a new {@link PubSubRecordSender}.
   *
   * @param <T> Type of the records that should later be send.
   */
  public static class Builder<T> {

    private final String topic;
    private final Function<T, ByteBuffer> recordSerializer; // NOPMD
    private Function<T, Long> timestampAccessor = null; // NOPMD
    private Function<T, String> orderingKeyAccessor = null; // NOPMD

    /**
     * Creates a Builder object for a {@link PubSubRecordSender}.
     *
     * @param topic The topic where to write.
     * @param recordSerializer A function serializing objects to {@link ByteBuffer}.
     */
    private Builder(final String topic, final Function<T, ByteBuffer> recordSerializer) {
      this.topic = topic;
      this.recordSerializer = recordSerializer;
    }

    public Builder<T> timestampAccessor(final Function<T, Long> timestampAccessor) {
      this.timestampAccessor = timestampAccessor;
      return this;
    }

    public Builder<T> orderingKeyAccessor(final Function<T, String> keyAccessor) {
      this.orderingKeyAccessor = keyAccessor;
      return this;
    }

    public PubSubRecordSender<T> build() {
      return new PubSubRecordSender<>(this);
    }
  }

}
