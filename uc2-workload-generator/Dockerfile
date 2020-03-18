FROM openjdk:11-slim

ADD build/distributions/uc2-workload-generator.tar /

CMD  JAVA_OPTS="$JAVA_OPTS -Dorg.slf4j.simpleLogger.defaultLogLevel=$LOG_LEVEL" \
     /uc2-workload-generator/bin/uc2-workload-generator