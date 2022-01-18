package theodolite.commons.workloadgeneration;

import java.util.stream.Stream;

enum LoadGeneratorTarget {

  KAFKA("kafka"), HTTP("http");

  final String value;

  LoadGeneratorTarget(final String value) {
    this.value = value;
  }

  static LoadGeneratorTarget from(final String value) {
    return Stream.of(LoadGeneratorTarget.values())
        .filter(t -> t.value.equals(value))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Target '" + value + "' does not exist."));
  }

}
