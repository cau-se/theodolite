package theodolite.uc4.application.uc4specifics;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

/** A pipeline serializer for the HourOfDayKey to allow for parallelization. */
public class ValueGroupSerializer implements StreamSerializer<ValueGroup> {

  private static final int TYPE_ID = 1;

  @Override
  public int getTypeId() {
    return TYPE_ID;
  }

  @Override
  public void write(final ObjectDataOutput out, final ValueGroup key) throws IOException {
    out.writeDouble(key.getValueInW());
    out.writeString(String.join(",", key.getGroups()));
  }

  @Override
  public ValueGroup read(final ObjectDataInput in) throws IOException {
    return new ValueGroup(in.readDouble(),
        new HashSet<>(Arrays.asList(in.readString().split(","))));
  }

}

