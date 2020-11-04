import argparse

def default_parser(description):
    """
    Returns the default parser that can be used for thodolite and run uc py
    :param description: The description the argument parser should show.
    """
    parser = argparse.ArgumentParser(description=description)
    parser.add_argument('--uc',
                        metavar='<uc>',
                        help='[mandatory] use case number, one of 1, 2, 3 or 4')
    parser.add_argument('--partitions', '-p',
                        default=40,
                        type=int,
                        metavar='<partitions>',
                        help='Number of partitions for Kafka topics')
    parser.add_argument('--cpu-limit', '-cpu',
                        default='1000m',
                        metavar='<CPU limit>',
                        help='Kubernetes CPU limit')
    parser.add_argument('--memory-limit', '-mem',
                        default='4Gi',
                        metavar='<memory limit>',
                        help='Kubernetes memory limit')
    parser.add_argument('--commit-ms',
                        default=100,
                        type=int,
                        metavar='<commit ms>',
                        help='Kafka Streams commit interval in milliseconds')
    parser.add_argument('--duration', '-d',
                        default=5,
                        type=int,
                        metavar='<duration>',
                        help='Duration in minutes subexperiments should be \
                                executed for')
    parser.add_argument('--reset',
                        action="store_true",
                        help='Resets the environment before execution')
    parser.add_argument('--reset-only',
                        action="store_true",
                        help='Only resets the environment. Ignores all other parameters')
    return parser

def benchmark_parser(description):
    """
    Parser for the overall benchmark execution
    :param description: The description the argument parser should show.
    """
    parser = default_parser(description)

    parser.add_argument('--loads',
                        type=int,
                        metavar='<load>',
                        nargs='+',
                        help='[mandatory] Loads that should be executed')
    parser.add_argument('--instances', '-i',
                        dest='instances_list',
                        type=int,
                        metavar='<instances>',
                        nargs='+',
                        help='[mandatory] List of instances used in benchmarks')
    parser.add_argument('--domain-restriction',
                        action="store_true",
                        help='To use domain restriction. For details see README')
    parser.add_argument('--search-strategy',
                        default='default',
                        metavar='<strategy>',
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
                        help='[mandatory] ID of the experiment')
    parser.add_argument('--load',
                        type=int,
                        metavar='<load>',
                        help='[mandatory] Load that should be used for benchmakr')
    parser.add_argument('--instances',
                        type=int,
                        metavar='<instances>',
                        help='[mandatory] Numbers of instances to be benchmarked')
    return parser
