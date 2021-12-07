package theodolite.uc2.application.uc2specifics;

import com.google.common.math.Stats;
import com.google.common.math.StatsAccumulator;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;
import java.io.IOException;

/**
 * A serializer and deserializer for the StatsAccumulator which is used in the UC2 implementation
 * using Hazelcast Jet.
 */
public class StatsAccumulatorSerializer implements StreamSerializer<StatsAccumulator> {

  private static final int TYPE_ID = 69420;

  @Override
  public int getTypeId() {
    // TODO Auto-generated method stub
    return TYPE_ID;
  }

  @Override
  public void write(final ObjectDataOutput out, final StatsAccumulator object) throws IOException {
    final byte[] byteArray = object.snapshot().toByteArray();
    out.writeByteArray(byteArray);
  }

  @Override
  public StatsAccumulator read(final ObjectDataInput in) throws IOException {
    final byte[] byteArray = in.readByteArray();
    final Stats deserializedStats = Stats.fromByteArray(byteArray);
    final StatsAccumulator accumulator = new StatsAccumulator();
    accumulator.addAll(deserializedStats);
    return accumulator;
  }

}
