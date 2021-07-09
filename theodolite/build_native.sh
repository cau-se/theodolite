
./gradlew build -Dquarkus.package.type=native -x test

docker build -f src/main/docker/Dockerfile.native -t quarkus/theodolite .

docker run -i --rm -p 8080:8080 quarkus/theodolite
