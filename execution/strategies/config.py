from dataclasses import dataclass

@dataclass
class ExperimentConfig:
    """ Wrapper for the configuration of an experiment. """
    use_case: str
    dim_values: list
    replicas: list
    partitions: int
    cpu_limit: str
    memory_limit: str
    kafka_streams_commit_interval_ms: int
    execution_minutes: int
    benchmarking_strategy: object
    subexperiment_executor: object