FROM openjdk:11-slim

ADD build/distributions/uc3-beam-samza.tar /
ADD samza-standalone.properties /

CMD /uc3-beam-samza/bin/uc3-beam-samza --configFactory=org.apache.samza.config.factories.PropertiesConfigFactory --configFilePath=samza-standalone.properties --samzaExecutionEnvironment=STANDALONE --enableMetrics=false
