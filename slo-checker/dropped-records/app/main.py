from fastapi import FastAPI,Request
import logging
import os
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


def check_service_level_objective(results, threshold):
    return max(results) < threshold

@app.post("/dropped-records",response_model=bool)
async def evaluate_slope(request: Request):
    data = json.loads(await request.body())
    warmup = int(data['results'][0][0]['values'][0][0]) + int(data['metadata']['warmup'])
    results = [int(val[1]) if(int(val[0]>=warmup)) else 0 for result in data['results'] for r in result for val in r['values']  ]
    return check_service_level_objective(results=results, threshold=data['metadata']["threshold"])

logger.info("SLO evaluator is online")