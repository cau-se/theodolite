FROM openjdk:11-slim

ADD build/distributions/uc1-beam-flink.tar /

CMD /uc1-beam-flink/bin/uc1-beam-flink --runner=FlinkRunner --maxSourceParallelism=$MAX_SOURCE_PARALLELISM --streaming --disableMetrics=true --fasterCopy

