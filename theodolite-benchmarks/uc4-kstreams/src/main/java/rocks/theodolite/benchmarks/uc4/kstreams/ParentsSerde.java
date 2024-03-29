package rocks.theodolite.benchmarks.uc4.kstreams;

import java.util.HashSet;
import java.util.Set;
import org.apache.kafka.common.serialization.Serde;
import rocks.theodolite.benchmarks.commons.kafka.simpleserdes.BufferSerde;
import rocks.theodolite.benchmarks.commons.kafka.simpleserdes.ReadBuffer;
import rocks.theodolite.benchmarks.commons.kafka.simpleserdes.SimpleSerdes;
import rocks.theodolite.benchmarks.commons.kafka.simpleserdes.WriteBuffer;

/**
 * {@link Serde} factory for {@link Set} of parent identifiers.
 */
public final class ParentsSerde implements BufferSerde<Set<String>> {

  private ParentsSerde() {}

  @Override
  public void serialize(final WriteBuffer buffer, final Set<String> parents) {
    buffer.putInt(parents.size());
    for (final String parent : parents) {
      buffer.putString(parent);
    }
  }

  @Override
  public Set<String> deserialize(final ReadBuffer buffer) {
    final int size = buffer.getInt();
    final Set<String> parents = new HashSet<>(size);
    for (int i = 0; i < size; i++) {
      final String parent = buffer.getString();
      parents.add(parent);
    }
    return parents;
  }

  public static Serde<Set<String>> serde() {
    return SimpleSerdes.create(new ParentsSerde());
  }

}
