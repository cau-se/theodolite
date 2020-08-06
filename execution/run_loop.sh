#!/usr/bin/env python3.7

import sys
import os
from strategies.config import ExperimentConfig
import strategies.strategies.default_strategy as default_strategy
from strategies.experiment_execution import ExperimentExecutor
import strategies.subexperiment_execution.subexperiment_executor as subexperiment_executor

uc=sys.argv[1]
dim_values=sys.argv[2].split(',')
replicas=sys.argv[3].split(',')
partitions=sys.argv[4] if len(sys.argv) >= 5 and sys.argv[4] else 40
cpu_limit=sys.argv[5] if len(sys.argv) >= 6 and sys.argv[5] else "1000m"
memory_limit=sys.argv[6] if len(sys.argv) >= 7 and sys.argv[6] else "4Gi"
kafka_streams_commit_interval_ms=sys.argv[7] if len(sys.argv) >= 8 and sys.argv[7] else 100
execution_minutes=sys.argv[8] if len(sys.argv) >= 9 and sys.argv[8] else 5
benchmark_strategy=sys.argv[9] if len(sys.argv) >= 10 and sys.argv[9] else "default"

print("Chosen benchmarking strategy: "+benchmark_strategy)
print("Going to execute " + str(len(dim_values)*len(replicas)) + " subexperiments in total..")

experiment_config = ExperimentConfig(uc, dim_values, replicas, partitions, cpu_limit, memory_limit, kafka_streams_commit_interval_ms, execution_minutes, default_strategy, subexperiment_executor)
executor = ExperimentExecutor(experiment_config)
executor.execute()