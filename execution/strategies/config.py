class ExperimentConfig:
    """ Wrapper for the configuration of an experiment. """
    def __init__(self, use_case, dim_values, replicas, partitions, cpu_limit, memory_limit, kafka_streams_commit_interval_ms, execution_minutes, benchmarking_strategy, subexperiment_executor):
        self.use_case=use_case
        self.dim_values=dim_values
        self.replicas=replicas
        self.partitions=partitions
        self.cpu_limit=cpu_limit
        self.memory_limit=memory_limit
        self.kafka_streams_commit_interval_ms=kafka_streams_commit_interval_ms
        self.execution_minutes=execution_minutes
        self.benchmarking_strategy=benchmarking_strategy
        self.subexperiment_executor=subexperiment_executor