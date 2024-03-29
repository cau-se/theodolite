package rocks.theodolite.benchmarks.loadgenerator;

/**
 * Describes the before action which is executed before every sub experiment.
 */
@FunctionalInterface
public interface BeforeAction {

  public void run();

  public static BeforeAction doNothing() {
    return () -> {
    };
  }

}
