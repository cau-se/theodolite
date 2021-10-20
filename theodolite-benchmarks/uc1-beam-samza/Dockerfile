FROM openjdk:8-slim

ADD build/distributions/uc1-application-samza.tar /
ADD ../config/samza-standalone.properties /


CMD /uc1-application-samza/bin/uc1-application-samza --configFactory=org.apache.samza.config.factories.PropertiesConfigFactory --configFilePath=samza-standalone.properties --samzaExecutionEnvironment=STANDALONE --maxSourceParallelism=$MAX_SOURCE_PARALLELISM --enableMetrics=false
