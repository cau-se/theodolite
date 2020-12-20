# theodolite-quarkus project

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: https://quarkus.io/ .

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./gradlew quarkusDev
```

## Packaging and running the application

The application can be packaged using:
```shell script
./gradlew build
```
It produces the `theodolite-quarkus-1.0.0-SNAPSHOT-runner.jar` file in the `/build` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `build/lib` directory.

If you want to build an _über-jar_, execute the following command:
```shell script
./gradlew build -Dquarkus.package.type=uber-jar
```

The application is now runnable using `java -jar build/theodolite-quarkus-1.0.0-SNAPSHOT-runner.jar`.

## Creating a native executable

You can create a native executable using:
```shell script
./gradlew build -Dquarkus.package.type=native
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:
```shell script
./gradlew build -Dquarkus.package.type=native -Dquarkus.native.container-build=true
```

You can then execute your native executable with:
```./build/theodolite-quarkus-1.0.0-SNAPSHOT-runner```

If you want to learn more about building native executables, please consult https://quarkus.io/guides/gradle-tooling.

# RESTEasy JAX-RS

<p>A Hello World RESTEasy resource</p>

Guide: https://quarkus.io/guides/rest-json

## Build and afterwards run the application in Docker container

```build_jvm.sh```   to build the jvm version

```build_native.sh``` to build the native image graal version 

## Install Detekt Code analysis Plugin


Install https://plugins.jetbrains.com/plugin/10761-detekt

- Install the plugin
- Navigate to Settings/Preferences -> Tools -> Detekt
- Check Enable Detekt
- Specify your detekt configuration and baseline file (optional)


-> detekt issues will be annotated on-the-fly while coding

**ingore Failures in build:** add

```ignoreFailures = true```

 to build.gradle detekt task
