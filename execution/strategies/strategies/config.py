class SubexperimentConfig:
    """ Wrapper for the configuration of a subexperiment """
    def __init__(self, use_case, counter, dim_value, replicas, partitions, cpu_limit, memory_limit, kafka_streams_commit_interval_ms, execution_minutes, subexperiment_executor):
        self.use_case=use_case
        self.counter=counter
        self.dim_value=dim_value
        self.replicas=replicas
        self.partitions=partitions
        self.cpu_limit=cpu_limit
        self.memory_limit=memory_limit
        self.kafka_streams_commit_interval_ms=kafka_streams_commit_interval_ms
        self.execution_minutes=execution_minutes
        self.subexperiment_executor=subexperiment_executor