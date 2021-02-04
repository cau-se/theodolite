import pprint

from strategies.config import ExperimentConfig
import strategies.strategies.search.binary_search_strategy as binary_search_strategy
import strategies.strategies.domain_restriction.lower_bound_strategy as lower_bound_strategy
from strategies.experiment_execution import ExperimentExecutor
import strategies.subexperiment_execution.subexperiment_executor as subexperiment_executor

class Object(object):
    pass

pp = pprint.PrettyPrinter(indent=4)

dim_values = [0, 1, 2, 3, 4, 5, 6]
replicass = [0, 1, 2, 3, 4, 5, 6]

# True means the experiment was successful
# the experiments are indexed row (representing dimension values) and column (representing number of replicas) wise as common known arrays from 0 - 6 respectively.
# this means the first row starts with (0,0), the second row with (1, 0) etc.
successful = [
       [ True , True , True , True , True , True , True  ],
       [ False, False, True , True , True , True , True  ],
       [ False, False, True , True , True , True , True  ],
       [ False, False, False, True , True , True , True  ],
       [ False, False, False, False, True , True , True  ],
       [ False, False, False, False, False, False, True  ],
       [ False, False, False, False, False, False, False ] 
    ]

expected_order = [
        (0,3), # workload dim 0
        (0,1), 
        (0,0),
        (1,3), # workload dim 1
        (1,1),
        (1,2),
        (2,4), # workload dim 2
        (2,2),
        (3,4), # workload dim 3
        (3,2),
        (3,3),
        (4,4), # workload dim 4
        (4,3),
        (5,5), # workload dim 5
        (5,6),
        (6,6) # workload dim 6
    ]

last_experiment = (0, 0)
experiment_counter = -1
subexperiment_executor = Object()

def subexperiment_executor_executor(config):
    global experiment_counter, last_experiment, pp
    print("Simulate subexperiment with config:")
    pp.pprint(config)
    last_experiment = (config.dim_value, config.replicas)
    experiment_counter += 1
    print("Simulation complete")

subexperiment_executor.execute = subexperiment_executor_executor


# returns True if the experiment was successful

subexperiment_evaluator = Object()

def subexperiment_evaluator_execute(i):
    print("Evaluating last experiment. Index was:")
    global expected_order, experiment_counter, last_experiment, successful
    pp.pprint(last_experiment)
    print("Index was expected to be:")
    pp.pprint(expected_order[experiment_counter])
    assert expected_order[experiment_counter] == last_experiment
    print("Index was as expected. Evaluation finished.")
    return 1 if successful[last_experiment[0]][last_experiment[1]] else 0

subexperiment_evaluator.execute = subexperiment_evaluator_execute

def test_binary_search_strategy():
    # declare parameters
    uc="test-uc"
    partitions=40
    cpu_limit="1000m"
    memory_limit="4Gi"
    kafka_streams_commit_interval_ms=100
    execution_minutes=5

    # execute
    experiment_config = ExperimentConfig(
        exp_id="0",
        use_case=uc,
        dim_values=dim_values,
        replicass=replicass,
        partitions=partitions,
        cpu_limit=cpu_limit,
        memory_limit=memory_limit,
        kafka_streams_commit_interval_ms=kafka_streams_commit_interval_ms,
        execution_minutes=execution_minutes,
        domain_restriction_strategy=lower_bound_strategy,
        search_strategy=binary_search_strategy,
        subexperiment_executor=subexperiment_executor,
        subexperiment_evaluator=subexperiment_evaluator)
    executor = ExperimentExecutor(experiment_config)
    executor.execute()