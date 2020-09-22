import argparse  # parse arguments from cli
import atexit # used to clear resources at exit of program (e.g. ctrl-c)
from kubernetes import client, config  # kubernetes api
from kubernetes.stream import stream
import logging  # logging
from os import path  # path utilities
import subprocess  # execute bash commands
import sys # for exit of program
import time  # process sleep
import yaml  # convert from file to yaml object

coreApi = None  # acces kubernetes core api
appsApi = None  # acces kubernetes apps api
customApi = None  # acces kubernetes custom object api
args = None  # CLI arguments


def load_variables():
    """Load the CLI variables given at the command line"""
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
    parser.add_argument('--reset', '-res',
                        dest='reset',
                        action="store_true",
                        help='Resets the environment before execution')
    parser.add_argument('--reset-only', '-reso',
                        dest='reset_only',
                        action="store_true",
                        help='Only resets the environment. Ignores all other parameters')

    args = parser.parse_args()
    print(args)


def initialize_kubernetes_api():
    """Load the kubernetes config from local or the cluster and creates
    needed APIs.
    """
    global coreApi, appsApi, customApi
    print('Connect to kubernetes api')
    try:
        config.load_kube_config()  # try using local config
    except config.config_exception.ConfigException as e:
        # load config from pod, if local config is not available
        logging.debug('Failed loading local Kubernetes configuration,'
                      + ' try from cluster')
        logging.debug(e)
        config.load_incluster_config()

    coreApi = client.CoreV1Api()
    appsApi = client.AppsV1Api()
    customApi = client.CustomObjectsApi()


def create_topics(topics):
    """Create the topics needed for the use cases
    :param topics: List of topics that should be created.
    """
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
        resp = stream(coreApi.connect_get_namespaced_pod_exec,
                      "kafka-client",
                      'default',
                      command=exec_command,
                      stderr=True, stdin=False,
                      stdout=True, tty=False)
        print(resp)


def load_yaml(file_path):
    """Creates a yaml file from the file at given path.
    :param file_path: The path to the file which contains the yaml.
    :return: The file as a yaml object.
    """
    try:
        f = open(path.join(path.dirname(__file__), file_path))
        with f:
            return yaml.safe_load(f)
    except Exception as e:
        logging.error('Error opening file %s' % file_path)
        logging.error(e)


def load_yaml_files():
    """Load the needed yaml files and creates objects from them.
    :return: wg, app_svc, app_svc_monitor ,app_jmx, app_deploy
    """
    print('Load kubernetes yaml files')
    wg = load_yaml('uc-workload-generator/base/workloadGenerator.yaml')
    app_svc = load_yaml('uc-application/base/aggregation-service.yaml')
    app_svc_monitor = load_yaml('uc-application/base/service-monitor.yaml')
    app_jmx = load_yaml('uc-application/base/jmx-configmap.yaml')
    app_deploy = load_yaml('uc-application/base/aggregation-deployment.yaml')

    print('Kubernetes yaml files loaded')
    return wg, app_svc, app_svc_monitor, app_jmx, app_deploy


def start_workload_generator(wg_yaml):
    """Starts the workload generator.
    :param wg_yaml: The yaml object for the workload generator.
    :return:
        The StatefulSet created by the API or in case it already exist/error
        the yaml object.
    """
    print('Start workload generator')

    num_sensors = args.dim_value
    wl_max_records = 150000
    wl_instances = int(((num_sensors + (wl_max_records - 1)) / wl_max_records))

    # set parameters special for uc 2
    if args.uc_id == '2':
        print('use uc2 stuff')
        num_nested_groups = args.dim_value
        num_sensors = '4'
        approx_num_sensors = int(num_sensors) ** num_nested_groups
        wl_instances = int(
            ((approx_num_sensors + wl_max_records - 1) / wl_max_records)
        )

    # Customize workload generator creations
    wg_yaml['spec']['replicas'] = wl_instances
    # TODO: acces over name of container
    # Set used use case
    wg_containter = wg_yaml['spec']['template']['spec']['containers'][0]
    wg_containter['image'] = 'theodolite/theodolite-uc' + args.uc_id + \
        '-workload-generator:latest'
    # TODO: acces over name of attribute
    # Set environment variables
    wg_containter['env'][0]['value'] = str(num_sensors)
    wg_containter['env'][1]['value'] = str(wl_instances)
    if args.uc_id == '2':  # Special configuration for uc2
        wg_containter['env'][2]['value'] = str(num_nested_groups)

    try:
        wg_ss = appsApi.create_namespaced_deployment(
            namespace="default",
            body=wg_yaml
        )
        print("Deployment '%s' created." % wg_ss.metadata.name)
        return wg_ss
    except client.rest.ApiException as e:
        print("Deployment creation error: %s" % e.reason)
        return wg_yaml


def start_application(svc_yaml, svc_monitor_yaml, jmx_yaml, deploy_yaml):
    """Applies the service, service monitor, jmx config map and start the
    use case application.

    :param svc_yaml: The yaml object for the service.
    :param svc_monitor_yaml: The yaml object for the service monitor.
    :param jmx_yaml: The yaml object for the jmx config map.
    :param deploy_yaml: The yaml object for the application.
    :return:
        The Service, ServiceMonitor, JMX ConfigMap and Deployment.
        In case the resource already exist/error the yaml object is returned.
        return svc, svc_monitor, jmx_cm, app_deploy
    """
    print('Start use case application')
    svc, svc_monitor, jmx_cm, app_deploy = None, None, None, None

    # Create Service
    try:
        svc = coreApi.create_namespaced_service(
            namespace="default", body=svc_yaml)
        print("Service '%s' created." % svc.metadata.name)
    except client.rest.ApiException as e:
        svc = svc_yaml
        logging.error("Service creation error: %s" % e.reason)

    # Create custom object service monitor
    try:
        svc_monitor = customApi.create_namespaced_custom_object(
            group="monitoring.coreos.com",
            version="v1",
            namespace="default",
            plural="servicemonitors",  # CustomResourceDef of ServiceMonitor
            body=svc_monitor_yaml,
        )
        print("ServiceMonitor '%s' created." % svc_monitor['metadata']['name'])
    except client.rest.ApiException as e:
        svc_monitor = svc_monitor_yaml
        logging.error("ServiceMonitor creation error: %s" % e.reason)

    # Apply jmx config map for aggregation service
    try:
        jmx_cm = coreApi.create_namespaced_config_map(
            namespace="default", body=jmx_yaml)
        print("ConfigMap '%s' created." % jmx_cm.metadata.name)
    except client.rest.ApiException as e:
        jmx_cm = jmx_yaml
        logging.error("ConfigMap creation error: %s" % e.reason)

    # Create deployment
    deploy_yaml['spec']['replicas'] = args.instances
    # TODO: acces over name of container
    app_container = deploy_yaml['spec']['template']['spec']['containers'][0]
    app_container['image'] = 'theodolite/theodolite-uc' + args.uc_id \
        + '-kstreams-app:latest'
    # TODO: acces over name of attribute
    app_container['env'][0]['value'] = str(args.commit_interval_ms)
    app_container['resources']['limits']['memory'] = args.memory_limit
    app_container['resources']['limits']['cpu'] = args.cpu_limit
    try:
        app_deploy = appsApi.create_namespaced_deployment(
            namespace="default",
            body=deploy_yaml
        )
        print("Deployment '%s' created." % app_deploy.metadata.name)
    except client.rest.ApiException as e:
        app_deploy = deploy_yaml
        logging.error("Deployment creation error: %s" % e.reason)

    return svc, svc_monitor, jmx_cm, app_deploy


def wait_execution():
    """Wait time while in execution."""
    print('Wait while executing')
    # TODO: ask which fits better
    # time.sleep(args.execution_minutes * 60)
    for i in range(args.execution_minutes):
        time.sleep(60)
        print(f"Executed: {i+1} minutes")
    print('Execution finished')
    return


def run_evaluation_script():
    """Runs the evaluation script."""
    # TODO: implement
    # # Run eval script
    # source ../.venv/bin/activate
    # python lag_analysis.py $EXP_ID uc1 $DIM_VALUE $INSTANCES
    #   $EXECUTION_MINUTES
    # deactivate
    return


def delete_resource(obj, del_func):
    """
    Helper function to delete kuberentes resources.
    First tries to delete with the kuberentes object.
    Then it uses the dict representation of yaml to delete the object.
    :param obj: Either kubernetes resource object or yaml as a dict.
    :param del_func: The function that need to be executed for deletion
    """
    try:
        del_func(obj.metadata.name, 'default')
    except Exception as e:
        logging.debug('Error deleting resource with api object, try with dict.')
        try:
            del_func(obj['metadata']['name'], 'default')
        except Exception as e:
            logging.error("Error deleting resource")
            logging.error(e)
            return
    print('Resource deleted')


def stop_applications(wg, app_svc, app_svc_monitor, app_jmx, app_deploy):
    """Stops the applied applications and delete resources.
    :param wg: The workload generator statefull set.
    :param app_svc: The application service.
    :param app_svc_monitor: The application service monitor.
    :param app_jmx: The application jmx config map.
    :param app_deploy: The application deployment.
    """
    print('Stop use case application and workload generator')

    print('Delete workload generator')
    delete_resource(wg, appsApi.delete_namespaced_deployment)

    print('Delete app service')
    delete_resource(app_svc, coreApi.delete_namespaced_service)

    print('Delete service monitor')
    try:
        customApi.delete_namespaced_custom_object(
            group="monitoring.coreos.com",
            version="v1",
            namespace="default",
            plural="servicemonitors",
            name=app_svc_monitor['metadata']['name'])
        print('Resource deleted')
    except Exception as e:
        print("Error deleting service monitor")

    print('Delete jmx config map')
    delete_resource(app_jmx, coreApi.delete_namespaced_config_map)

    print('Delete uc application')
    delete_resource(app_deploy, appsApi.delete_namespaced_deployment)
    return


def delete_topics(topics):
    """Delete topics from Kafka.
    :param topics: List of topics to delete.
    """
    print('Delete topics from Kafka')

    topics_delete = 'theodolite-.*|' + '|'.join([ti[0] for ti in topics])

    num_topics_command = [
        '/bin/sh',
        '-c',
        f'kafka-topics --zookeeper my-confluent-cp-zookeeper:2181 --list \
        | sed -n -E "/^({topics_delete})\
        ( - marked for deletion)?$/p" | wc -l'
    ]

    topics_deletion_command = [
        '/bin/sh',
        '-c',
        f'kafka-topics --zookeeper my-confluent-cp-zookeeper:2181 --delete \
        --topic "{topics_delete}"'
    ]

    # Wait that topics get deleted
    while True:
        # topic deletion, sometimes a second deletion seems to be required
        resp = stream(coreApi.connect_get_namespaced_pod_exec,
                      "kafka-client",
                      'default',
                      command=topics_deletion_command,
                      stderr=True, stdin=False,
                      stdout=True, tty=False)
        print(resp)

        print('Wait for topic deletion')
        time.sleep(5)
        resp = stream(coreApi.connect_get_namespaced_pod_exec,
                      "kafka-client",
                      'default',
                      command=num_topics_command,
                      stderr=True, stdin=False,
                      stdout=True, tty=False)
        if resp == '0':
            print("Topics deleted")
            break
    return


def reset_zookeeper():
    """Delete ZooKeeper configurations used for workload generation.
    """
    print('Delete ZooKeeper configurations used for workload generation')

    delete_zoo_data_command = [
        'kubectl',
        'exec',
        'zookeeper-client',
        '--',
        'bash',
        '-c',
        'zookeeper-shell my-confluent-cp-zookeeper:2181 deleteall '
        + '/workload-generation'
    ]

    check_zoo_data_command = [
        'kubectl',
        'exec',
        'zookeeper-client',
        '--',
        'bash',
        '-c',
        'zookeeper-shell my-confluent-cp-zookeeper:2181 get '
        + '/workload-generation'
    ]

    # Wait for configuration deletion
    while True:
        # Delete Zookeeper configuration data
        output = subprocess.run(delete_zoo_data_command,
                                capture_output=True,
                                text=True)
        logging.debug(output.stdout)

        # Check data is deleted
        output = subprocess.run(check_zoo_data_command,
                                capture_output=True,
                                text=True)
        logging.debug(output)

        if output.returncode == 1: # Means data not available anymore
            print('ZooKeeper reset was successful.')
            break
        else:
            print('ZooKeeper reset was not successful. Retrying in 5s.')
            time.sleep(5)
    return


def stop_lag_exporter():
    """
    Stop the lag exporter in order to reset it and allow smooth execution for
    next use cases.
    """
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


def reset_cluster(wg, app_svc, app_svc_monitor, app_jmx, app_deploy, topics):
    """
    Stop the applications, delete topics, reset zookeeper and stop lag exporter.
    """
    print('Reset cluster')
    stop_applications(wg, app_svc, app_svc_monitor, app_jmx, app_deploy)
    print('---------------------')
    delete_topics(topics)
    print('---------------------')
    reset_zookeeper()
    print('---------------------')
    stop_lag_exporter()

def main():
    load_variables()
    print('---------------------')

    wg, app_svc, app_svc_monitor, app_jmx, app_deploy = load_yaml_files()
    print('---------------------')

    initialize_kubernetes_api()
    print('---------------------')

    topics = [('input', args.partitions),
              ('output', args.partitions),
              ('aggregation-feedback', args.partitions),
              ('configuration', 1)]

    # Check for reset options
    if args.reset_only:
        # Only reset cluster an then end program
        reset_cluster(wg, app_svc, app_svc_monitor, app_jmx, app_deploy, topics)
        sys.exit()
    if args.reset:
        # Reset cluster before execution
        print('Reset only mode')
        reset_cluster(wg, app_svc, app_svc_monitor, app_jmx, app_deploy, topics)
        print('---------------------')

    # Register the reset operation so that is executed at the end of program
    atexit.register(reset_cluster, wg, app_svc, app_svc_monitor, app_jmx, app_deploy, topics)

    create_topics(topics)
    print('---------------------')

    wg = start_workload_generator(wg)
    print('---------------------')

    app_svc, app_svc_monitor, app_jmx, app_deploy = start_application(
        app_svc,
        app_svc_monitor,
        app_jmx,
        app_deploy)
    print('---------------------')

    wait_execution()
    print('---------------------')

    # Cluster is resetted with atexit method
    # reset_cluster(wg, app_svc, app_svc_monitor, app_jmx, app_deploy, topics)


if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO)
    main()
