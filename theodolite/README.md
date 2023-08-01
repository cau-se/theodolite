# Theodolite

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```sh
./gradlew quarkusDev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at http://localhost:8080/q/dev/.


## Packaging and running the application

The application can be packaged using:

```sh
./gradlew build
```

It produces the `quarkus-run.jar` file in the `build/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `build/quarkus-app/lib/` directory.

The application is now runnable using `java -jar build/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```sh
./gradlew build -Dquarkus.package.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar build/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```sh
./gradlew build -Dquarkus.package.type=native
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```sh
./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./build/theodolite-0.10.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult https://quarkus.io/guides/gradle-tooling.

## Building container images

For the JVM version use:

```sh
./gradlew build
docker build -f src/main/docker/Dockerfile.jvm -t theodolite-jvm .
```

Alternatively, you can also use Kaniko to build the image:

```sh
docker run -it --rm --name kaniko -v "`pwd`":/theodolite --entrypoint "" gcr.io/kaniko-project/executor:debug /kaniko/executor --context /theodolite --dockerfile src/main/docker/Dockerfile.jvm --no-push
```

For the native image version use:

```sh
./gradlew build -Dquarkus.package.type=native
docker build -f src/main/docker/Dockerfile.native -t theodolite-native .
```

## Run a container

Remember to set the environment variables first.

Jvm version:

```sh
docker run -i --rm theodolite-jvm
```

Native image version:

```sh
docker run -i --rm theodolite-native
```

## Environment variables

**Execution in Docker**:

| Variables name               | Default value                      |Usage         |
| -----------------------------|:----------------------------------:| ------------:|
| `NAMESPACE`                  | `default`                          |Determines the namespace of the Theodolite will be executed in. Used in the KubernetesBenchmark|
| `THEODOLITE_EXECUTION`       |  `execution/execution.yaml`        |The complete path to the benchmarkExecution file. Used in the TheodoliteYamlExecutor. |
| `THEODOLITE_BENCHMARK_TYPE`  |  `benchmark/benchmark.yaml`        |The complete path to the benchmarkType file. Used in the TheodoliteYamlExecutor.|
| `THEODOLITE_APP_RESOURCES`   |  `./benchmark-resources`           |The path under which the yamls for the resources for the subexperiments are found. Used in the KubernetesBenchmark|
| `MODE`                       | `standalone`                       |Defines the mode of operation: either `standalone` or `operator`

**Execution in IntelliJ**:

When running Theodolite from within IntelliJ via
[Run Configurations](https://www.jetbrains.com/help/idea/work-with-gradle-tasks.html#gradle_run_config), set the *Environment variables* field to:

Set the following environment variables to run the example in the `standalone` mode within the IDE:

```sh
THEODOLITE_BENCHMARK=./../../../../examples/standalone/example-benchmark.yaml;THEODOLITE_EXECUTION=./../../../../examples/standalone/example-execution.yaml;THEODOLITE_APP_RESOURCES=./../../../../examples/resources;
```

Alternative:

``` sh
export THEODOLITE_BENCHMARK=./../../../../examples/standalone/example-benchmark.yaml
export THEODOLITE_APP_RESOURCES=./../../../../examples/resources;
export THEODOLITE_EXECUTION=./../../../../examples/standalone/example-execution.yaml
./gradlew quarkusDev
```

Set the following environment variables to run the example in the `operator` mode within the IDE:

```sh
THEODOLITE_APP_RESOURCES=./../../../../examples/resources;MODE=operator
```

Alternative:

``` sh
export THEODOLITE_APP_RESOURCES=./../../../../examples/resources;
export MODE=operator
./gradlew quarkusDev
```

Additionally, the benchmark and execution resources must be installed.

### Install Detekt Code analysis Plugin

Install <https://plugins.jetbrains.com/plugin/10761-detekt>

- Install the plugin
- Navigate to Settings/Preferences -> Tools -> Detekt
- Check Enable Detekt
- Specify your detekt configuration and baseline file (optional)

-> detekt issues will be annotated on-the-fly while coding

**ingore Failures in build**: add

```ignoreFailures = true```

to build.gradle detekt task
