FROM openjdk:11-slim

ENV MAX_SOURCE_PARALLELISM=1024
ENV ENABLE_METRICS=true

ADD build/distributions/uc4-beam-samza.tar /
ADD samza-standalone.properties /

CMD JAVA_OPTS="$JAVA_OPTS -Dorg.slf4j.simpleLogger.defaultLogLevel=$LOG_LEVEL" \
    /uc4-beam-samza/bin/uc4-beam-samza \
    --configFilePath=samza-standalone.properties \
    --samzaExecutionEnvironment=STANDALONE \
    --maxSourceParallelism=$MAX_SOURCE_PARALLELISM \
    --enableMetrics=$ENABLE_METRICS \
    --configOverride="{\"job.coordinator.zk.connect\":\"$SAMZA_JOB_COORDINATOR_ZK_CONNECT\"}"
