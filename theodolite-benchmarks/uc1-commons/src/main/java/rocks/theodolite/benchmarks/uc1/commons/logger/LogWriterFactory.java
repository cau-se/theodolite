package rocks.theodolite.benchmarks.uc1.commons.logger;

import org.slf4j.Logger;
import rocks.theodolite.benchmarks.uc1.commons.DatabaseAdapter;

/**
 * Provides factory methods for creating a dummy {@link DatabaseAdapter} writing records as logs
 * using a SLF4J {@link Logger}.
 */
public final class LogWriterFactory {

  private LogWriterFactory() {}

  public static DatabaseAdapter<String> forJson() {
    return DatabaseAdapter.from(new JsonConverter(), new LogWriter());
  }

}
