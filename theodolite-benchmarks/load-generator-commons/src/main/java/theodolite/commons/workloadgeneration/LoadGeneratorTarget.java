package theodolite.commons.workloadgeneration;

import java.util.stream.Stream;

enum LoadGeneratorTarget {

  HTTP("http"), KAFKA("kafka"), PUBSUB("pubsub");

  private final String value;

  LoadGeneratorTarget(final String value) {
    this.value = value;
  }

  String getValue() {
    return this.value;
  }

  static LoadGeneratorTarget from(final String value) {
    return Stream.of(LoadGeneratorTarget.values())
        .filter(t -> t.value.equals(value))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Target '" + value + "' does not exist."));
  }

}
