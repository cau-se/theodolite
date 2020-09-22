# The check_all strategy
import os
from strategies.strategies.config import SubexperimentConfig

def execute(config, dim_value_index, lower_replicas_bound_index, subexperiment_counter):
    new_lower_replicas_bound_index=lower_replicas_bound_index
    new_lower_replicas_bound_found=False
    subexperiments_total=len(config.dim_values)*len(config.replicass)
    while lower_replicas_bound_index < len(config.replicass):
        subexperiment_counter+=1
        dim_value=config.dim_values[dim_value_index]
        replicas=config.replicass[lower_replicas_bound_index]
        print(f"Run subexperiment {subexperiment_counter} of {subexperiments_total} with dimension value {dim_value} and {replicas} replicas.")

        subexperiment_config = SubexperimentConfig(config.use_case, subexperiment_counter, dim_value, replicas, config.partitions, config.cpu_limit, config.memory_limit, config.kafka_streams_commit_interval_ms, config.execution_minutes)

        config.subexperiment_executor.execute(subexperiment_config)

        result = config.subexperiment_evaluator.execute(subexperiment_config) == 1
        if result == 1 and not new_lower_replicas_bound_found:
            new_lower_replicas_bound_found = True
            new_lower_replicas_bound_index = lower_replicas_bound_index
        lower_replicas_bound_index+=1
    return (new_lower_replicas_bound_index, subexperiment_counter)
