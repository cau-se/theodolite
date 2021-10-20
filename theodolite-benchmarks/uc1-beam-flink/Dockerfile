FROM openjdk:8-slim

ADD build/distributions/uc1-application-flink.tar /

CMD /uc1-application-flink/bin/uc1-application-flink --runner=FlinkRunner --flinkMaster=flink-jobmanager:8081 --streaming --parallelism=$PARALLELISM --disableMetrics=true --fasterCopy