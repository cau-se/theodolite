package rocks.theodolite.benchmarks.commons.flink;

/**
 * Keys to access configuration parameters.
 */
public final class ConfigurationKeys {

  public static final String FLINK_STATE_BACKEND = "flink.state.backend";

  public static final String FLINK_STATE_BACKEND_PATH = "flink.state.backend.path";

  public static final String FLINK_STATE_BACKEND_MEMORY_SIZE = // NOPMD
      "flink.state.backend.memory.size";

  public static final String FLINK_CHECKPOINTING = "checkpointing";

  private ConfigurationKeys() {}

}
