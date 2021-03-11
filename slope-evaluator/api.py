from fastapi import FastAPI,Request
import trend_slope_computer as trend_slope_computer
import logging
import os
import pandas as pd
import json
import numpy as np
from fastapi.encoders import jsonable_encoder

app = FastAPI()

def execute(results, threshold):

    d = []
    for result in results:
        #print(results)
        group = result['metric']['group']
        for value in result['values']:
            # print(value)
            d.append({'group': group, 'timestamp': int(
                value[0]), 'value': int(value[1]) if value[1] != 'NaN' else 0})

    df = pd.DataFrame(d)

    print(df)
    try:
        trend_slope = trend_slope_computer.compute(df, 0)
    except Exception as e:
        err_msg = 'Computing trend slope failed'
        print(err_msg)
        logging.exception(err_msg)
        print('Mark this subexperiment as not successful and continue benchmark')
        return False

    print(f"Trend Slope: {trend_slope}")

    return trend_slope < threshold

@app.post("/evaluate-slope",response_model=bool)
async def evaluate_slope(request: Request):
    print("request received")
    x = json.loads(await request.body())
    #x = np.array(x['total_lag'])
    y = execute(x['total_lag'], 1000)
    print(print(y))
    return y
