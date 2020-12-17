import lib.trend_slope_computer as trend_slope_computer
import logging
import os
import sys

THRESHOLD = 2000
WARMUP_SEC = 60

def execute(config):
    cwd = f'{os.getcwd()}/{config.result_path}'
    file = f"exp{config.exp_id}_uc{config.use_case}_{config.dim_value}_{config.replicas}_totallag.csv"

    try:
        trend_slope = trend_slope_computer.compute(cwd, file, WARMUP_SEC, THRESHOLD)
    except Exception as e:
        err_msg = 'Computing trend slope failed'
        print(err_msg)
        logging.exception(err_msg)
        print('Mark this subexperiment as not successful and continue benchmark')
        return 0


    print(f"Trend Slope: {trend_slope}")
    success = 0 if trend_slope > THRESHOLD else 1
    return success
