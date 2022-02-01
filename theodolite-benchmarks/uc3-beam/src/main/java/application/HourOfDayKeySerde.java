package application;

import org.apache.kafka.common.serialization.Serde;
import titan.ccp.common.kafka.simpleserdes.BufferSerde;
import titan.ccp.common.kafka.simpleserdes.ReadBuffer;
import titan.ccp.common.kafka.simpleserdes.SimpleSerdes;
import titan.ccp.common.kafka.simpleserdes.WriteBuffer;

/**
 * {@link BufferSerde} for a {@link HourOfDayKey}. Use the {@link #create()} method to create a new
 * Kafka {@link Serde}.
 */
public class HourOfDayKeySerde implements BufferSerde<HourOfDayKey> {

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

}
