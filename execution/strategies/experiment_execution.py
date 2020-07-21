class ExperimentExecutor:
    def __init__(self, config):
        self.config=config
    
    def execute(self):
        self.config.benchmarking_strategy.execute(self.config)
