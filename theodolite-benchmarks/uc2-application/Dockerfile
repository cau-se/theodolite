FROM openjdk:11-slim

ADD build/distributions/uc2-application.tar /

CMD JAVA_OPTS="$JAVA_OPTS -Dorg.slf4j.simpleLogger.defaultLogLevel=$LOG_LEVEL" \
     /uc2-application/bin/uc2-application