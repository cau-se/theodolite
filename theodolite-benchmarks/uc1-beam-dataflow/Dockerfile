FROM openjdk:11-slim

ENV MAX_SOURCE_PARALLELISM=1024

ADD build/distributions/uc1-beam-samza.tar /
ADD samza-standalone.properties /

CMD /uc1-beam-samza/bin/uc1-beam-samza --configFilePath=samza-standalone.properties --samzaExecutionEnvironment=STANDALONE --maxSourceParallelism=$MAX_SOURCE_PARALLELISM --enableMetrics=false --configOverride="{\"job.coordinator.zk.connect\":\"$SAMZA_JOB_COORDINATOR_ZK_CONNECT\"}"

