package rocks.theodolite.benchmarks.commons.flink;

import java.io.IOException;
import org.apache.commons.configuration2.Configuration;
import org.apache.flink.contrib.streaming.state.RocksDBStateBackend;
import org.apache.flink.runtime.state.StateBackend;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.runtime.state.memory.MemoryStateBackend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides factory methods for creating Flink {@link StateBackend}s.
 */
public final class StateBackends {

  public static final String STATE_BACKEND_TYPE_MEMORY = "memory";
  public static final String STATE_BACKEND_TYPE_FILESYSTEM = "filesystem";
  public static final String STATE_BACKEND_TYPE_ROCKSDB = "rocksdb";
  // public static final String STATE_BACKEND_TYPE_DEFAULT = STATE_BACKEND_TYPE_ROCKSDB;
  public static final String STATE_BACKEND_TYPE_DEFAULT = STATE_BACKEND_TYPE_MEMORY;
  public static final String DEFAULT_STATE_BACKEND_PATH = "file:///opt/flink/statebackend";

  private static final Logger LOGGER = LoggerFactory.getLogger(StateBackends.class);

  private StateBackends() {}

  /**
   * Create a Flink {@link StateBackend} from a {@link Configuration} and the
   * {@code ConfigurationKeys#FLINK_STATE_BACKEND},
   * {@code ConfigurationKeys#FLINK_STATE_BACKEND_MEMORY_SIZE} and
   * {@code ConfigurationKeys#FLINK_STATE_BACKEND_PATH} configuration keys. Possible options for the
   * {@code ConfigurationKeys#FLINK_STATE_BACKEND} configuration are
   * {@code #STATE_BACKEND_TYPE_ROCKSDB}, {@code #STATE_BACKEND_TYPE_FILESYSTEM} and
   * {@code StateBackendFactory#STATE_BACKEND_TYPE_MEMORY}, where
   * {@code StateBackendFactory#STATE_BACKEND_TYPE_ROCKSDB} is the default.
   */
  public static StateBackend fromConfiguration(final Configuration configuration) {
    final String stateBackendType =
        configuration.getString(ConfigurationKeys.FLINK_STATE_BACKEND, STATE_BACKEND_TYPE_DEFAULT);
    switch (stateBackendType) {
      case STATE_BACKEND_TYPE_MEMORY:
        final int memoryStateBackendSize = configuration.getInt(
            ConfigurationKeys.FLINK_STATE_BACKEND_MEMORY_SIZE,
            MemoryStateBackend.DEFAULT_MAX_STATE_SIZE);
        return new MemoryStateBackend(memoryStateBackendSize);
      case STATE_BACKEND_TYPE_FILESYSTEM:
        final String stateBackendPath = configuration.getString(
            ConfigurationKeys.FLINK_STATE_BACKEND_PATH,
            DEFAULT_STATE_BACKEND_PATH);
        return new FsStateBackend(stateBackendPath);
      case STATE_BACKEND_TYPE_ROCKSDB:
        final String stateBackendPath2 = configuration.getString(
            ConfigurationKeys.FLINK_STATE_BACKEND_PATH,
            DEFAULT_STATE_BACKEND_PATH);
        try {
          return new RocksDBStateBackend(stateBackendPath2, true);
        } catch (final IOException e) {
          LOGGER.error("Cannot create RocksDB state backend.", e);
          throw new IllegalStateException(e);
        }
      default:
        throw new IllegalArgumentException(
            "Unsupported state backend '" + stateBackendType + "' configured.");
    }
  }

}
