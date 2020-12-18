
./gradlew build -Dquarkus.package.type=native


docker build -f src/main/docker/Dockerfile.native -t quarkus/theodolite-quarkus .

docker run -i --rm -p 8080:8080 quarkus/theodolite-quarkus
