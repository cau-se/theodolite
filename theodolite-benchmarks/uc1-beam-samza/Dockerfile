FROM openjdk:11-slim

ENV MAX_SOURCE_PARALLELISM=1024

ADD build/distributions/uc1-beam-samza.tar /
ADD samza-standalone.properties /

CMD /uc1-beam-samza/bin/uc1-beam-samza --configFactory=org.apache.samza.config.factories.PropertiesConfigFactory --configFilePath=samza-standalone.properties --samzaExecutionEnvironment=STANDALONE --maxSourceParallelism=$MAX_SOURCE_PARALLELISM --enableMetrics=false

