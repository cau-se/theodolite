package theodolite.commons.workloadgeneration;

import com.google.api.core.ApiFuture;
import com.google.api.gax.core.CredentialsProvider;
import com.google.api.gax.core.NoCredentialsProvider;
import com.google.api.gax.grpc.GrpcTransportChannel;
import com.google.api.gax.rpc.FixedTransportChannelProvider;
import com.google.api.gax.rpc.TransportChannelProvider;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.protobuf.util.Timestamps;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends monitoring records to Pub/Sub.
 *
 * @param <T> Record type to send
 */
public class PubSubRecordSender<T> implements RecordSender<T> {

  private static final int SHUTDOWN_TIMEOUT_SEC = 5;

  private static final Logger LOGGER = LoggerFactory.getLogger(KafkaRecordSender.class);

  private final Function<T, ByteBuffer> recordSerializer;

  private final Function<T, Long> timestampAccessor;

  private final Function<T, String> orderingKeyAccessor;

  private final Publisher publisher;

  private PubSubRecordSender(final Builder<T> builder) {
    this.orderingKeyAccessor = builder.orderingKeyAccessor;
    this.timestampAccessor = builder.timestampAccessor;
    this.recordSerializer = builder.recordSerializer;

    try {
      this.publisher = builder.buildPublisher();
    } catch (final IOException e) {
      throw new IllegalStateException("Can not create Pub/Sub publisher.", e);
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
    final PubsubMessage message = messageBuilder.build();
    LOGGER.debug("Send message to PubSub topic {}: {}", this.publisher.getTopicName(), message);
    final ApiFuture<String> publishResult = this.publisher.publish(message);
    if (LOGGER.isDebugEnabled()) {
      try {
        LOGGER.debug("Publishing result is {}.", publishResult.get());
      } catch (InterruptedException | ExecutionException e) {
        LOGGER.warn("Can not get publishing result.", e);
      }
    }
  }

  /**
   * Creates a {@link Builder} object for a {@link PubSubRecordSender}.
   *
   * @param project The project where to write.
   * @param topic The topic where to write.
   * @param recordSerializer A function serializing objects to {@link ByteBuffer}.
   */
  public static <T> Builder<T> builderForProject(
      final String project,
      final String topic,
      final Function<T, ByteBuffer> recordSerializer) {
    return new Builder<>(project, topic, recordSerializer);
  }

  /**
   * Creates a {@link Builder} object for a {@link PubSubRecordSender}.
   *
   * @param emulatorHost Host of the emulator.
   * @param topic The topic where to write.
   * @param recordSerializer A function serializing objects to {@link ByteBuffer}.
   */
  public static <T> Builder<T> builderForEmulator(
      final String emulatorHost,
      final String topic,
      final Function<T, ByteBuffer> recordSerializer) {
    return new WithEmulatorBuilder<>(emulatorHost, topic, recordSerializer);
  }

  /**
   * Builder class to build a new {@link PubSubRecordSender}.
   *
   * @param <T> Type of the records that should later be send.
   */
  public static class Builder<T> {

    protected final TopicName topicName;
    private final Function<T, ByteBuffer> recordSerializer; // NOPMD
    private Function<T, Long> timestampAccessor = null; // NOPMD
    private Function<T, String> orderingKeyAccessor = null; // NOPMD

    /**
     * Creates a Builder object for a {@link PubSubRecordSender}.
     *
     * @param topic The topic where to write.
     * @param recordSerializer A function serializing objects to {@link ByteBuffer}.
     */
    private Builder(
        final String project,
        final String topic,
        final Function<T, ByteBuffer> recordSerializer) {
      this.topicName = TopicName.of(project, topic);
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

    protected Publisher buildPublisher() throws IOException {
      return Publisher.newBuilder(this.topicName).build();
    }

  }

  private static class WithEmulatorBuilder<T> extends Builder<T> {

    private static final String DUMMY_PROJECT = "dummy-project-id";

    private final String emulatorHost;

    /**
     * Creates a Builder object for a {@link PubSubRecordSender}.
     *
     * @param emulatorHost host of the emulator.
     * @param topic The topic where to write.
     * @param recordSerializer A function serializing objects to {@link ByteBuffer}.
     */
    private WithEmulatorBuilder(
        final String emulatorHost,
        final String topic,
        final Function<T, ByteBuffer> recordSerializer) {
      super(DUMMY_PROJECT, topic, recordSerializer);
      this.emulatorHost = emulatorHost;
    }

    @Override
    protected Publisher buildPublisher() throws IOException {
      final ManagedChannel channel = ManagedChannelBuilder
          .forTarget(this.emulatorHost)
          .usePlaintext()
          .build();

      final TransportChannelProvider channelProvider = FixedTransportChannelProvider
          .create(GrpcTransportChannel.create(channel));
      final CredentialsProvider credentialsProvider = NoCredentialsProvider.create();

      return Publisher.newBuilder(super.topicName)
          .setChannelProvider(channelProvider)
          .setCredentialsProvider(credentialsProvider)
          .build();
    }

  }

}
