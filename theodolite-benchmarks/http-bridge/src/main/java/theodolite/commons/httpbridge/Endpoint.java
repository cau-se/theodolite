package theodolite.commons.httpbridge;

import theodolite.commons.workloadgeneration.RecordSender;

public class Endpoint<T> {

  private final String path;

  private final Deserializer<? extends T> recordDeserializer;

  private final RecordSender<? super T> recordSender;

  public Endpoint(
      final String path,
      final Deserializer<? extends T> recordDeserializer,
      final RecordSender<? super T> recordSender) {
    this.path = path;
    this.recordDeserializer = recordDeserializer;
    this.recordSender = recordSender;
  }

  public Endpoint(
      final String path,
      final Class<T> recordType,
      final RecordSender<? super T> recordSender) {
    this.path = path;
    this.recordDeserializer = new GsonDeserializer<>(recordType);
    this.recordSender = recordSender;
  }

  public String getPath() {
    return this.path;
  }

  public void convert(final String json) {
    final T record = this.recordDeserializer.deserialize(json);
    this.recordSender.send(record);
  }

}
