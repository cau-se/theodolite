# Flink 
kubectl create configmap benchmark-resources-uc1-flink --from-file uc1-flink/resources
kubectl create configmap benchmark-resources-uc2-flink --from-file uc2-flink/resources
kubectl create configmap benchmark-resources-uc3-flink --from-file uc3-flink/resources
kubectl create configmap benchmark-resources-uc4-flink --from-file uc4-flink/resources

# Hazelcast Jet
kubectl create configmap benchmark-resources-uc1-hazelcastjet --from-file uc1-hazelcastjet/resources
kubectl create configmap benchmark-resources-uc2-hazelcastjet --from-file uc2-hazelcastjet/resources
kubectl create configmap benchmark-resources-uc3-hazelcastjet --from-file uc3-hazelcastjet/resources
kubectl create configmap benchmark-resources-uc4-hazelcastjet --from-file uc4-hazelcastjet/resources

# Kafka Streams
kubectl create configmap benchmark-resources-uc1-kstreams --from-file uc1-kstreams/resources
kubectl create configmap benchmark-resources-uc2-kstreams --from-file uc2-kstreams/resources
kubectl create configmap benchmark-resources-uc3-kstreams --from-file uc3-kstreams/resources
kubectl create configmap benchmark-resources-uc4-kstreams --from-file uc4-kstreams/resources

# Beam Flink
kubectl create configmap benchmark-resources-uc1-beam-flink --from-file uc1-beam-flink/resources
kubectl create configmap benchmark-resources-uc2-beam-flink --from-file uc2-beam-flink/resources
kubectl create configmap benchmark-resources-uc3-beam-flink --from-file uc3-beam-flink/resources
kubectl create configmap benchmark-resources-uc4-beam-flink --from-file uc4-beam-flink/resources

# Beam Samza
kubectl create configmap benchmark-resources-uc1-beam-samza --from-file uc1-beam-samza/resources
kubectl create configmap benchmark-resources-uc2-beam-samza --from-file uc2-beam-samza/resources
kubectl create configmap benchmark-resources-uc3-beam-samza --from-file uc3-beam-samza/resources
kubectl create configmap benchmark-resources-uc4-beam-samza --from-file uc4-beam-samza/resources

# Load Generator
kubectl create configmap benchmark-resources-uc1-load-generator --from-file uc1-load-generator/resources
kubectl create configmap benchmark-resources-uc2-load-generator --from-file uc2-load-generator/resources
kubectl create configmap benchmark-resources-uc3-load-generator --from-file uc3-load-generator/resources
kubectl create configmap benchmark-resources-uc4-load-generator --from-file uc4-load-generator/resources
