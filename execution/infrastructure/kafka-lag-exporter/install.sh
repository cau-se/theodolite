helm install kafka-lag-exporter https://github.com/lightbend/kafka-lag-exporter/releases/download/v0.6.2/kafka-lag-exporter-0.6.2.tgz \
  --set clusters\[0\].name=my-confluent-cp-kafka \
  --set clusters\[0\].bootstrapBrokers=my-confluent-cp-kafka:9092 \
  --set pollIntervalSeconds=15 #5

# Helm could also create ServiceMonitor
