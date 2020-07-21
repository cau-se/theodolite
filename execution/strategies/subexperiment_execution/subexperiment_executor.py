# Wrapper that makes the execution method of a subexperiment interchangable.

import os

dirname = os.path.dirname(__file__)
os.chdir(dirname+"/../../")
def execute(subexperiment_config):
    os.system("./run_uc"+subexperiment_config.use_case+"-new.sh "+str(subexperiment_config.counter)+" "+str(subexperiment_config.dim_value)+" "+str(subexperiment_config.replicas)+" "+str(subexperiment_config.partitions)+" "+subexperiment_config.cpu_limit+" "+subexperiment_config.memory_limit+" "+str(subexperiment_config.kafka_streams_commit_interval_ms)+" "+str(subexperiment_config.execution_minutes))
