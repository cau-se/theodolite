package common;

import common.dimensions.KeySpace;

public class WorkloadDeclaration {
  private final KeySpace keySpace;
  private final int numberOfWorkers;
  
  public WorkloadDeclaration(final KeySpace keySpace, final int numberOfWorkers) {

    this.keySpace = keySpace;
    this.numberOfWorkers = numberOfWorkers;
  }
  
  public KeySpace getKeySpace() {
    return this.keySpace;
  }
  
  public int getNumberOfWorkers() {
    return this.numberOfWorkers;
  }
  
  /*
   * Replace by json format serialization/deserialization
   */
  @Override
  public String toString() {
    return this.getKeySpace().getPrefix() + ";" + this.getKeySpace().getMin() + ";" + this.getKeySpace().getMax() + ";" + this.getNumberOfWorkers();
  }

  public static WorkloadDeclaration fromString(String declarationString) {
    final String[] deserialized = declarationString.split(";");
 
    return new WorkloadDeclaration(new KeySpace(deserialized[0], Integer.valueOf(deserialized[1]), Integer.valueOf(deserialized[2])), Integer.valueOf(deserialized[3]));
  }
}
