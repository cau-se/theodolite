# Contains the default strategy that executes a subexperiment for all combinations of instances and dimension values.

import os
from .config import SubexperimentConfig


def execute(config):
    subexperiment_counter=0
    subexperiments_total=len(config.dim_values)*len(config.replicas)
    i=0
    j=0
    while i in range(len(config.dim_values)):
        while j in range(len(config.replicas)):
            subexperiment_counter+=1
            print(f"Run subexperiment {subexperiment_counter}/{subexperiments_total} with dimension value {config.dim_values[i]} and {config.replicas[j]} replicas.")

            subexperiment_config = SubexperimentConfig(config.use_case, subexperiment_counter, config.dim_values[i], config.replicas[j], config.partitions, config.cpu_limit, config.memory_limit, config.kafka_streams_commit_interval_ms, config.execution_minutes)

            config.subexperiment_executor.execute(subexperiment_config)
            result = config.subexperiment_evaluator.execute()
            if result:
                i+=1
            else:
                j+=1
        i+=1