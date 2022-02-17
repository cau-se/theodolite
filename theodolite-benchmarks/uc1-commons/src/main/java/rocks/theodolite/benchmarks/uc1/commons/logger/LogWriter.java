package rocks.theodolite.benchmarks.uc1.commons.logger;

import java.io.Serializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rocks.theodolite.benchmarks.uc1.commons.DatabaseWriter;

/**
 * Writes string records to a {@link Logger}.
 */
public class LogWriter implements DatabaseWriter<String>, Serializable {

  private static final long serialVersionUID = -5263671231838353749L; // NOPMD

  private static final Logger LOGGER = LoggerFactory.getLogger(LogWriter.class);

  @Override
  public void write(final String string) {
    LOGGER.info("Record: {}", string);
  }

}
