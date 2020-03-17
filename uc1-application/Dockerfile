FROM openjdk:11-slim

ADD build/distributions/uc1-application.tar /

CMD export JAVA_OPTS=-Dorg.slf4j.simpleLogger.defaultLogLevel=$LOG_LEVEL \
    && /uc1-application/bin/uc1-application