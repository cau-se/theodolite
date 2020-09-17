#!/usr/bin/env python

import sys
import os
from strategies.config import ExperimentConfig
import strategies.strategies.domain_restriction.lower_bound_strategy as lower_bound_strategy
import strategies.strategies.domain_restriction.no_lower_bound_strategy as no_lower_bound_strategy
import strategies.strategies.search.check_all_strategy as check_all_strategy
import strategies.strategies.search.linear_search_strategy as linear_search_strategy
import strategies.strategies.search.binary_search_strategy as binary_search_strategy
from strategies.experiment_execution import ExperimentExecutor
import strategies.subexperiment_execution.subexperiment_executor as subexperiment_executor
import strategies.subexperiment_evaluation.subexperiment_evaluator as subexperiment_evaluator

uc=sys.argv[1]
dim_values=sys.argv[2].split(',')
replicas=sys.argv[3].split(',')
partitions=sys.argv[4] if len(sys.argv) >= 5 and sys.argv[4] else 40
cpu_limit=sys.argv[5] if len(sys.argv) >= 6 and sys.argv[5] else "1000m"
memory_limit=sys.argv[6] if len(sys.argv) >= 7 and sys.argv[6] else "4Gi"
kafka_streams_commit_interval_ms=sys.argv[7] if len(sys.argv) >= 8 and sys.argv[7] else 100
execution_minutes=sys.argv[8] if len(sys.argv) >= 9 and sys.argv[8] else 5
domain_restriction=bool(sys.argv[9]) if len(sys.argv) >= 10 and sys.argv[9] == "restrict-domain" else False
search_strategy=sys.argv[10] if len(sys.argv) >= 11 and (sys.argv[10] == "linear-search" or sys.argv[10] == "binary-search") else "default"

print(f"Domain restriction of search space activated: {domain_restriction}")
print(f"Chosen search strategy: {search_strategy}")

# use h3
if domain_restriction:
    # use h1 & h3
    if search_strategy == "linear-search":
        print(f"Going to execute at most {len(dim_values)+len(replicas)-1} subexperiments in total..")
        experiment_config = ExperimentConfig(
            use_case=uc,
            dim_values=dim_values,
            replicass=replicas,
            partitions=partitions,
            cpu_limit=cpu_limit,
            memory_limit=memory_limit,
            kafka_streams_commit_interval_ms=kafka_streams_commit_interval_ms,
            execution_minutes=execution_minutes,
            domain_restriction_strategy=lower_bound_strategy,
            search_strategy=linear_search_strategy,
            subexperiment_executor=subexperiment_executor,
            subexperiment_evaluator=subexperiment_evaluator)
    # use h2 & h3
    elif search_strategy == "binary-search":
        experiment_config = ExperimentConfig(
            use_case=uc,
            dim_values=dim_values,
            replicass=replicas,
            partitions=partitions,
            cpu_limit=cpu_limit,
            memory_limit=memory_limit,
            kafka_streams_commit_interval_ms=kafka_streams_commit_interval_ms,
            execution_minutes=execution_minutes,
            domain_restriction_strategy=lower_bound_strategy,
            search_strategy=binary_search_strategy,
            subexperiment_executor=subexperiment_executor,
            subexperiment_evaluator=subexperiment_evaluator)
    # use h0 & h3
    else:
        print(f"Going to execute {len(dim_values)*len(replicas)} subexperiments in total..")
        experiment_config = ExperimentConfig(
            use_case=uc,
            dim_values=dim_values,
            replicass=replicas,
            partitions=partitions,
            cpu_limit=cpu_limit,
            memory_limit=memory_limit,
            kafka_streams_commit_interval_ms=kafka_streams_commit_interval_ms,
            execution_minutes=execution_minutes,
            domain_restriction_strategy=lower_bound_strategy,
            search_strategy=check_all_strategy,
            subexperiment_executor=subexperiment_executor,
            subexperiment_evaluator=subexperiment_evaluator)
# not use h3
else:
    # use h1 & !h3
    if search_strategy == "linear-search":
        print(f"Going to execute at most {len(dim_values)+len(replicas)-1} subexperiments in total..")
        experiment_config = ExperimentConfig(
            use_case=uc,
            dim_values=dim_values,
            replicass=replicas,
            partitions=partitions,
            cpu_limit=cpu_limit,
            memory_limit=memory_limit,
            kafka_streams_commit_interval_ms=kafka_streams_commit_interval_ms,
            execution_minutes=execution_minutes,
            domain_restriction_strategy=no_lower_bound_strategy,
            search_strategy=linear_search_strategy,
            subexperiment_executor=subexperiment_executor,
            subexperiment_evaluator=subexperiment_evaluator)
    # use h2 & !h3
    elif search_strategy == "binary-search":
        experiment_config = ExperimentConfig(
            use_case=uc,
            dim_values=dim_values,
            replicass=replicas,
            partitions=partitions,
            cpu_limit=cpu_limit,
            memory_limit=memory_limit,
            kafka_streams_commit_interval_ms=kafka_streams_commit_interval_ms,
            execution_minutes=execution_minutes,
            domain_restriction_strategy=no_lower_bound_strategy,
            search_strategy=binary_search_strategy,
            subexperiment_executor=subexperiment_executor,
            subexperiment_evaluator=subexperiment_evaluator)
    # use h0 & !h3
    else:
        print(f"Going to execute {len(dim_values)*len(replicas)} subexperiments in total..")
        experiment_config = ExperimentConfig(
            use_case=uc,
            dim_values=dim_values,
            replicass=replicas,
            partitions=partitions,
            cpu_limit=cpu_limit,
            memory_limit=memory_limit,
            kafka_streams_commit_interval_ms=kafka_streams_commit_interval_ms,
            execution_minutes=execution_minutes,
            domain_restriction_strategy=no_lower_bound_strategy,
            search_strategy=check_all_strategy,
            subexperiment_executor=subexperiment_executor,
            subexperiment_evaluator=subexperiment_evaluator)

executor = ExperimentExecutor(experiment_config)
executor.execute()