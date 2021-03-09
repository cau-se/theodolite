package theodolite.uc2.application;

import com.google.common.math.Stats;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

public class StatsProcessWindowFunction
    extends ProcessWindowFunction<Stats, Tuple2<String, Stats>, String, TimeWindow> {

  private static final long serialVersionUID = 4363099880614593379L; // NOPMD

  @Override
  public void process(final String key, final Context context, final Iterable<Stats> elements,
      final Collector<Tuple2<String, Stats>> out) {
    final Stats stats = elements.iterator().next();
    out.collect(new Tuple2<>(key, stats));
  }
}
