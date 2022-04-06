package rocks.theodolite.benchmarks.uc1.beam.dynamodb;

import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutRequest;
import com.amazonaws.services.dynamodbv2.model.WriteRequest;
import java.util.Map;
import org.apache.beam.sdk.transforms.SimpleFunction;
import titan.ccp.model.records.ActivePowerRecord;

final class PutRequestMapper extends SimpleFunction<ActivePowerRecord, WriteRequest> {

  private static final long serialVersionUID = -5263671231838343749L; // NOPMD

  public PutRequestMapper() {
    super();
  }

  @Override
  public WriteRequest apply(final ActivePowerRecord record) {
    final Map<String, AttributeValue> recordMap = Map.of(
        "identifier", new AttributeValue().withS(record.getIdentifier()),
        "timestamp", new AttributeValue().withN(Long.toString(record.getTimestamp())),
        "valueInW", new AttributeValue().withN(Double.toString(record.getValueInW())));
    return new WriteRequest().withPutRequest(new PutRequest(recordMap));
  }

}
