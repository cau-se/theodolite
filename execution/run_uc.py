from kubernetes import client, config  # kubernetes api
from kubernetes.stream import stream
import argparse  # parse arguments from cli
from os import path # path utilities
import subprocess  # execute bash commands
import time  # process sleep
import yaml # convert from file to yaml object

coreApi = None  # acces kubernetes core api
appsApi = None  # acces kubernetes apps api
customApi = None  # acces kubernetes custom object api
args = None  # CLI arguments


def load_variables():
    global args
    print('Load CLI variables')
    parser = argparse.ArgumentParser(description='Run use case Programm')
    parser.add_argument('--use-case', '-uc',
                        dest='uc_id',
                        default='1',
                        metavar='UC_NUMBER',
                        help='use case number, one of 1, 2, 3 or 4')
    parser.add_argument('--dim-value', '-d',
                        dest='dim_value',
                        default=10000,
                        type=int,
                        metavar='DIM_VALUE',
                        help='Value for the workload generator to be tested')
    parser.add_argument('--instances', '-i',
                        dest='instances',
                        default=1,
                        type=int,
                        metavar='INSTANCES',
                        help='Numbers of instances to be benchmarked')
    parser.add_argument('--partitions', '-p',
                        dest='partitions',
                        default=40,
                        type=int,
                        metavar='PARTITIONS',
                        help='Number of partitions for Kafka topics')
    parser.add_argument('--cpu-limit', '-cpu',
                        dest='cpu_limit',
                        default='1000m',
                        metavar='CPU_LIMIT',
                        help='Kubernetes CPU limit')
    parser.add_argument('--memory-limit', '-mem',
                        dest='memory_limit',
                        default='4Gi',
                        metavar='MEMORY_LIMIT',
                        help='Kubernetes memory limit')
    parser.add_argument('--commit-interval', '-ci',
                        dest='commit_interval_ms',
                        default=100,
                        type=int,
                        metavar='KAFKA_STREAMS_COMMIT_INTERVAL_MS',
                        help='Kafka Streams commit interval in milliseconds')
    parser.add_argument('--executions-minutes', '-exm',
                        dest='execution_minutes',
                        default=5,
                        type=int,
                        metavar='EXECUTION_MINUTES',
                        help='Duration in minutes subexperiments should be \
                                executed for')

    args = parser.parse_args()
    print(args)


def initialize_kubernetes_api():
    global coreApi, appsApi, customApi
    print('Connect to kubernetes api')
    try:
        config.load_kube_config()  # try using local config
    except config.config_exception.ConfigException:
        # load config from pod, if local config is not available
        config.load_incluster_config()

    coreApi = client.CoreV1Api()
    appsApi = client.AppsV1Api()
    customApi = client.CustomObjectsApi()


def create_topics(topics):
    # Calling exec and waiting for response
    print('Create topics')
    for (topic, partitions) in topics:
        print('Create topic ' + topic + ' with #' + str(partitions)
              + ' partitions')
        exec_command = [
            '/bin/sh',
            '-c',
            f'kafka-topics --zookeeper my-confluent-cp-zookeeper:2181\
            --create --topic {topic} --partitions {partitions}\
            --replication-factor 1'
        ]
        resp = stream(v1.connect_get_namespaced_pod_exec,
                      "kafka-client",
                      'default',
                      command=exec_command,
                      stderr=True, stdin=False,
                      stdout=True, tty=False)
        print(resp)


def start_workload_generator():
    print('Start workload generator')
    num_sensors = args.dim_value
    wl_max_records = 150000
    # TODO: How is this calculation done?
    wl_instances = int(((num_sensors + (wl_max_records - 1)) / wl_max_records))

    parameters_path = 'uc-workload-generator/overlay/uc' + args.uc_id\
        + '-workload-generator'
    f = open(parameters_path + '/set_paramters.yaml', 'w')
    f.write('\
apiVersion: apps/v1\n\
kind: StatefulSet\n\
metadata:\n\
  name: titan-ccp-load-generator\n\
spec:\n\
  replicas: ' + str(wl_instances) + '\n\
  template:\n\
    spec:\n\
      containers:\n\
      - name: workload-generator\n\
        env:\n\
        - name: NUM_SENSORS\n\
          value: "' + str(num_sensors) + '"\n\
        - name: INSTANCES\n\
          value: "' + str(wl_instances) + '"\n')
    f.close()

    exec_command = [
        'kubectl',
        'apply',
        '-k',
        parameters_path
    ]
    output = subprocess.run(exec_command, capture_output=True, text=True)
    print(output)
    return


def start_application():
    print('Start use case applications')

    # Apply aggregation service
    with open(path.join(path.dirname(__file__), "uc-application/base/aggregation-service.yaml")) as f:
        dep = yaml.safe_load(f)
        try:
            resp = coreApi.create_namespaced_service(
                namespace="default", body=dep)
            print("Service '%s' created." % resp.metadata.name)
        except:
            print("Service creation error.")

    # Apply jmx config map for aggregation service
    with open(path.join(path.dirname(__file__), "uc-application/base/jmx-configmap.yaml")) as f:
        dep = yaml.safe_load(f)
        try:
            resp = coreApi.create_namespaced_config_map(
                namespace="default", body=dep)
            print("ConfigMap '%s' created." % resp.metadata.name)
        except:
            print("ConfigMap creation error.")

    # Create custom object service monitor
    with open(path.join(path.dirname(__file__), "uc-application/base/service-monitor.yaml")) as f:
        dep = yaml.safe_load(f)
        try:
            resp = customApi.create_namespaced_custom_object(
                group="monitoring.coreos.com",
                version="v1",
                namespace="default",
                plural="servicemonitors", # From CustomResourceDefinition of ServiceMonitor
                body=dep,
            )
            print("ServiceMonitor '%s' created." % resp['metadata']['name'])
        except:
            print("ServiceMonitor creation error")

    # Create deployment
    with open(path.join(path.dirname(__file__), "uc-application/base/aggregation-deployment.yaml")) as f:
        dep = yaml.safe_load(f)
        dep['spec']['replicas'] = args.instances
        uc_container = dep['spec']['template']['spec']['containers'][0]
        uc_container['image'] = 'soerenhenning/uc1-app:latest'
        uc_container['env'][1]['value'] = str(args.commit_interval_ms)
        uc_container['resources']['limits']['memory'] = str(args.memory_limit)
        uc_container['resources']['limits']['cpu'] = str(args.cpu_limit) # cpu limit is already str
        try:
            resp = appsApi.create_namespaced_deployment(
                namespace="default",
                body=dep
            )
            print("Deployment '%s' created." % resp.metadata.name)
        except client.rest.ApiException as e:
            print("Deployment creation error: %s" % e.reason)
    return


def wait_execution():
    print('Wait while executing')
    # TODO: ask which fits better
    # time.sleep(args.execution_minutes * 60)
    for i in range(args.execution_minutes):
        time.sleep(60)
        print(f"Executed: {i+1} minutes")
    print('Execution finished')
    return


def run_evaluation_script():
    # TODO: implement
    # # Run eval script
    # source ../.venv/bin/activate
    # python lag_analysis.py $EXP_ID uc1 $DIM_VALUE $INSTANCES
    #   $EXECUTION_MINUTES
    # deactivate
    return


def stop_applications():
    print('Stop use case application and workload generator')
    exec_command = [
        'kubectl',
        'delete',
        '-k',
        'uc-workload-generator/overlay/uc' + args.uc_id + '-workload-generator'
    ]
    output = subprocess.run(exec_command, capture_output=True, text=True)
    print(output)

    exec_command[3] = 'uc-application/overlay/uc' + args.uc_id + '-application'
    output = subprocess.run(exec_command, capture_output=True, text=True)
    print(output)
    return


def delete_topics(topics):
    print('Delete topics from Kafka')

    num_topics_command = [
        '/bin/sh',
        '-c',
        f'kafka-topics --zookeeper my-confluent-cp-zookeeper:2181 --list | sed -n -E "/^(theodolite-.*|input|output|configuration)( - marked for deletion)?$/p" | wc -l'
    ]

    topics_deletion_command = [
        '/bin/sh',
        '-c',
        f'kafka-topics --zookeeper my-confluent-cp-zookeeper:2181 --delete --topic "input|output|configuration|theodolite-.*"'
    ]

    # Wait that topics get deleted
    while True:
        # topic deletion, sometimes a second deletion seems to be required
        resp = stream(v1.connect_get_namespaced_pod_exec,
                      "kafka-client",
                      'default',
                      command=topics_deletion_command,
                      stderr=True, stdin=False,
                      stdout=True, tty=False)
        print(resp)

        print('Wait for topic deletion')
        time.sleep(5)
        resp = stream(v1.connect_get_namespaced_pod_exec,
                      "kafka-client",
                      'default',
                      command=num_topics_command,
                      stderr=True, stdin=False,
                      stdout=True, tty=False)
        if resp == '0':
            print("Topics deleted")
            break
    return


# Stop the lag exporter in order to reset it and allow smooth execution for
# next use cases
def stop_lag_exporter():
    print('Stop the lag exporter')

    find_pod_command = [
        'kubectl',
        'get',
        'pod',
        '-l',
        'app.kubernetes.io/name=kafka-lag-exporter',
        '-o',
        'jsonpath="{.items[0].metadata.name}"'
    ]
    output = subprocess.run(find_pod_command, capture_output=True, text=True)
    lag_exporter_pod = output.stdout.replace('"', '')
    delete_pod_command = [
        'kubectl',
        'delete',
        'pod',
        lag_exporter_pod
    ]
    output = subprocess.run(delete_pod_command, capture_output=True, text=True)
    print(output)
    return


def main():
    load_variables()
    print('---------------------')

    topics = [('input', args.partitions),
              ('output', args.partitions),
              ('configuration', 1)]

    initialize_kubernetes_api()
    print('---------------------')
    create_topics(topics)
    print('---------------------')
    start_workload_generator()
    print('---------------------')
    start_application()
    print('---------------------')
    wait_execution()
    print('---------------------')
    stop_applications()
    print('---------------------')
    delete_topics(topics)
    print('---------------------')
    stop_lag_exporter()


if __name__ == '__main__':
    main()
