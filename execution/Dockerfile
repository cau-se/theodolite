FROM python:3.8

RUN mkdir /app
WORKDIR /app
ADD requirements.txt /app/
RUN pip install -r requirements.txt
COPY uc-workload-generator /app/uc-workload-generator
COPY uc-application /app/uc-application
COPY strategies /app/strategies
COPY lib /app/lib
COPY lag_analysis.py /app/
COPY run_uc.py /app/
COPY theodolite.py /app/

CMD ["python", "/app/theodolite.py"]
