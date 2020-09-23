from dataclasses import dataclass

@dataclass
class SubexperimentConfig:
    """ Wrapper for the configuration of a subexperiment """
    use_case: str
    exp_id: int
    counter: int
    dim_value: int
    replicas: int
    partitions: int
    cpu_limit: str
    memory_limit: str
    kafka_streams_commit_interval_ms: int
    execution_minutes: int