FROM ubuntu:bionic

RUN apt-get update
RUN apt-get dist-upgrade

RUN apt-get install -y curl jq

RUN curl -LO https://storage.googleapis.com/kubernetes-release/release/$(curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt)/bin/linux/amd64/kubectl

RUN chmod +x ./kubectl

RUN mv ./kubectl /usr/local/bin/kubectl

ADD schedule.sh /bin/schedule

CMD /bin/schedule