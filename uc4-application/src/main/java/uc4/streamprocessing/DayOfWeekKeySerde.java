package uc4.streamprocessing;

import java.time.DayOfWeek;
import org.apache.kafka.common.serialization.Serde;
import titan.ccp.common.kafka.simpleserdes.BufferSerde;
import titan.ccp.common.kafka.simpleserdes.ReadBuffer;
import titan.ccp.common.kafka.simpleserdes.SimpleSerdes;
import titan.ccp.common.kafka.simpleserdes.WriteBuffer;

/**
 * {@link BufferSerde} for a {@link DayOfWeekKey}. Use the {@link #create()} method to create a new
 * Kafka {@link Serde}.
 */
public class DayOfWeekKeySerde implements BufferSerde<DayOfWeekKey> {

  @Override
  public void serialize(final WriteBuffer buffer, final DayOfWeekKey data) {
    buffer.putInt(data.getDayOfWeek().getValue());
    buffer.putString(data.getSensorId());
  }

  @Override
  public DayOfWeekKey deserialize(final ReadBuffer buffer) {
    final DayOfWeek dayOfWeek = DayOfWeek.of(buffer.getInt());
    final String sensorId = buffer.getString();
    return new DayOfWeekKey(dayOfWeek, sensorId);
  }

  public static Serde<DayOfWeekKey> create() {
    return SimpleSerdes.create(new DayOfWeekKeySerde());
  }

}
