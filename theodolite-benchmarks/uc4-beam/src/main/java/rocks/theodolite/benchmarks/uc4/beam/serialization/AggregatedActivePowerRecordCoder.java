package rocks.theodolite.benchmarks.uc4.beam.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.CoderException;
import rocks.theodolite.benchmarks.commons.model.records.AggregatedActivePowerRecord;

/**
 * {@link Coder} for an {@link AggregatedActivePowerRecord}.
 */
@SuppressWarnings("serial")
public class AggregatedActivePowerRecordCoder extends Coder<AggregatedActivePowerRecord>
    implements Serializable {

  private static final boolean DETERMINISTIC = true;

  private transient AvroCoder<AggregatedActivePowerRecord> avroEnCoder =
      AvroCoder.of(AggregatedActivePowerRecord.class);

  @Override
  public void encode(final AggregatedActivePowerRecord value, final OutputStream outStream)
      throws CoderException, IOException {
    if (this.avroEnCoder == null) {
      this.avroEnCoder = AvroCoder.of(AggregatedActivePowerRecord.class);
    }
    this.avroEnCoder.encode(value, outStream);

  }

  @Override
  public AggregatedActivePowerRecord decode(final InputStream inStream)
      throws CoderException, IOException {
    if (this.avroEnCoder == null) {
      this.avroEnCoder = AvroCoder.of(AggregatedActivePowerRecord.class);
    }
    return this.avroEnCoder.decode(inStream);

  }

  @Override
  public List<? extends Coder<?>> getCoderArguments() {
    return List.of();
  }

  @Override
  public void verifyDeterministic() throws NonDeterministicException {
    if (!DETERMINISTIC) {
      throw new NonDeterministicException(this, "This class should be deterministic.");
    }
  }
}
