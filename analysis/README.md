# Theodolite Analysis

This directory contains Jupyter notebooks for analyzing and visualizing
benchmark execution results and plotting. The following notebooks are provided:

* [demand-metric.ipynb](demand-metric.ipynb): Create CSV files describing scalability according to the Theodolite `demand` metric.
* [demand-metric-plot.ipynb](demand-metric-plot.ipynb): Create plots based on such CSV files of the `demand` metric.

For legacy reasons, we also provide the following notebooks, which, however, are not documented:

* [scalability-graph.ipynb](scalability-graph.ipynb): Creates a scalability graph for a certain benchmark execution.
* [scalability-graph-final.ipynb](scalability-graph-final.ipynb): Combines the scalability graphs of multiple benchmarks executions (e.g. for comparing different configuration).
* [lag-trend-graph.ipynb](lag-trend-graph.ipynb): Visualizes the consumer lag evaluation over time along with the computed trend.

## Usage

In general, the Theodolite Analysis Jupyter notebooks should be runnable by any Jupyter server. To make it a bit easier,
we provide introductions for running notebooks with Docker and with Visual Studio Code. These intoduction may also be
a good starting point for using another service.

For analyzing and visualizing benchmark results, either Docker or a Jupyter installation with Python 3.7 or newer is
required (e.g., in a virtual environment).

### Running with Docker

This option requires Docker to be installed. You can build and run a container using the following commands. Make sure
to set the `results` volume to the directory with your execution results and `results-inst` to a directory where the
final scalability graphs should be placed. The output of the *run* command gives you an URL of the form
`http://127.0.0.1:8888/?token=...`, which you should open in your webbrowser. From there you can access all notebooks.
You can stop the Jupyter server with Crtl + C.

```sh
docker build . -t theodolite-analysis
docker run --rm -p 8888:8888 -v "$PWD/../results":/home/jovyan/results -v "$PWD/../results-inst":/home/jovyan/results-inst theodolite-analysis
```

### Running with Visual Studio Code

The [Visual Studio Code Documentation](https://code.visualstudio.com/docs/python/jupyter-support) shows to run Jupyter
notebooks with Visual Studio Code. For our notebooks, Python 3.7 or newer is required (e.g., in a virtual environment).
Moreover, they require some Python libraries, which can be installed by:

```sh
pip install -r requirements.txt
```