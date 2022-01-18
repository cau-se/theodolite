package application;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.KV;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs all Key Value pairs.
 */
@SuppressWarnings({"unused"})
public class LogKeyValue extends DoFn<KV<String, String>, KV<String, String>> {
  private static final long serialVersionUID = 4328743;
  private static final Logger LOGGER = LoggerFactory.getLogger(LogKeyValue.class);

  /**
   * Logs all key value pairs it processes.
   */
  @ProcessElement
  public void processElement(@Element final KV<String, String> kv,
      final OutputReceiver<KV<String, String>> out) {
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info("Key: {}, Value: {}", kv.getKey(), kv.getValue());
    }
    out.output(kv);
  }
}
