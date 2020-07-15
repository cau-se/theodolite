package theodolite.uc4.application.util;

import com.google.common.math.Stats;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.streams.kstream.Window;
import org.apache.kafka.streams.kstream.Windowed;

/**
 * Factory interface for creating a stats Avro record from a {@link Windowed} and a {@link Stats}.
 * The {@link Windowed} contains about information about the start end end of the {@link Window} as
 * well as the sensor id and the aggregated time unit. The {@link Stats} objects contains the actual
 * aggregation results.
 *
 * @param <K> Key type of the {@link Windowed}
 * @param <R> Avro record type
 */
@FunctionalInterface
public interface StatsRecordFactory<K, R extends SpecificRecord> {

  R create(Windowed<K> windowed, Stats stats);

}
