package rocks.theodolite.benchmarks.uc3.beam;

import org.apache.beam.sdk.transforms.DoFn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple {@link DoFn} that simply logs all incoming objects.
 *
 * @param <T> Type of objects to be logged.
 */
public class LogWriter<T> extends DoFn<T, Void> {

  private static final long serialVersionUID = -5263671231838353742L; // NOPMD

  private static final Logger LOGGER = LoggerFactory.getLogger(LogWriter.class);

  @ProcessElement
  public void processElement(@Element final T record, final OutputReceiver<Void> out) {
    LOGGER.info("Record: {}", record);
  }

}
