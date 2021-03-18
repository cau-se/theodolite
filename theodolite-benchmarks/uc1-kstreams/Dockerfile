FROM openjdk:11-slim

ADD build/distributions/uc1-kstreams.tar /


CMD  JAVA_OPTS="$JAVA_OPTS -Dorg.slf4j.simpleLogger.defaultLogLevel=$LOG_LEVEL" \
     /uc1-kstreams/bin/uc1-kstreams