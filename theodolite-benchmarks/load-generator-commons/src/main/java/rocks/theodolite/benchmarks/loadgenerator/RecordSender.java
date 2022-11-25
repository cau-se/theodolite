package rocks.theodolite.benchmarks.loadgenerator;

import java.io.Closeable;

/**
 * This interface describes a function that consumes a message {@code T}. This function is dedicated
 * to be used to transport individual messages to the messaging system.
 *
 * @param <T> the type of records to send as messages.
 */
@FunctionalInterface
public interface RecordSender<T> extends Closeable {

  void send(final T message);

  @Override
  default void close() {
    // Nothing to do per default
  }

}
