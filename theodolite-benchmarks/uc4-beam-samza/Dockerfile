FROM openjdk:8-slim

ADD build/distributions/uc2-application-samza.tar /
ADD ../config/standalone.properties /


CMD /uc2-application-samza/bin/uc2-application-samza --configFactory=org.apache.samza.config.factories.PropertiesConfigFactory --configFilePath=samza-standalone.properties --samzaExecutionEnvironment=STANDALONE --maxSourceParallelism=$MAX_SOURCE_PARALLELISM --enableMetrics=false
