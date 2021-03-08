package theodolite.uc3.application.util;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.apache.kafka.common.serialization.Serde;
import titan.ccp.common.kafka.simpleserdes.BufferSerde;
import titan.ccp.common.kafka.simpleserdes.ReadBuffer;
import titan.ccp.common.kafka.simpleserdes.SimpleSerdes;
import titan.ccp.common.kafka.simpleserdes.WriteBuffer;

import java.io.Serializable;

/**
 * {@link BufferSerde} for a {@link HourOfDayKey}. Use the {@link #create()} method to create a new
 * Kafka {@link Serde}.
 */
public class HourOfDayKeySerde extends Serializer<HourOfDayKey> implements BufferSerde<HourOfDayKey>, Serializable {

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
  public void write(Kryo kryo, Output output, HourOfDayKey object) {
    byte[] data = object.toByteArray();
    output.writeInt(data.length);
    output.writeBytes(data);
  }

  @Override
  public HourOfDayKey read(Kryo kryo, Input input, Class<HourOfDayKey> type) {
    final int numBytes = input.readInt();
    return HourOfDayKey.fromByteArray(input.readBytes(numBytes));
  }
}
