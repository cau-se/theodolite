# Theodolite Benchmarks

Theodolite comes with a set of 4 benchmarks for event-driven microservices, which are implemented with Kafka Streams, Apache Flink, Hazelcast Jet, and Apache Beam. For the Beam implementations, we support runners for Apache Samza and Apache Flink.
The benchmarks are based on typical use cases for stream processing and named: UC1, UC2, UC3 and UC4.
Additionally, we include a load generator for each benchmark.

## Project organization

All benchmark implementations are organized in a Gradle multi-project. See the [`settings.gradle`](settings.gradle) file for an overview of subprojects and how they are organized.
We also use Gradle convention plugins, organized in [`buildSrc`](buildSrc), for sharing build configuration among subprojects.

Additionally, this directory contains:

* *Theodolite* Benchmark definitions for all benchmarks in [`definitions`](definitions).
* Docker Compose files to assist in local development and to run smoke tests in [`docker-test`](docker-test).

## Building and packaging the benchmarks

All benchmarks can be built with:

```sh
./gradlew build
```

This produces `.tar` files in `<benchmark-impl>/build/distribution`, where `<benchmark-impl>` is for example
`uc1-kstreams`.

## Building Docker images of the benchmarks

Each benchmark implementation directory contains a Dockerfile. To build an image (in this case of `uc1-kstreams`), run:

```sh
docker build -t theodolite-uc1-kstreams ./uc1-kstreams
```
