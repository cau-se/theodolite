FROM openjdk:8-slim

ADD build/distributions/uc4-application-flink.tar /

CMD /uc4-application-flink/bin/uc4-application-flink --runner=FlinkRunner --flinkMaster=flink-jobmanager:8081 --streaming --parallelism=$PARALLELISM --disableMetrics=true --fasterCopy --stateBackend=rocksdb --stateBackendStoragePath=file:///data/flink/checkpoints