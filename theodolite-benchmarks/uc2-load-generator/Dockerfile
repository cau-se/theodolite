FROM eclipse-temurin:11-jre

ADD build/distributions/uc2-load-generator.tar /

CMD  JAVA_OPTS="$JAVA_OPTS -Dorg.slf4j.simpleLogger.defaultLogLevel=$LOG_LEVEL" \
     /uc2-load-generator/bin/uc2-load-generator