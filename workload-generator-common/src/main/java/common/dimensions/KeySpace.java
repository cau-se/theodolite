package common.dimensions;

public class KeySpace {
  
  private final String prefix;
  private final int min;
  private final int max;
  
  
  public KeySpace(final String prefix, final int min, final int max) {
    super();
    this.prefix = prefix;
    this.min = min;
    this.max = max;
  }
  
  public KeySpace(final String prefix, final int numberOfKeys) {
    this(prefix, 0, numberOfKeys-1);
  }
  
  public KeySpace(final int numberOfKeys) {
    this("sensor_", 0, numberOfKeys-1);
  }

  public String getPrefix() {
    return prefix;
  }


  public int getMin() {
    return min;
  }


  public int getMax() {
    return max;
  }
}
