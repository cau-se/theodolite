FROM openjdk:11-slim

ADD build/distributions/uc3-hazelcastjet.tar /


CMD  JAVA_OPTS="$JAVA_OPTS -Dorg.slf4j.simpleLogger.defaultLogLevel=$LOG_LEVEL" \
     /uc3-hazelcastjet/bin/uc3-hazelcastjet