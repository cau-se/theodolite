# The lower bound strategy
def execute(config):
    dim_value_index = 0
    lower_bound_replicas_index = 0
    subexperiment_counter = 0
    while dim_value_index < len(config.dim_values) and lower_bound_replicas_index >= 0 and lower_bound_replicas_index < len(config.replicass):
        lower_bound_replicas_index, subexperiment_counter = config.search_strategy.execute(
            config=config,
            dim_value_index=dim_value_index,
            lower_replicas_bound_index=lower_bound_replicas_index,
            subexperiment_counter=subexperiment_counter)
        dim_value_index+=1