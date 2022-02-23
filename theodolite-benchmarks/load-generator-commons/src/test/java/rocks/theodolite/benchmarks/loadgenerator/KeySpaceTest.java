package rocks.theodolite.benchmarks.loadgenerator;

import org.junit.Assert;
import org.junit.Test;
import rocks.theodolite.benchmarks.loadgenerator.KeySpace;

public class KeySpaceTest {

  @Test
  public void testCountFixedRangeFromZero() {
    final KeySpace keySpace = new KeySpace("prefix", 0, 9);
    final int count = keySpace.getCount();
    Assert.assertEquals(10, count);
  }

  @Test
  public void testCountFixedRangeNotFromZero() {
    final KeySpace keySpace = new KeySpace("prefix", 4, 11);
    final int count = keySpace.getCount();
    Assert.assertEquals(8, count);
  }

  @Test
  public void testCountAutoRange() {
    final KeySpace keySpace = new KeySpace("prefix", 42);
    final int count = keySpace.getCount();
    Assert.assertEquals(42, count);
  }

}
