# Generic SLO Evaluator

## Execution

For development:

```sh
uvicorn main:app --reload
```

## Test

Run unit tests via:

```sh
python -m unittest
```

## Build the Docker image:

```sh
docker build . -t theodolite-evaluator
```

Run the Docker image:

```sh
docker run -p 80:80 theodolite-evaluator
```

## Configuration

You can set the `HOST` and the `PORT` (and a lot of more parameters) via environment variables. Default is `0.0.0.0:80`.
For more information see the [Gunicorn/FastAPI Docker docs](https://github.com/tiangolo/uvicorn-gunicorn-fastapi-docker#advanced-usage).

Configure the log level via the `LOG_LEVEL` environment variable. Possible values are `DEBUG`, `INFO`, `WARNING`, `ERROR`, and `CRITICAL`. The default is `WARNING`.

## API Documentation

The running webserver provides a REST API with the following route:

* /
  * Method: POST
  * Body:
    * results
      * metric-metadata
      * values
    * metadata
      * warmup
      * queryAggregation
      * repetitionAggregation
      * operator
      * threshold

The body of the request must be a JSON string that satisfies the following conditions:

* **dropped records**: This property is based on the [Range Vector type](https://www.prometheus.io/docs/prometheus/latest/querying/api/#range-vectors) from Prometheus and must have the following JSON *structure*:

    ```json
    {
        "results": [
            [
                {
                    "metric": {
                        "<label-name>": "<label-value>"
                    },
                    "values": [
                        [
                            <unix_timestamp>, // 1.634624989695E9
                            "<sample_value>" // integer
                        ]
                    ]
                }
            ]
        ],
        "metadata": {
            "warmup": 60,
            "queryAggregation": "max",
            "repetitionAggregation": "median",
            "operator": "lt",
            "threshold": 2000000
        }
    }
    ```

### Description

* results:
  * metric-metadata:
    * Labels of this metric. The *Generic* SLO checker does not use labels in the calculation of the service level objective.
  * results
    * The `<unix_timestamp>` provided as the first element of each element in the `values` array must be the timestamp of the measurement value in seconds (with optional decimal precision).
    * The `<sample_value>` must be the measurement value as string.
* metadata: For the calculation of the service level objective require metadata.
  * **warmup**: Specifies the warmup time in seconds that are ignored for evaluating the SLO.
  * **queryAggregation**: Specifies the function used to aggregate a query. 
  * **repetitionAggregation**: Specifies the function used to aggregate a the results of multiple query aggregations.
  * **operator**: Specifies how the result should be checked against a threshold. Possible values are `lt`, `lte`, `gt` and `gte`. Additionally, the `true` and `false` are supported, which will cause an always successful or unsuccessful evaluation (can be helpful for testing and debugging).
  * **threshold**: Must be an unsigned integer that specifies the threshold for the SLO evaluation.
