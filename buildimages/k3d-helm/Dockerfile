ARG DOCKER_VERSION=latest

FROM docker:${DOCKER_VERSION}

ARG KUBECTL_VERSION=v1.21.3

RUN apk add -U wget bash openssl
# install kubectl
RUN wget -q -O /usr/local/bin/kubectl https://storage.googleapis.com/kubernetes-release/release/${KUBECTL_VERSION}/bin/linux/amd64/kubectl && \
    chmod +x /usr/local/bin/kubectl
# install k3d
RUN wget -q -O - https://raw.githubusercontent.com/rancher/k3d/main/install.sh | bash
# install Helm
RUN wget -q -O - https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3 | bash
