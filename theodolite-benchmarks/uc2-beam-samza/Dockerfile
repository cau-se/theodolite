FROM openjdk:11-slim

ADD build/distributions/uc2-beam-samza.tar /
ADD samza-standalone.properties /

CMD /uc2-beam-samza/bin/uc2-beam-samza --configFactory=org.apache.samza.config.factories.PropertiesConfigFactory --configFilePath=samza-standalone.properties --samzaExecutionEnvironment=STANDALONE --maxSourceParallelism=$MAX_SOURCE_PARALLELISM --enableMetrics=false --jobInstance=$HOSTNAME
