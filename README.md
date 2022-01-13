# ch.resrc.testing

A repository to experiment with testing concepts and frameworks

## Build the Application

The core of this application is framework independent. Adapters and applications of this core are implemented for the
following frameworks:

* Quarkus (Java and native)
* Spring Boot

The applications can be built with the following command:

```shell
./gradlew build
```

The Quarkus application can be built with the following command:

```shell
./gradlew :products:quarkus:app:build
```

If you want to build a native image you can do this by:

1. Installing a compatible version of GraalVM. If you have [SDKMAN!](https://sdkman.io/) installed you can install it by
   running:

```shell
sdk install java 21.3.0.r17-grl
```

**NOTE:** You do not need GraalVM if you build via Docker by passing `-Dquarkus.native.container-build=true` to the
Quarkus-Build task mentioned below. However, per default the building via Docker will build a Linux-specific
application (which you would have to run in Docker unless your OS is Linux).

2. Executing the Quarkus-Build task with the package type `native`:

```shell
./gradlew quarkusBuild -Dquarkus.package.type=native
```

**NOTE:** The build process requires at least 8GB of RAM (if you build via Docker you might have to change the memory
settings of the Docker daemon).

The Spring Boot application can be built with the following command:

```shell
./gradlew :products:spring-boot:app:build
```

## Run the Application

### Run Quarkus Application

The Quarkus application can be run in `dev`-Mode by executing:

```shell
./gradlew :products:quarkus:app:quarkusDev
```

The Dev UI is available under `http://localhost:8080/api/q/dev/`.

The Swagger UI is available under `http://localhost:8080/api/q/swagger-ui/`

For performance testing use:

```shell
./gradlew :products:quarkus:app:run
```

or if you have build a native application the executable located under `ch.resrc.testing/products/quarkus/app/build/`:

```shell
./app-0.0.1-SNAPSHOT-runner
```

### Run Spring Boot

The Spring Boot application can be run by executing:

```shell
./gradlew products:spring-boot:app:bootRun
```

For performance testing use:

```shell
./gradlew products:spring-boot:app:run
```

## Performance Testing

### Gatling

The performance test suite can be executed against all applications (i.e. Quarkus, Quarkus native and Spring Boot) since
they run on the same port per default and expose the same endpoints. The results are comparable since the application
core is exactly the same in all of the applications.

Follow these steps to execute the performance test scenarios:

1. [Build the application](#build-the-application)
2. [Run the application](#run-the-application) using the command for performance tests
3. Execute `./gradlew gatlingRun`
4. Open the report located under `ch.resrc.testing/tests/performance/gatling/build/reports/gatling`. Each run will
   produce a new report in a folder in the mentioned directory.
