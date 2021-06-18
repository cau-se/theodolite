cd uc1-flink/
kubectl create configmap benchmark-resources-uc1-flink --from-file .
cd ../uc2-flink/
kubectl create configmap benchmark-resources-uc2-flink --from-file .
cd ../uc3-flink/
kubectl create configmap benchmark-resources-uc3-flink --from-file .
cd ../uc4-flink/
kubectl create configmap benchmark-resources-uc4-flink --from-file .
cd ../uc1-kstreams/
kubectl create configmap benchmark-resources-uc1-kstreams --from-file .
cd ../uc2-kstreams/
kubectl create configmap benchmark-resources-uc2-kstreams --from-file .
cd ../uc3-kstreams/
kubectl create configmap benchmark-resources-uc3-kstreams --from-file .
cd ../uc4-kstreams/
kubectl create configmap benchmark-resources-uc4-kstreams --from-file .
cd ..