package rocks.theodolite.benchmarks.loadgenerator;

/**
 * This interface describes a function that consumes a message {@code T}. This function is dedicated
 * to be used to transport individual messages to the messaging system.
 *
 * @param <T> the type of records to send as messages.
 */
@FunctionalInterface
public interface RecordSender<T> {

  void send(final T message);

}
