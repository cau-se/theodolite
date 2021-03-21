FROM openjdk:11-slim

ADD build/distributions/uc4-kstreams.tar /

CMD JAVA_OPTS="$JAVA_OPTS -Dorg.slf4j.simpleLogger.defaultLogLevel=$LOG_LEVEL" \
     /uc4-kstreams/bin/uc4-kstreams
