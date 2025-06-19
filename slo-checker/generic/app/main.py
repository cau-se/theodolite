from fastapi import FastAPI,Request
import logging
import os
import json
import sys
import re
import pandas as pd

pd.options.mode.copy_on_write = True # Until Pandas 3.0, https://pandas.pydata.org/pandas-docs/stable/user_guide/indexing.html#returning-a-view-versus-a-copy

app = FastAPI()

logging.basicConfig(stream=sys.stdout,
                    format="%(asctime)s %(levelname)s %(name)s: %(message)s")
logger = logging.getLogger("API")

if os.getenv('LOG_LEVEL') == 'DEBUG':
    logger.setLevel(logging.DEBUG)
elif os.getenv('LOG_LEVEL') == 'INFO':
    logger.setLevel(logging.INFO)
elif os.getenv('LOG_LEVEL') == 'ERROR':
    logger.setLevel(logging.ERROR)
elif os.getenv('LOG_LEVEL') == 'WARNING':
    logger.setLevel(logging.WARNING)
elif os.getenv('LOG_LEVEL') == 'CRITICAL':
    logger.setLevel(logging.CRITICAL)


def get_aggr_func(func_string: str):
    if func_string in ['mean', 'median', 'mode', 'sum', 'count', 'max', 'min', 'std', 'var', 'skew', 'kurt']:
        return func_string
    elif func_string == 'first':
        def first(x):
            return x.iloc[0]
        first.__name__ = 'first'
        return first
    elif func_string == 'last':
        def last(x):
            return x.iloc[-1]
        last.__name__ = 'last'
        return last
    elif re.search(r'^p\d\d?(\.\d+)?$', func_string): # matches strings like 'p99', 'p99.99', 'p1', 'p0.001'
        def percentile(x):
            return x.quantile(float(func_string[1:]) / 100)
        percentile.__name__ = func_string
        return percentile
    else:
        raise ValueError('Invalid function string.')

def aggr_query(values: dict, warmup: int, aggr_func):
    df = pd.DataFrame.from_dict(values)
    df.columns = ['timestamp', 'value']
    filtered = df[df['timestamp'] >= (df['timestamp'][0] + warmup)]
    filtered['value'] = filtered['value'].astype(float)
    return filtered['value'].aggregate(aggr_func)

def check_result(result, operator: str, threshold):
    if operator == 'lt':
        return result < threshold
    if operator == 'lte':
        return result <= threshold
    if operator == 'gt':
        return result > threshold
    if operator == 'gte':
        return result >= threshold
    if operator == 'true':
        return True # Mainly used for testing
    if operator == 'false':
        return False # Mainly used for testing
    else:
        raise ValueError('Invalid operator string.')



@app.post("/", response_model=bool)
async def check_slo(request: Request):
    data = json.loads(await request.body())
    logger.info('Received request with metadata: %s', data['metadata'])

    warmup = int(data['metadata']['warmup'])
    query_aggregation = get_aggr_func(data['metadata']['queryAggregation'])
    rep_aggregation = get_aggr_func(data['metadata']['repetitionAggregation'])
    operator = data['metadata']['operator']
    threshold = float(data['metadata']['threshold'])

    query_results = [aggr_query(r[0]["values"], warmup, query_aggregation) for r in data["results"]]
    result = pd.DataFrame(query_results).aggregate(rep_aggregation).at[0]
    return check_result(result, operator, threshold)

logger.info("SLO evaluator is online")