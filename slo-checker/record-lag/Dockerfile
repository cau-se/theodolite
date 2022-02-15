FROM python:3.8

WORKDIR /code

COPY ./requirements.txt /code/requirements.txt
RUN pip install --no-cache-dir --upgrade -r /code/requirements.txt

COPY ./app /code/app

WORKDIR /code/app

ENV HOST 0.0.0.0
ENV PORT 80

CMD ["sh", "-c", "uvicorn main:app --host $HOST --port $PORT"]
