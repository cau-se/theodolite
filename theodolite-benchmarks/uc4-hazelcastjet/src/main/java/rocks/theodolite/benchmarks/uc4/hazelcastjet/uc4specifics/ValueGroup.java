package rocks.theodolite.benchmarks.uc4.hazelcastjet.uc4specifics;

import java.util.Objects;
import java.util.Set;
import rocks.theodolite.benchmarks.commons.model.records.ActivePowerRecord;


/**
 * Structure: (valueInW, Set(Groups)).
 */
public class ValueGroup {

  private final ActivePowerRecord record;
  private final Set<String> groups;

  public ValueGroup(final ActivePowerRecord record, final Set<String> groups) {
    this.record = record;
    this.groups = groups;
  }

  public ActivePowerRecord getRecord() {
    return this.record;
  }

  public Double getValueInW() {
    return this.record.getValueInW();
  }

  public Set<String> getGroups() {
    return this.groups;
  }

  @Override
  public String toString() {
    String groupString = "[";
    for (final String group : this.groups) {
      groupString = groupString + group + "/";// NOPMD
    }
    return this.record.getValueInW() + ";" + groupString + "]";
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.record, this.groups);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof ValueGroup) {
      final ValueGroup other = (ValueGroup) obj;
      return Objects.equals(this.record.getValueInW(), other.getValueInW())
          && this.groups.containsAll(other.groups);
    }
    return false;
  }

}
