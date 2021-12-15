FROM openjdk:11-slim

ADD build/distributions/uc3-beam-flink.tar /

CMD /uc3-beam-flink/bin/uc3-beam-flink --runner=FlinkRunner --flinkMaster=flink-jobmanager:8081 --streaming --parallelism=$PARALLELISM --disableMetrics=true --fasterCopy --stateBackend=rocksdb --stateBackendStoragePath=file:///data/flink/checkpoints
