package rocks.theodolite.benchmarks.uc4.kstreams;

import java.util.Objects;
import java.util.Set;
import titan.ccp.model.records.ActivePowerRecord;

/**
 * A joined pair of an {@link ActivePowerRecord} and its associated parents. Both the record and the
 * parents may be <code>null</code>.
 */
public class JointRecordParents {

  private final Set<String> parents;

  private final ActivePowerRecord record;

  public JointRecordParents(final Set<String> parents, final ActivePowerRecord record) {
    this.parents = parents;
    this.record = record;
  }

  public Set<String> getParents() {
    return this.parents;
  }

  public ActivePowerRecord getRecord() {
    return this.record;
  }

  @Override
  public String toString() {
    return "{" + this.parents + ", " + this.record + "}";
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.parents, this.record);
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == this) {
      return true;
    }
    if (obj instanceof JointRecordParents) {
      final JointRecordParents other = (JointRecordParents) obj;
      return Objects.equals(this.parents, other.parents)
          && Objects.equals(this.record, other.record);
    }
    return false;
  }

}
