FROM openjdk:11-slim

ADD build/distributions/exp-bigdata19-bridge.tar /

CMD export JAVA_OPTS=-Dorg.slf4j.simpleLogger.defaultLogLevel=$LOG_LEVEL \
    && /exp-bigdata19-bridge/bin/exp-bigdata19-bridge