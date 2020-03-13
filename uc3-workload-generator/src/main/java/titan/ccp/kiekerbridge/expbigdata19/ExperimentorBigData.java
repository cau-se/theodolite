package titan.ccp.kiekerbridge.expbigdata19;

import java.io.IOException;
import java.util.Objects;

public class ExperimentorBigData {

  public static void main(final String[] args) throws InterruptedException, IOException {

    final String modus = Objects.requireNonNullElse(System.getenv("MODUS"), "LoadCounter");

    if (modus.equals("LoadGenerator")) {
      LoadGenerator.main(args);
    } else if (modus.equals("LoadGeneratorExtrem")) {
      LoadGeneratorExtrem.main(args);
    } else if (modus.equals("LoadCounter")) {
      LoadCounter.main(args);
    }

  }
}
