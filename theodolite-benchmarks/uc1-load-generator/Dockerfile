FROM openjdk:11-slim

ADD build/distributions/uc1-load-generator.tar /

CMD  JAVA_OPTS="$JAVA_OPTS -Dorg.slf4j.simpleLogger.defaultLogLevel=$LOG_LEVEL" \
     /uc1-load-generator/bin/uc1-load-generator