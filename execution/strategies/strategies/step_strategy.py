# Contains the default strategy that executes a subexperiment for all combinations of instances and dimension values.

import os
from .config import SubexperimentConfig

def execute(config):
    subexperiment_counter=0
    subexperiments_total=len(config.dim_values)*len(config.replicas)
    i=0
    j=0
    while i < len(config.replicas) and j < len(config.dim_values):
        subexperiment_counter+=1
        print(f"Run subexperiment {subexperiment_counter}/{subexperiments_total} with dimension value {config.dim_values[j]} and {config.replicas[i]} replicas.")

        subexperiment_config = SubexperimentConfig(config.use_case, subexperiment_counter, config.dim_values[j], config.replicas[i], config.partitions, config.cpu_limit, config.memory_limit, config.kafka_streams_commit_interval_ms, config.execution_minutes)

        config.subexperiment_executor.execute(subexperiment_config)
        result = config.subexperiment_evaluator.execute()
        if result == 1:
            j+=1
        else:
            i+=1

    print(f"Executed {subexperiment_counter} experiments in total.")