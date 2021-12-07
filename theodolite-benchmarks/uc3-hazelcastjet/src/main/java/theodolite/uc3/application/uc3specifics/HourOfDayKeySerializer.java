package theodolite.uc3.application.uc3specifics;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;
import java.io.IOException;

/** A pipeline serializer for the HourOfDayKey to allow for parallelization. */
public class HourOfDayKeySerializer implements StreamSerializer<HourOfDayKey> {

  private static final int TYPE_ID = 1;
  
  @Override
  public int getTypeId() {
    return TYPE_ID;
  }

  @Override
  public void write(final ObjectDataOutput out, final HourOfDayKey key) throws IOException {
    out.writeInt(key.getHourOfDay());
    out.writeUTF(key.getSensorId()); 
  }

  @Override
  public HourOfDayKey read(final ObjectDataInput in) throws IOException {
    return new HourOfDayKey(in.readInt(), in.readUTF());
  }

}
