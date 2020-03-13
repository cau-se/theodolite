package titan.ccp.aggregation.streamprocessing;

import java.util.Set;
import titan.ccp.models.records.ActivePowerRecord;

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



}
