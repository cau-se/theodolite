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
    execution_minutes: int
    prometheus_base_url: str
    reset: bool
    namespace: str
    result_path: str
    configurations: dict
