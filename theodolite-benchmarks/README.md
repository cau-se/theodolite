# Theodolite Benchmarks

Theodolite comes with a set of 4 benchmarks for event-driven microservices, which are implemented with Kafka Streams
and Apache Flink. The benchmarks are based on typical use cases for stream processing and named: UC1, UC2, UC3 and UC4.

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
