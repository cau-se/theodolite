# Wrapper that makes the execution method of a subexperiment interchangable.

import os

dirname = os.path.dirname(__file__)
os.chdir(dirname+"/../../")
def execute(subexperiment_config):
    os.system("./run_uc{}.sh {} {} {} {} {} {} {} {}".format(subexperiment_config.use_case, subexperiment_config.counter, subexperiment_config.dim_value, subexperiment_config.replicas, subexperiment_config.partitions, subexperiment_config.cpu_limit, subexperiment_config.memory_limit, subexperiment_config.kafka_streams_commit_interval_ms, subexperiment_config.execution_minutes))