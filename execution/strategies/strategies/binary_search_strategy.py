import os
from .config import SubexperimentConfig

def searchTransition(config, replica_index, lower, upper, subexperiment_counter):
    if lower==upper:
        print(f"Run subexperiment {subexperiment_counter} with config {config.dim_values[lower]} {config.replicas[replica_index]}")
        subexperiment_config = SubexperimentConfig(config.use_case, subexperiment_counter, config.dim_values[lower], config.replicas[replica_index], config.partitions, config.cpu_limit, config.memory_limit, config.kafka_streams_commit_interval_ms, config.execution_minutes)
        config.subexperiment_executor.execute(subexperiment_config)
        result = config.subexperiment_evaluator.execute()
        if result==1: # successful, the upper neighbor must be not successful
            return lower+1
        else: # not successful
            return lower
    elif lower+1 == upper:
        print(f"Run subexperiment {subexperiment_counter} with config {config.dim_values[lower]} {config.replicas[replica_index]}")
        subexperiment_config = SubexperimentConfig(config.use_case, subexperiment_counter, config.dim_values[lower], config.replicas[replica_index], config.partitions, config.cpu_limit, config.memory_limit, config.kafka_streams_commit_interval_ms, config.execution_minutes)
        config.subexperiment_executor.execute(subexperiment_config)
        result = config.subexperiment_evaluator.execute()
        if result==1: # successful, the upper neighbor must be not successful
            print(f"Run subexperiment {subexperiment_counter} with config {config.dim_values[upper]} {config.replicas[replica_index]}")
            subexperiment_config = SubexperimentConfig(config.use_case, subexperiment_counter, config.dim_values[upper], config.replicas[replica_index], config.partitions, config.cpu_limit, config.memory_limit, config.kafka_streams_commit_interval_ms, config.execution_minutes)
            config.subexperiment_executor.execute(subexperiment_config)
            result = config.subexperiment_evaluator.execute()
            if result == 1:
                return upper+1
            else:
                return upper
        else: # not successful
            return lower
    else:
        # test mid
        mid=(upper+lower)//2
        print(f"Run subexperiment {subexperiment_counter} with config {config.dim_values[mid]} {config.replicas[replica_index]}")
        subexperiment_config = SubexperimentConfig(config.use_case, subexperiment_counter, config.dim_values[mid], config.replicas[replica_index], config.partitions, config.cpu_limit, config.memory_limit, config.kafka_streams_commit_interval_ms, config.execution_minutes)
        config.subexperiment_executor.execute(subexperiment_config)
        result = config.subexperiment_evaluator.execute()
        if result == 1: # success -> search in (mid+1, upper)
            return searchTransition(config, replica_index, mid+1, upper, subexperiment_counter+1)
        else: # not success -> search in (lower, mid-1)
            return searchTransition(config, replica_index, lower, mid-1, subexperiment_counter+1)


def execute(config):
    subexperiment_counter=0
    lower = 0
    upper = len(config.dim_values)-1
    j = 0
    while j < len(config.replicas) and lower < len(config.dim_values):
        lower = searchTransition(config, j, lower, upper, subexperiment_counter+1)
        j+=1

