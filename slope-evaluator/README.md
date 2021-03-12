# Slope Evaluator

## Execution

For development:

```sh
uvicorn main:app --reload
```

Build the docker image:

```sh
docker build . -t theodolite-evaluator
```

Run the Docker image:

```sh
 docker run -p 80:80 theodolite-evaluator
```

## Configuration

You can set the `HOST` and the `PORT`  (and a lot of more parameters) via environment variablen. Default is `0.0.0.0:80`.
For more information see [here](https://github.com/tiangolo/uvicorn-gunicorn-fastapi-docker#advanced-usage).
