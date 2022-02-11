FROM openjdk:11-slim

ADD build/distributions/http-bridge.tar /

CMD  JAVA_OPTS="$JAVA_OPTS -Dorg.slf4j.simpleLogger.defaultLogLevel=$LOG_LEVEL" \
     /http-bridge/bin/http-bridge