FROM eclipse-temurin:11-jre

ADD build/distributions/uc4-load-generator.tar /

CMD JAVA_OPTS="$JAVA_OPTS -Dorg.slf4j.simpleLogger.defaultLogLevel=$LOG_LEVEL" \
     /uc4-load-generator/bin/uc4-load-generator
