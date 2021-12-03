FROM openjdk:11-slim

ADD build/distributions/uc4-beam-samza.tar /
ADD samza-standalone.properties /

CMD /uc4-beam-samza/bin/uc4-beam-samza --configFactory=org.apache.samza.config.factories.PropertiesConfigFactory --configFilePath=samza-standalone.properties --samzaExecutionEnvironment=STANDALONE --enableMetrics=false
