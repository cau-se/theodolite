package titan.ccp.aggregation.streamprocessing;

import static org.junit.Assert.assertEquals;
import java.util.function.Function;

public class SerdeTester<T> {

  private final T originalObject;

  private final T serdedObject;

  public SerdeTester(final T originalObject, final T serdedObject) {
    this.originalObject = originalObject;
    this.serdedObject = serdedObject;
  }

  public void test() {
    this.test(o -> o);
  }

  public void test(final Function<T, Object> fieldAccessor) {
    final Object originalField = fieldAccessor.apply(this.originalObject);
    final Object serdedField = fieldAccessor.apply(this.serdedObject);
    assertEquals(originalField, serdedField);
  }

}
