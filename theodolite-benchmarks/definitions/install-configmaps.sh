# flink 
kubectl create configmap benchmark-resources-uc1-flink --from-file uc1-flink/resources
kubectl create configmap benchmark-resources-uc2-flink --from-file uc2-flink/resources
kubectl create configmap benchmark-resources-uc3-flink --from-file uc3-flink/resources
kubectl create configmap benchmark-resources-uc4-flink --from-file uc4-flink/resources

# kafka
kubectl create configmap benchmark-resources-uc1-kstreams --from-file uc1-kstreams/resources
kubectl create configmap benchmark-resources-uc2-kstreams --from-file uc2-kstreams/resources
kubectl create configmap benchmark-resources-uc3-kstreams --from-file uc3-kstreams/resources
kubectl create configmap benchmark-resources-uc4-kstreams --from-file uc4-kstreams/resources

# load generator
kubectl create configmap benchmark-resources-uc1-loadgen --from-file uc1-loadGen
kubectl create configmap benchmark-resources-uc2-loadgen --from-file uc2-loadGen
kubectl create configmap benchmark-resources-uc3-loadgen --from-file uc3-loadGen
kubectl create configmap benchmark-resources-uc4-loadgen --from-file uc4-loadGen