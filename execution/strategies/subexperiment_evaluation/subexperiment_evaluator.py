import lib.trend_slope_computer as trend_slope_computer
import logging
import os

WARMUP_SEC = 60

def execute(config, threshold):
    """
    Check the trend slope of the totallag of the subexperiment if it comes below
    the threshold.

    :param config: Configuration of the subexperiment.
    :param threshold: The threshold the trendslope need to come below.
    """
    cwd = f'{os.getcwd()}/{config.result_path}'
    file = f"exp{config.exp_id}_uc{config.use_case}_{config.dim_value}_{config.replicas}_totallag.csv"

    try:
        trend_slope = trend_slope_computer.compute(cwd, file, WARMUP_SEC)
    except Exception as e:
        err_msg = 'Computing trend slope failed'
        print(err_msg)
        logging.exception(err_msg)
        print('Mark this subexperiment as not successful and continue benchmark')
        return False

    print(f"Trend Slope: {trend_slope}")

    return trend_slope < threshold
