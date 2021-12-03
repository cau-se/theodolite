FROM openjdk:11-slim

ADD build/distributions/uc2-beam-flink.tar /

CMD /uc2-beam-flink/bin/uc2-beam-flink --runner=FlinkRunner --streaming --disableMetrics=true --fasterCopy --stateBackend=rocksdb --stateBackendStoragePath=file:///data/flink/checkpoints
