import os
import sys
import os
import lib.trend_slope_computer as trend_slope_computer

THRESHOLD = 2000
WARMUP_SEC = 60 

def execute(config):
    cwd = os.getcwd()
    file = f"exp{config.counter}_uc{config.use_case}_{config.dim_value}_{config.replicas}_totallag.csv"

    trend_slope = trend_slope_computer.compute(cwd, file, WARMUP_SEC, THRESHOLD)

    print(f"Trend Slope: {trend_slope}")
    success = 0 if trend_slope > THRESHOLD else 1
    return success
