ARG DOCKER_VERSION=latest

FROM docker:${DOCKER_VERSION}

RUN apk update && \
    apk add jq && \
    apk add py-pip python3-dev libffi-dev openssl-dev gcc libc-dev rust cargo make && \
    pip install pyyaml==5.3.1 docker-compose
