# Docker Compose Files for Testing

This directory contains Docker Compose files, which help testing Benchmark implementations.
For each stream processing engine (Kafka Streams and Flink) and Benchmark (UC1-4), a Docker Compose file is provided
in the corresponding subdirectory.

## Full Dockerized Testing

Running the load generator, the benchmark and all required infrastructure (Kafka etc.) is easy. Simply, `cd` into the
directory of the benchmark implementation Compose file and *up* it.
For example:

```sh
cd uc1-kstreams-docker-compose/
docker-compose up -d
```

On less powerful hardware, starting all containers together might fail. In such cases, it usually helps to first *up*
Kafka and ZooKeeper (`docker-compose up -d kafka zookeeper`), then after some delay the Schema Registry
(`docker-compose up -d schema-registry`) and finally, after some more further delay, the rest (`docker-compose up -d`).

To tear down the entire Docker Compose configuration:

```sh
docker-compose down
```

## Benchmark (+ Load Generator) on Host, Infrastructure in Docker

For development and debugging purposes, it is often required to run the benchmark and/or the load generator directly on
the host, for example, from the IDE or Gradle. In such cases, the following adjustments have to be made to the
`docker-compose.yaml` file:

1. Comment out the services that you intend to run locally.
2. Uncomment the `ports` block in the Kafka and the Schema Registry services.

You can now connect to Kafka from your host system with bootstrap server `localhost:19092` and contact the Schema
Registry via `localhost:8081`. **Pay attention to the Kafka port, which is *19092* instead of the default one *9092*.**

## Running Smoke Tests

The `smoketest-runner.sh` script can be used to run a simple test for a specific Docker Compose file. You can call it with

```sh
./smoketest-runner.sh <docker-compose-dir>
```

where `<docker-compose-dir>` is the directory of a Docker-Compose file, for example, `uc2-beam-samza`. The script exists with a zero exit code in case of success and a non-zero exit code otherwise.

You can also run the set of all smoke test with:

```sh
./smoketest-runner-all.sh
```
