package rocks.theodolite.benchmarks.uc3.beam;

import org.apache.beam.sdk.transforms.DoFn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogWriter<T> extends DoFn<T, Void> {

  private static final long serialVersionUID = -5263671231838353742L; // NOPMD

  private static final Logger LOGGER = LoggerFactory.getLogger(LogWriter.class);

  public LogWriter() {
    super();
  }

  @ProcessElement
  public void processElement(@Element final T record, final OutputReceiver<Void> out) {
    System.out.println(record);
    LOGGER.info("Record: {}", record);
  }

}
