package rocks.theodolite.benchmarks.uc4.flink.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.io.Serializable;
import java.util.Set;

/**
 * A {@link Serializer} for serializing arbitrary {@link Set}s of {@link Object}s.
 */
public final class ImmutableSetSerializer extends Serializer<Set<Object>> implements Serializable {

  private static final long serialVersionUID = 6919877826110724620L; // NOPMD

  public ImmutableSetSerializer() {
    super(false, true);
  }

  @Override
  public void write(final Kryo kryo, final Output output, final Set<Object> object) {
    output.writeInt(object.size(), true);
    for (final Object elm : object) {
      kryo.writeClassAndObject(output, elm);
    }
  }

  @Override
  public Set<Object> read(final Kryo kryo, final Input input, final Class<Set<Object>> type) {
    final int size = input.readInt(true);
    final Object[] list = new Object[size];
    for (int i = 0; i < size; ++i) {
      list[i] = kryo.readClassAndObject(input);
    }
    return Set.of(list);
  }

  /**
   * Creates a new {@link ImmutableSetSerializer} and registers its serializer for the several
   * related classes.
   *
   * @param kryo the {@link Kryo} instance to set the serializer on
   */
  public static void registerSerializers(final Kryo kryo) {
    final ImmutableSetSerializer serializer = new ImmutableSetSerializer();
    kryo.register(Set.of().getClass(), serializer);
    kryo.register(Set.of(1).getClass(), serializer);
    kryo.register(Set.of(1, 2, 3, 4).getClass(), serializer); // NOCS
  }
}
