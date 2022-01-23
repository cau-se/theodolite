from fastapi import FastAPI,Request
from .trend_slope_computer import compute
import logging
import os
import pandas as pd
import json
import sys
from statistics import median

app = FastAPI()

logging.basicConfig(stream=sys.stdout,
                    format="%(asctime)s %(levelname)s %(name)s: %(message)s")
logger = logging.getLogger("API")


if os.getenv('LOG_LEVEL') == 'INFO':
    logger.setLevel(logging.INFO)
elif os.getenv('LOG_LEVEL') == 'WARNING':
    logger.setLevel(logging.WARNING)
elif os.getenv('LOG_LEVEL') == 'DEBUG':
    logger.setLevel(logging.DEBUG)

def calculate_slope_trend(results, warmup):
    d = []
    for result in results:
        group = result['metric']['group']
        for value in result['values']:
            d.append({'group': group, 'timestamp': int(
                value[0]), 'value': int(value[1]) if value[1] != 'NaN' else 0})

    df = pd.DataFrame(d)

    logger.info("Calculating trend slope with warmup of %s seconds for data frame:\n %s", warmup, df)
    try:
        trend_slope = compute(df, warmup)
    except Exception as e:
        err_msg = 'Computing trend slope failed.'
        logger.exception(err_msg)
        logger.error('Mark this subexperiment as not successful and continue benchmark.')
        return float('inf')

    logger.info("Computed lag trend slope is '%s'", trend_slope)
    return trend_slope

def check_service_level_objective(results, threshold):
    return median(results) < threshold

@app.post("/evaluate-slope",response_model=bool)
async def evaluate_slope(request: Request):
    data = json.loads(await request.body())
    results = [calculate_slope_trend(total_lag, data['metadata']['warmup']) for total_lag in data['results']]
    return check_service_level_objective(results=results, threshold=data['metadata']["threshold"])

logger.info("SLO evaluator is online")