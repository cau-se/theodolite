FROM openjdk:11-slim

ADD build/distributions/uc2-beam-flink.tar /

CMD /uc2-beam-flink/bin/uc2-beam-flink --runner=FlinkRunner --flinkMaster=flink-jobmanager:8081 --streaming --parallelism=$PARALLELISM --disableMetrics=true --fasterCopy
