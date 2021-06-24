# Lag Trend SLO Evaluator

## Execution

For development:

```sh
uvicorn main:app --reload # run this command inside the app/ folder
```

## Build the docker image:

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

## API Documentation

The running webserver provides a REST API with the following route:

* /evaluate-slope
    * Method: POST
    * Body:
        * total_lags
        * threshold
        * warmup

The body of the request must be a JSON string that satisfies the following conditions:

* **total_lag**: This property is based on the [Range Vector type](https://www.prometheus.io/docs/prometheus/latest/querying/api/#range-vectors) from Prometheus and must have the following JSON structure:
    ```
        { 
            [
                "metric": {
                    "group": "<label_value>"
                },
                "values": [
                    [
                        <unix_timestamp>,
                        "<sample_value>"
                    ]
                ]
            ]
        }
    ```
    * The `<label_value>` provided in "metric.group" must be equal to the id of the Kafka consumer group.
    * The `<unix_timestamp>` provided as the first element of each element in the "values" array must be the timestamp of the measurement value in seconds (with optional decimal precision)
    * The `<sample_value>` must be the measurement value as string.
* **threshold**: Must be an unsigned integer that specifies the threshold for the SLO evaluation. The SLO is considered fulfilled, if the result value is below the threshold. If the result value is equal or above the threshold, the SLO is considered not fulfilled.
* **warmup**: Specifieds the warmup time in seconds that are ignored for evaluating the SLO.