package rocks.theodolite.benchmarks.uc3.flink.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import java.io.Serializable;
import org.apache.kafka.common.serialization.Serde;
import titan.ccp.common.kafka.simpleserdes.BufferSerde;
import titan.ccp.common.kafka.simpleserdes.ReadBuffer;
import titan.ccp.common.kafka.simpleserdes.SimpleSerdes;
import titan.ccp.common.kafka.simpleserdes.WriteBuffer;

/**
 * {@link BufferSerde} for a {@link HourOfDayKey}. Use the {@link #create()} method to create a new
 * Kafka {@link Serde}.
 */
public class HourOfDayKeySerde extends Serializer<HourOfDayKey>
    implements BufferSerde<HourOfDayKey>, Serializable {

  private static final long serialVersionUID = 1262778284661945041L; // NOPMD

  @Override
  public void serialize(final WriteBuffer buffer, final HourOfDayKey data) {
    buffer.putInt(data.getHourOfDay());
    buffer.putString(data.getSensorId());
  }

  @Override
  public HourOfDayKey deserialize(final ReadBuffer buffer) {
    final int hourOfDay = buffer.getInt();
    final String sensorId = buffer.getString();
    return new HourOfDayKey(hourOfDay, sensorId);
  }

  public static Serde<HourOfDayKey> create() {
    return SimpleSerdes.create(new HourOfDayKeySerde());
  }

  @Override
  public void write(final Kryo kryo, final Output output, final HourOfDayKey object) {
    final byte[] data = object.toByteArray();
    output.writeInt(data.length);
    output.writeBytes(data);
  }

  @Override
  public HourOfDayKey read(final Kryo kryo, final Input input, final Class<HourOfDayKey> type) {
    final int numBytes = input.readInt();
    return HourOfDayKey.fromByteArray(input.readBytes(numBytes));
  }
}
