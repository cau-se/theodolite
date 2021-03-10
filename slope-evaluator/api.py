from fastapi import FastAPI
import trend_slope_computer as trend_slope_computer
import logging
import os
import pandas as pd

app = FastAPI()


@app.get("/evaluate-slope")
def evaluate_slope(total_lag):
    print("request received")
    print(total_lag)
    execute(total_lag, 1000)
    return {"suitable" : "false"}



def execute(total_lag, threshold):
    df = pd.DataFrame(total_lag)
    try:
        trend_slope = trend_slope_computer.compute(df, 60)
    except Exception as e:
        err_msg = 'Computing trend slope failed'
        print(err_msg)
        logging.exception(err_msg)
        print('Mark this subexperiment as not successful and continue benchmark')
        return False

    print(f"Trend Slope: {trend_slope}")

    return trend_slope < threshold
