#!/usr/bin/env python

import argparse
import logging  # logging
import os
import sys
from strategies.config import ExperimentConfig
import strategies.strategies.domain_restriction.lower_bound_strategy as lower_bound_strategy
import strategies.strategies.domain_restriction.no_lower_bound_strategy as no_lower_bound_strategy
import strategies.strategies.search.check_all_strategy as check_all_strategy
import strategies.strategies.search.linear_search_strategy as linear_search_strategy
import strategies.strategies.search.binary_search_strategy as binary_search_strategy
from strategies.experiment_execution import ExperimentExecutor
import strategies.subexperiment_execution.subexperiment_executor as subexperiment_executor
import strategies.subexperiment_evaluation.subexperiment_evaluator as subexperiment_evaluator


def load_variables():
    """Load the CLI variables given at the command line"""
    print('Load CLI variables')
    parser = argparse.ArgumentParser(description='Run use case Programm')
    parser.add_argument('uc',
                        metavar='<uc>',
                        help='use case number, one of 1, 2, 3 or 4')
    parser.add_argument('loads',
                        type=int,
                        metavar='<load>',
                        nargs='+',
                        help='Loads that should be executed')
    parser.add_argument('--instances', '-i',
                        dest='instances_list',
                        default=[1],
                        type=int,
                        metavar='<instance>',
                        nargs='+',
                        help='List of instances used in benchmarks')
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
    parser.add_argument('--domain-restriction',
                        action="store_true",
                        help='To use domain restriction. For details see README')
    parser.add_argument('--search-strategy',
                        default='default',
                        metavar='<strategy>',
                        help='The benchmarking search strategy. Can be set to default, linear-search or binary-search')
    parser.add_argument('--reset',
                        action="store_true",
                        help='Resets the environment before execution')
    parser.add_argument('--reset-only',
                        action="store_true",
                        help='Only resets the environment. Ignores all other parameters')
    args = parser.parse_args()
    print(args)
    return args


def main(uc, loads, instances_list, partitions, cpu_limit, memory_limit, commit_ms, duration, domain_restriction, search_strategy, reset, reset_only):

    print(f"Domain restriction of search space activated: {domain_restriction}")
    print(f"Chosen search strategy: {search_strategy}")

    if os.path.exists("exp_counter.txt"):
        with open("exp_counter.txt", mode="r") as read_stream:
            exp_id = int(read_stream.read())
    else:
        exp_id = 0

    with open("exp_counter.txt", mode="w") as write_stream:
        write_stream.write(str(exp_id + 1))

    # domain restriction
    if domain_restriction:
        # domain restriction + linear-search
        if search_strategy == "linear-search":
            print(f"Going to execute at most {len(loads)+len(instances_list)-1} subexperiments in total..")
            experiment_config = ExperimentConfig(
                use_case=uc,
                exp_id=exp_id,
                dim_values=loads,
                replicass=instances_list,
                partitions=partitions,
                cpu_limit=cpu_limit,
                memory_limit=memory_limit,
                kafka_streams_commit_interval_ms=commit_ms,
                execution_minutes=duration,
                domain_restriction_strategy=lower_bound_strategy,
                search_strategy=linear_search_strategy,
                subexperiment_executor=subexperiment_executor,
                subexperiment_evaluator=subexperiment_evaluator)
        # domain restriction + binary-search
        elif search_strategy == "binary-search":
            experiment_config = ExperimentConfig(
                use_case=uc,
                exp_id=exp_id,
                dim_values=loads,
                replicass=instances_list,
                partitions=partitions,
                cpu_limit=cpu_limit,
                memory_limit=memory_limit,
                kafka_streams_commit_interval_ms=commit_ms,
                execution_minutes=duration,
                domain_restriction_strategy=lower_bound_strategy,
                search_strategy=binary_search_strategy,
                subexperiment_executor=subexperiment_executor,
                subexperiment_evaluator=subexperiment_evaluator)
        # domain restriction + check_all
        else:
            print(f"Going to execute {len(loads)*len(instances_list)} subexperiments in total..")
            experiment_config = ExperimentConfig(
                use_case=uc,
                exp_id=exp_id,
                dim_values=loads,
                replicass=instances_list,
                partitions=partitions,
                cpu_limit=cpu_limit,
                memory_limit=memory_limit,
                kafka_streams_commit_interval_ms=commit_ms,
                execution_minutes=duration,
                domain_restriction_strategy=lower_bound_strategy,
                search_strategy=check_all_strategy,
                subexperiment_executor=subexperiment_executor,
                subexperiment_evaluator=subexperiment_evaluator)
    # no domain restriction
    else:
        # no domain restriction + linear-search
        if search_strategy == "linear-search":
            print(f"Going to execute at most {len(loads)*len(instances_list)} subexperiments in total..")
            experiment_config = ExperimentConfig(
                use_case=uc,
                exp_id=exp_id,
                dim_values=loads,
                replicass=instances_list,
                partitions=partitions,
                cpu_limit=cpu_limit,
                memory_limit=memory_limit,
                kafka_streams_commit_interval_ms=commit_ms,
                execution_minutes=duration,
                domain_restriction_strategy=no_lower_bound_strategy,
                search_strategy=linear_search_strategy,
                subexperiment_executor=subexperiment_executor,
                subexperiment_evaluator=subexperiment_evaluator)
        # no domain restriction + binary-search
        elif search_strategy == "binary-search":
            experiment_config = ExperimentConfig(
                use_case=uc,
                exp_id=exp_id,
                dim_values=loads,
                replicass=instances_list,
                partitions=partitions,
                cpu_limit=cpu_limit,
                memory_limit=memory_limit,
                kafka_streams_commit_interval_ms=commit_ms,
                execution_minutes=duration,
                domain_restriction_strategy=no_lower_bound_strategy,
                search_strategy=binary_search_strategy,
                subexperiment_executor=subexperiment_executor,
                subexperiment_evaluator=subexperiment_evaluator)
        # no domain restriction + check_all
        else:
            print(f"Going to execute {len(loads)*len(instances_list)} subexperiments in total..")
            experiment_config = ExperimentConfig(
                use_case=uc,
                exp_id=exp_id,
                dim_values=loads,
                replicass=instances_list,
                partitions=partitions,
                cpu_limit=cpu_limit,
                memory_limit=memory_limit,
                kafka_streams_commit_interval_ms=commit_ms,
                execution_minutes=duration,
                domain_restriction_strategy=no_lower_bound_strategy,
                search_strategy=check_all_strategy,
                subexperiment_executor=subexperiment_executor,
                subexperiment_evaluator=subexperiment_evaluator)

    executor = ExperimentExecutor(experiment_config)
    executor.execute()


if __name__ == '__main__':
    logging.basicConfig(level=logging.INFO)
    args = load_variables()
    main(args.uc, args.loads, args.instances_list, args.partitions, args.cpu_limit,
         args.memory_limit, args.commit_ms, args.duration,
         args.domain_restriction, args.search_strategy, args.reset,
         args.reset_only)
