FROM jupyter/base-notebook:python-3.10

COPY . /home/jovyan

WORKDIR /home/jovyan
RUN rm -r work
RUN pip install -r requirements.txt
