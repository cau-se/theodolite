import argparse
import os

def env_list_default(env, tf):
    """
    Makes a list from an environment string.
    """
    v = os.environ.get(env)
    if v is not None:
        v = [tf(s) for s in v.split(',')]
    return v

def default_parser(description):
    """
    Returns the default parser that can be used for thodolite and run uc py
    :param description: The description the argument parser should show.
    """
    parser = argparse.ArgumentParser(description=description)
    parser.add_argument('--uc',
                        metavar='<uc>',
                        default=os.environ.get('UC'),
                        help='[mandatory] use case number, one of 1, 2, 3 or 4')
    parser.add_argument('--partitions', '-p',
                        metavar='<partitions>',
                        type=int,
                        default=os.environ.get('PARTITIONS', 40),
                        help='Number of partitions for Kafka topics')
    parser.add_argument('--cpu-limit', '-cpu',
                        metavar='<CPU limit>',
                        default=os.environ.get('CPU_LIMIT', '1000m'),
                        help='Kubernetes CPU limit')
    parser.add_argument('--memory-limit', '-mem',
                        metavar='<memory limit>',
                        default=os.environ.get('MEMORY_LIMIT', '4Gi'),
                        help='Kubernetes memory limit')
    parser.add_argument('--commit-ms',
                        metavar='<commit ms>',
                        type=int,
                        default=os.environ.get('COMMIT_MS', 100),
                        help='Kafka Streams commit interval in milliseconds')
    parser.add_argument('--duration', '-d',
                        metavar='<duration>',
                        type=int,
                        default=os.environ.get('DURATION', 5),
                        help='Duration in minutes subexperiments should be \
                                executed for')
    parser.add_argument('--namespace',
                        metavar='<NS>',
                        default=os.environ.get('NAMESPACE', 'default'),
                        help='Defines the Kubernetes where the applications should run')
    parser.add_argument('--reset',
                        action="store_true",
                        help='Resets the environment before execution')
    parser.add_argument('--reset-only',
                        action="store_true",
                        help='Only resets the environment. Ignores all other parameters')
    parser.add_argument('--prometheus',
                        metavar='<URL>',
                        default=os.environ.get('PROMETHEUS_BASE_URL', 'http://localhost:9090'),
                        help='Defines where to find the prometheus instance')
    parser.add_argument('--path',
                        metavar='<path>',
                        default=os.environ.get('RESULT_PATH', 'results'),
                        help='A directory path for the results')
    return parser

def benchmark_parser(description):
    """
    Parser for the overall benchmark execution
    :param description: The description the argument parser should show.
    """
    parser = default_parser(description)

    parser.add_argument('--loads',
                        metavar='<load>',
                        type=int,
                        nargs='+',
                        default=env_list_default('LOADS', int),
                        help='[mandatory] Loads that should be executed')
    parser.add_argument('--instances', '-i',
                        dest='instances_list',
                        metavar='<instances>',
                        type=int,
                        nargs='+',
                        default=env_list_default('INSTANCES', int),
                        help='[mandatory] List of instances used in benchmarks')
    parser.add_argument('--domain-restriction',
                        action="store_true",
                        help='To use domain restriction. For details see README')
    parser.add_argument('--search-strategy',
                        metavar='<strategy>',
                        default=os.environ.get('SEARCH_STRATEGY', 'default'),
                        help='The benchmarking search strategy. Can be set to default, linear-search or binary-search')
    return parser

def execution_parser(description):
    """
    Parser for executing one use case
    :param description: The description the argument parser should show.
    """
    parser = default_parser(description)
    parser.add_argument('--exp-id',
                        metavar='<exp id>',
                        default=os.environ.get('EXP_ID'),
                        help='[mandatory] ID of the experiment')
    parser.add_argument('--load',
                        metavar='<load>',
                        type=int,
                        default=os.environ.get('LOAD'),
                        help='[mandatory] Load that should be used for benchmakr')
    parser.add_argument('--instances',
                        metavar='<instances>',
                        type=int,
                        default=os.environ.get('INSTANCES'),
                        help='[mandatory] Numbers of instances to be benchmarked')
    return parser
