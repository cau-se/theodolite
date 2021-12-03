FROM openjdk:11-slim

ADD build/distributions/uc4-beam-flink.tar /

CMD /uc4-beam-flink/bin/uc4-beam-flink --runner=FlinkRunner --streaming --disableMetrics=true --fasterCopy --stateBackend=rocksdb --stateBackendStoragePath=file:///data/flink/checkpoints
