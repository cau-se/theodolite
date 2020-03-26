FROM openjdk:11-slim


ADD build/distributions/uc4-application.tar /


CMD  JAVA_OPTS="$JAVA_OPTS -Dorg.slf4j.simpleLogger.defaultLogLevel=$LOG_LEVEL" \
     /uc4-application/bin/uc4-application
