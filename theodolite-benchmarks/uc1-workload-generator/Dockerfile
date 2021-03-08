FROM openjdk:11-slim

ADD build/distributions/uc1-workload-generator.tar /

CMD  JAVA_OPTS="$JAVA_OPTS -Dorg.slf4j.simpleLogger.defaultLogLevel=$LOG_LEVEL" \
     /uc1-workload-generator/bin/uc1-workload-generator