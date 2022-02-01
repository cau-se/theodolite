FROM docker:${DOCKER_VERSION:-latest}

RUN apk update && \
    apk add jq && \
    apk add py-pip python3-dev libffi-dev openssl-dev gcc libc-dev rust cargo make && \
    pip install docker-compose
