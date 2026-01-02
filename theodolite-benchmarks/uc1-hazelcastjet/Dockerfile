FROM eclipse-temurin:11-jre

ADD build/distributions/uc1-hazelcastjet.tar /


CMD  JAVA_OPTS="$JAVA_OPTS -Dorg.slf4j.simpleLogger.defaultLogLevel=$LOG_LEVEL" \
     /uc1-hazelcastjet/bin/uc1-hazelcastjet