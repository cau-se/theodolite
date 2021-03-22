FROM openjdk:11-slim

ADD build/distributions/uc3-load-generator.tar /

CMD  JAVA_OPTS="$JAVA_OPTS -Dorg.slf4j.simpleLogger.defaultLogLevel=$LOG_LEVEL" \
     /uc3-load-generator/bin/uc3-load-generator
