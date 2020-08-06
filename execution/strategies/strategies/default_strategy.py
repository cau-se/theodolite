# Contains the default strategy that executes a subexperiment for all combinations of instances and dimension values.

import os
from .config import SubexperimentConfig

def execute(config):
    subexperiment_counter=0
    subexperiments_total=len(config.dim_values)*len(config.replicas)
    for dim_value in config.dim_values:
        for replica in config.replicas:
            subexperiment_counter+=1
            print("Run subexperiment {}/{} with dimension value {} and {} replicas.".format(subexperiment_counter, subexperiments_total, dim_value, replica))

            subexperiment_config = SubexperimentConfig(config.use_case, subexperiment_counter, dim_value, replica, config.partitions, config.cpu_limit, config.memory_limit, config.kafka_streams_commit_interval_ms, config.execution_minutes, config.subexperiment_executor)

            config.subexperiment_executor.execute(subexperiment_config)
