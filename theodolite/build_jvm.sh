
./gradlew build -x test

docker build -f src/main/docker/Dockerfile.jvm -t quarkus/theodolite-jvm .

docker run -i --rm -p 8080:8080 quarkus/theodolite-jvm
