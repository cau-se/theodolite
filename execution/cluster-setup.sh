#!/bin/bash

CP_HELM_PATH='../../cp-helm-charts'

## minikube ##
# if minikube stop responding after few minutes

# minikube delete 

# cd 
# rm -r .minikube
# cd Dokumente/Master-2-SoSe-2020/project/spesb/execution/

#minikube config set memory  4046
#minikube delete
#minikube config set cpus 4 
#minikube delete
#minikube start --vm-driver=virtualbox

## kind ## 
kind delete cluster 
kind create cluster # --config  infrastructure/cluster/kind-configuration.yaml 

# K8s dashboard
# Token: eyJhbGciOiJSUzI1NiIsImtpZCI6ImdGNlU1U3BnN01XcS14RnlWUFRBODlaTzNpeUtxa1hTV3VKNTVmVGVrZ2MifQ.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJrdWJlcm5ldGVzLWRhc2hib2FyZCIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJkZWZhdWx0LXRva2VuLW5rbnc1Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQubmFtZSI6ImRlZmF1bHQiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC51aWQiOiI2NDE5NTUzYy0yY2RkLTQ2OGUtYTMwNS03YzZlNWQ5NjhmMzMiLCJzdWIiOiJzeXN0ZW06c2VydmljZWFjY291bnQ6a3ViZXJuZXRlcy1kYXNoYm9hcmQ6ZGVmYXVsdCJ9.xkkZJViw4Q7RUbpzWjmTUGIGlgHfIhC94GuJ_bmGId3w8UxCP5PK6eq0yPNTTMJMT-yGr3qH7D1f616UNDJM1SrcoervG1fXyzw0XYmdbXluemW1LIm3WyzukhBs4dF4s93RrxUd9iHFjCrQanssXOSDDCZO-2V4BrpYEZ4TLvgMz9pAy4_k4-1gL4QKu8FBgCydBa2SBVOZ8tFy_5r38KH7j9eX0OJD8kyugcmPz0ARaIZhyZERyTHz3wmxY5E-W_qSe1GY12EeR9c5KlWeGYYIzheyBr-TpcyLuoQQguxmI7Ico917k0zG2YBSEYdfTo1I2LPXCYzbp__MAIV_TQ
# kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v2.0.0/aio/deploy/recommended.yaml
# kubectl proxy & 

# prometheus
helm install prometheus-operator stable/prometheus-operator -f infrastructure/prometheus/helm-values.yaml
kubectl apply -f infrastructure/prometheus/service-account.yaml
kubectl apply -f infrastructure/prometheus/cluster-role.yaml
kubectl apply -f infrastructure/prometheus/cluster-role-binding.yaml
kubectl apply -f infrastructure/prometheus/prometheus.yaml

# grafana 
kubectl apply -f infrastructure/grafana/prometheus-datasource-config-map.yaml
kubectl apply -f infrastructure/grafana/dashboard-config-map.yaml
helm install grafana stable/grafana -f infrastructure/grafana/values.yaml

# kafka + lag-exporter
helm install my-confluent $CP_HELM_PATH  -f infrastructure/kafka/values.yaml
kubectl apply -f $CP_HELM_PATH/examples/kafka-client.yaml 
kubectl apply -f infrastructure/kafka/service-monitor.yaml

helm install kafka-lag-exporter https://github.com/lightbend/kafka-lag-exporter/releases/download/v0.6.0/kafka-lag-exporter-0.6.0.tgz -f infrastructure/kafka-lag-exporter/values.yaml
kubectl apply -f infrastructure/kafka-lag-exporter/service-monitor.yaml

# port fowarding kubectl "port-forward --namespace monitoring <pod name> <local port>:<container port>"
sleep 3m # wait for grafana and prometheus pods
kubectl port-forward $(kubectl get pods  -o name | grep grafana) 3000:3000 & 
kubectl port-forward $(kubectl get pods  -o name | grep prometheus-prometheus) 9090:9090 &

# open web interfaces
# xdg-open http://localhost:8001/api/v1/namespaces/kubernetes-dashboard/services/https:kubernetes-dashboard:/proxy/. 
xdg-open http://localhost:9090
xdg-open http://localhost:3000

# grafana token
# kubectl get secret --namespace default grafana -o jsonpath="{.data.admin-password}" | base64 --decode ; echo
