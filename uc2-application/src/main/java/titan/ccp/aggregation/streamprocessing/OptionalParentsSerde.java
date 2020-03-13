package titan.ccp.aggregation.streamprocessing;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.apache.kafka.common.serialization.Serde;
import titan.ccp.common.kafka.simpleserdes.BufferSerde;
import titan.ccp.common.kafka.simpleserdes.ReadBuffer;
import titan.ccp.common.kafka.simpleserdes.SimpleSerdes;
import titan.ccp.common.kafka.simpleserdes.WriteBuffer;

/**
 * {@link Serde} factory for an optional {@link Set} of parent identifiers.
 */
public final class OptionalParentsSerde implements BufferSerde<Optional<Set<String>>> {

  private OptionalParentsSerde() {}

  @Override
  public void serialize(final WriteBuffer buffer, final Optional<Set<String>> data) {
    if (data.isPresent()) {
      buffer.putByte((byte) 1);
      final Set<String> parents = data.get();
      buffer.putInt(parents.size());
      for (final String parent : parents) {
        buffer.putString(parent);
      }
    } else {
      buffer.putByte((byte) 0);
    }
  }

  @Override
  public Optional<Set<String>> deserialize(final ReadBuffer buffer) {
    if (buffer.getByte() == 0) {
      return Optional.empty();
    }

    final int size = buffer.getInt();
    final Set<String> parents = new HashSet<>(size);
    for (int i = 0; i < size; i++) {
      final String parent = buffer.getString();
      parents.add(parent);
    }
    return Optional.of(parents);
  }

  public static Serde<Optional<Set<String>>> serde() {
    return SimpleSerdes.create(new OptionalParentsSerde());
  }

}
