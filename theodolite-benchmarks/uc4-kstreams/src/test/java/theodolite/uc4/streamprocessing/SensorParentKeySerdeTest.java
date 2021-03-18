package theodolite.uc4.streamprocessing;

import org.junit.Test;

public class SensorParentKeySerdeTest {

  private final SerdeTesterFactory<SensorParentKey> serdeTesterFactory =
      new SerdeTesterFactory<>(SensorParentKeySerde.serde());

  @Test
  public void test() {
    final SensorParentKey sensorParentKey = new SensorParentKey("sensor", "parent");
    final SerdeTester<SensorParentKey> tester = this.serdeTesterFactory.create(sensorParentKey);
    tester.test(o -> o.getParent());
    tester.test(o -> o.getSensor());
  }

}
