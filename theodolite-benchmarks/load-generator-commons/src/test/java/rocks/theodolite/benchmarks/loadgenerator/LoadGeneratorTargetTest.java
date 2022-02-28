package rocks.theodolite.benchmarks.loadgenerator;

import org.junit.Assert;
import org.junit.Test;

public class LoadGeneratorTargetTest {

  @Test
  public void testFromKafka() {
    final LoadGeneratorTarget target = LoadGeneratorTarget.from("kafka");
    Assert.assertEquals(LoadGeneratorTarget.KAFKA, target);
  }

  @Test
  public void testFromHttp() {
    final LoadGeneratorTarget target = LoadGeneratorTarget.from("http");
    Assert.assertEquals(LoadGeneratorTarget.HTTP, target);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFromInvalidTarget() {
    LoadGeneratorTarget.from("<invalid-target>");
  }


}
