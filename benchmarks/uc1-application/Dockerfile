FROM openjdk:11-slim

ADD . /
RUN ls /


CMD  JAVA_OPTS="$JAVA_OPTS -Dorg.slf4j.simpleLogger.defaultLogLevel=$LOG_LEVEL" \
     /uc1-application/bin/uc1-application