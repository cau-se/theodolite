package theodolite.uc4.application.uc4specifics;

import java.util.Objects;
import java.util.Set;

/**
 * Structure: (valueInW, Set(Groups))
 */
public class ValueGroup {

  private final Double valueInW;
  private final Set<String> groups;
  
  public ValueGroup(Double valueInW, Set<String> groups) {
    this.valueInW = valueInW;
    this.groups = groups;
  }
  
  public Double getValueInW() {
    return this.valueInW;
  }
  
  public Set<String> getGroups() {
    return this.groups;
  }
  
  @Override
  public String toString() {
    String groupString = "[";
    for (String group: groups) {
      groupString = groupString + group + "/";
    }
    return this.valueInW.toString() + ";" + groupString + "]";
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(this.valueInW, this.groups);
  }
  
  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof ValueGroup) {
      final ValueGroup other = (ValueGroup) obj;
      return Objects.equals(this.valueInW, other.valueInW)
          && this.groups.containsAll(other.groups);
    }
    return false;
  }
  
}
