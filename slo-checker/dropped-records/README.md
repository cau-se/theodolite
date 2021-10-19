# Lag Trend SLO Evaluator

## Execution

For development:

```sh
uvicorn main:app --reload  --port 81 # run this command inside the app/ folder
```

## Build the docker image:

```sh
docker build . -t theodolite-evaluator
```

Run the Docker image:

```sh
docker run -p 80:81 theodolite-evaluator
```

## Configuration

You can set the `HOST` and the `PORT` (and a lot of more parameters) via environment variables. Default is `0.0.0.0:80`.
For more information see the [Gunicorn/FastAPI Docker docs](https://github.com/tiangolo/uvicorn-gunicorn-fastapi-docker#advanced-usage).

## API Documentation

The running webserver provides a REST API with the following route:

* /dropped-records
  * Method: POST
  * Body:
    * results
      * metric-metadata
      * values
    * metadata
      * threshold
      * warmup

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
            "threshold": 2000000,
            "warmup": 60
        }
    }
    ```

### description

* results:
  * metric-metadata:
    * Labels of this metric. The `dropped-records` slo checker does not use labels in the calculation of the service level objective.
  * results
    * The `<unix_timestamp>` provided as the first element of each element in the "values" array must be the timestamp of the measurement value in seconds (with optional decimal precision)
    * The `<sample_value>` must be the measurement value as string.
* metadata: For the calculation of the service level objective require metadata.
  * **threshold**: Must be an unsigned integer that specifies the threshold for the SLO evaluation. The SLO is considered fulfilled, if the result value is below the threshold. If the result value is equal or above the threshold, the SLO is considered not fulfilled.
  * **warmup**: Specifieds the warmup time in seconds that are ignored for evaluating the SLO.
