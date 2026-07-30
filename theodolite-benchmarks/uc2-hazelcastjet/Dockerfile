FROM eclipse-temurin:11-jre

ADD build/distributions/uc2-hazelcastjet.tar /


CMD  JAVA_OPTS="$JAVA_OPTS -Dorg.slf4j.simpleLogger.defaultLogLevel=$LOG_LEVEL" \
     /uc2-hazelcastjet/bin/uc2-hazelcastjet