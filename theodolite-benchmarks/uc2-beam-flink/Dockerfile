FROM openjdk:11-slim

ADD build/distributions/uc3-beam-flink.tar /

CMD /uc3-beam-flink/bin/uc3-beam-flink --runner=FlinkRunner --streaming --disableMetrics=true --fasterCopy
