#!/bin/bash

UC=$1
IFS=', ' read -r -a DIM_VALUES <<< "$2"
IFS=', ' read -r -a REPLICAS <<< "$3"
PARTITIONS=${4:-40}
CPU_LIMIT=${5:-1000m}
MEMORY_LIMIT=${6:-4Gi}
KAFKA_STREAMS_COMMIT_INTERVAL_MS=${7:-100}
EXECUTION_MINUTES=${8:-5}

# Get and increment counter
EXP_ID=$(cat exp_counter.txt 2>/dev/null || echo "0")
echo $((EXP_ID+1)) > exp_counter.txt

# Store meta information
IFS=$', '; echo \
"UC=$UC
DIM_VALUES=${DIM_VALUES[*]}
REPLICAS=${REPLICAS[*]}
PARTITIONS=$PARTITIONS
CPU_LIMIT=$CPU_LIMIT
MEMORY_LIMIT=$MEMORY_LIMIT
KAFKA_STREAMS_COMMIT_INTERVAL_MS=$KAFKA_STREAMS_COMMIT_INTERVAL_MS
EXECUTION_MINUTES=$EXECUTION_MINUTES
" >> "exp${EXP_ID}_uc${UC}_meta.txt"

SUBEXPERIMENTS=$((${#DIM_VALUES[@]} * ${#REPLICAS[@]}))
SUBEXPERIMENT_COUNTER=0

echo "Going to execute $SUBEXPERIMENTS subexperiments in total..."
for DIM_VALUE in "${DIM_VALUES[@]}"
do
    for REPLICA in "${REPLICAS[@]}"
    do
        SUBEXPERIMENT_COUNTER=$((SUBEXPERIMENT_COUNTER+1))
        echo "Run subexperiment $SUBEXPERIMENT_COUNTER/$SUBEXPERIMENTS with config: $DIM_VALUE $REPLICA"
        ./run_uc$UC.sh $EXP_ID $DIM_VALUE $REPLICA $PARTITIONS $CPU_LIMIT $MEMORY_LIMIT $KAFKA_STREAMS_COMMIT_INTERVAL_MS $EXECUTION_MINUTES
        sleep 10s
    done
done
