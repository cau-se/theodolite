#!/bin/bash

UC=$1
IFS=', ' read -r -a DIM_VALUES <<< "$2"
IFS=', ' read -r -a REPLICAS <<< "$3"
PARTITIONS=$4

# Get and increment counter
EXP_ID=$(cat exp_counter.txt)
echo $((EXP_ID+1)) > exp_counter.txt

# Store meta information
IFS=$', '; echo \
"UC=$UC
DIM_VALUES=${DIM_VALUES[*]}
REPLICAS=${REPLICAS[*]}
PARTITIONS=$PARTITIONS
" >> "exp${EXP_ID}_uc${UC}_meta.txt"

for DIM_VALUE in "${DIM_VALUES[@]}"
do
    for REPLICA in "${REPLICAS[@]}"
    do
        echo "Run $DIM_VALUE $REPLICA"
        ./run_uc$UC-new.sh $EXP_ID $DIM_VALUE $REPLICA $PARTITIONS
        sleep 10s
    done
done
