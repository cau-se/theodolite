# The binary search strategy
import os
from strategies.strategies.config import SubexperimentConfig

def binary_search(config, dim_value, lower, upper, subexperiment_counter):
    if lower == upper:
        print(f"Run subexperiment {subexperiment_counter} with config {dim_value} {config.replicass[lower]}")
        subexperiment_config = SubexperimentConfig(config.use_case, config.exp_id, subexperiment_counter, dim_value, config.replicass[lower], config.partitions, config.cpu_limit, config.memory_limit, config.execution_minutes, config.prometheus_base_url, config.reset, config.namespace, config.result_path, config.configurations)
        config.subexperiment_executor.execute(subexperiment_config)
        success = config.subexperiment_evaluator.execute(subexperiment_config,
                                                         config.threshold)
        if success: # successful, the upper neighbor is assumed to also has been successful
            return (lower, subexperiment_counter+1)
        else: # not successful
            return (lower+1, subexperiment_counter)
    elif lower+1==upper:
        print(f"Run subexperiment {subexperiment_counter} with config {dim_value} {config.replicass[lower]}")
        subexperiment_config = SubexperimentConfig(config.use_case, config.exp_id, subexperiment_counter, dim_value, config.replicass[lower], config.partitions, config.cpu_limit, config.memory_limit, config.execution_minutes, config.prometheus_base_url, config.reset, config.namespace, config.result_path, config.configurations)
        config.subexperiment_executor.execute(subexperiment_config)
        success = config.subexperiment_evaluator.execute(subexperiment_config,
                                                         config.threshold)
        if success: # minimal instances found
            return (lower, subexperiment_counter)
        else: # not successful, check if lower+1 instances are sufficient
            print(f"Run subexperiment {subexperiment_counter} with config {dim_value} {config.replicass[upper]}")
            subexperiment_config = SubexperimentConfig(config.use_case, config.exp_id, subexperiment_counter, dim_value, config.replicass[upper], config.partitions, config.cpu_limit, config.memory_limit, config.execution_minutes, config.prometheus_base_url, config.reset, config.namespace, config.result_path, config.configurations)
            config.subexperiment_executor.execute(subexperiment_config)
            success = config.subexperiment_evaluator.execute(subexperiment_config,
                                                             config.threshold)
            if success: # minimal instances found
                return (upper, subexperiment_counter)
            else:
                return (upper+1, subexperiment_counter)
    else:
        # test mid
        mid=(upper+lower)//2
        print(f"Run subexperiment {subexperiment_counter} with config {dim_value} {config.replicass[mid]}")
        subexperiment_config = SubexperimentConfig(config.use_case, config.exp_id, subexperiment_counter, dim_value, config.replicass[mid], config.partitions, config.cpu_limit, config.memory_limit, config.execution_minutes, config.prometheus_base_url, config.reset, config.namespace, config.result_path, config.configurations)
        config.subexperiment_executor.execute(subexperiment_config)
        success = config.subexperiment_evaluator.execute(subexperiment_config,
                                                         config.threshold)
        if success: # success -> search in (lower, mid-1)
            return binary_search(config, dim_value, lower, mid-1, subexperiment_counter+1)
        else: # not success -> search in (mid+1, upper)
            return binary_search(config, dim_value, mid+1, upper, subexperiment_counter+1)

def execute(config, dim_value_index, lower_replicas_bound_index, subexperiment_counter):
    upper = len(config.replicass)-1
    dim_value=config.dim_values[dim_value_index]
    return binary_search(config, dim_value, lower_replicas_bound_index, upper, subexperiment_counter)
