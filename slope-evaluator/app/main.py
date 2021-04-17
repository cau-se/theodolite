from fastapi import FastAPI,Request
import trend_slope_computer as trend_slope_computer
import logging
import os
import pandas as pd
import json
import sys

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

def execute(results, threshold, warmup):
    d = []
    for result in results:
        group = result['metric']['group']
        for value in result['values']:
            d.append({'group': group, 'timestamp': int(
                value[0]), 'value': int(value[1]) if value[1] != 'NaN' else 0})

    df = pd.DataFrame(d)

    logger.info("Calculating trend slope for data frame:\n %s", df)
    try:
        trend_slope = trend_slope_computer.compute(df, warmup)
    except Exception as e:
        err_msg = 'Computing trend slope failed.'
        logger.exception(err_msg)
        logger.error('Mark this subexperiment as not successful and continue benchmark.')
        return False

    result = trend_slope < threshold
    logger.info("Computed lag trend slope is '%s'. Result is: %s", trend_slope, result)
    return result

@app.post("/evaluate-slope",response_model=bool)
async def evaluate_slope(request: Request):
    data = json.loads(await request.body())
    return execute(data['total_lag'], data['threshold'], data['warmup'])

logger.info("Slope evaluator is online")