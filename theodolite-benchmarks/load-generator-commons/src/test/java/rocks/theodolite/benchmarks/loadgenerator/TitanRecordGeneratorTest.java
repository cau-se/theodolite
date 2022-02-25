package rocks.theodolite.benchmarks.loadgenerator;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import org.junit.Assert;
import org.junit.Test;
import titan.ccp.model.records.ActivePowerRecord;

public class TitanRecordGeneratorTest {

  @Test
  public void testGenerate() {
    final ZoneId zoneId = ZoneOffset.UTC;
    final LocalDateTime dateTime = LocalDateTime.of(2022, 1, 17, 14, 2, 42);
    final Instant instant = dateTime.atZone(zoneId).toInstant();
    final TitanRecordGenerator generator =
        new TitanRecordGenerator(42.0, Clock.fixed(instant, zoneId));

    final ActivePowerRecord activePowerRecord = generator.generate("my-identifier");
    Assert.assertEquals("my-identifier", activePowerRecord.getIdentifier());
    Assert.assertEquals(instant.toEpochMilli(), activePowerRecord.getTimestamp());
    Assert.assertEquals(42.0, activePowerRecord.getValueInW(), 0.001);
  }

  @Test
  public void testTimestampForArbitraryClockTimeZone() {
    final LocalDateTime dateTime = LocalDateTime.of(2022, 1, 17, 14, 2, 42);
    final Instant instant = dateTime.atZone(ZoneId.of("Europe/Paris")).toInstant();
    // Setting of ZoneId should have no impact on result as we request epoch millis
    final Clock clock = Clock.fixed(instant, ZoneId.of("America/Sao_Paulo"));
    final TitanRecordGenerator generator = new TitanRecordGenerator(42.0, clock);

    final ActivePowerRecord activePowerRecord = generator.generate("my-identifier");
    Assert.assertEquals(instant.toEpochMilli(), activePowerRecord.getTimestamp());
  }

}
