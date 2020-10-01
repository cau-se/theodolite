class ExperimentExecutor:
    def __init__(self, config):
        self.config=config
    
    def execute(self):
        self.config.domain_restriction_strategy.execute(self.config)
