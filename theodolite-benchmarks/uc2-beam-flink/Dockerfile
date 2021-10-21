FROM openjdk:8-slim

ADD build/distributions/uc3-application-flink.tar /

CMD /uc3-application-flink/bin/uc3-application-flink --runner=FlinkRunner --flinkMaster=flink-jobmanager:8081 --streaming --parallelism=$PARALLELISM  --disableMetrics=true --fasterCopy