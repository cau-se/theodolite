# Theodolite project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/> .

## Running the application in dev mode

You can run your application in dev mode using:

```sh
./gradlew quarkusDev
```

## Packaging and running the application

The application can be packaged using:

```sh
./gradlew build
```

It produces the `theodolite-1.0.0-SNAPSHOT-runner.jar` file in the `/build` directory. Be aware that it’s not
an _über-jar_ as the dependencies are copied into the `build/lib` directory.

If you want to build an _über-jar_, execute the following command:

```sh
./gradlew build -Dquarkus.package.type=uber-jar
```

The application is now runnable using `java -jar build/theodolite-1.0.0-SNAPSHOT-runner.jar`.

## Creating a native executable

It is recommended to use the native GraalVM images to create executable jars from Theodolite. For more information please visit the [Native Image guide](https://www.graalvm.org/reference-manual/native-image/).

You can create a native executable using:

```sh
./gradlew build -Dquarkus.package.type=native
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```sh
./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true
```

You can then execute your native executable with:
```./build/theodolite-1.0.0-SNAPSHOT-runner```

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/gradle-tooling>.

## Build docker images

For the jvm version use:

```sh
./gradlew build
docker build -f src/main/docker/Dockerfile.jvm -t theodolite-jvm .
```

For the native image version use:

```sh
./gradlew build -Dquarkus.package.type=native
docker build -f src/main/docker/Dockerfile.native -t theodolite-native .
```

## Execute docker images

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

```sh
NAMESPACE=default;THEODOLITE_BENCHMARK=./../../../../config/BenchmarkType.yaml;THEODOLITE_APP_RESOURCES=./../../../../config;THEODOLITE_EXECUTION=./../../../../config/BenchmarkExecution.yaml;MODE=operator
```

Alternative:

``` sh
export NAMESPACE=default
export THEODOLITE_BENCHMARK=./../../../../config/BenchmarkType.yaml
export THEODOLITE_APP_RESOURCES=./../../../../config
export THEODOLITE_EXECUTION=./../../../../config/BenchmarkExecution.yaml
export MODE=operator
./gradlew quarkusDev
```

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
