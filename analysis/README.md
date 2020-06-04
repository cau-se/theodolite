# Theodolite Analysis

This directory contains Jupyter notebooks for analyzing and visualizing
benchmark execution results and plotting. The following notebooks are provided:

* [scalability-graph.ipynb](scalability-graph.ipynb): Creates a scalability graph for a certain benchmark execution.
* [scalability-graph-final.ipynb](scalability-graph-final.ipynb): Combines the scalability graphs of multiple benchmarks executions (e.g. for comparing different configuration).
* [lag-trend-graph.ipynb](lag-trend-graph.ipynb): Visualizes the consumer lag evaluation over time along with the computed trend.

## Usage

For executing benchmarks and analyzing their results, a **Python 3.7**
installation is required (e.g., in a virtual environment). Our notebooks require some
Python libraries, which can be installed via:

```sh
pip install -r requirements.txt 
```

We have tested these
notebooks with [Visual Studio Code](https://code.visualstudio.com/docs/python/jupyter-support),
however, every other server should be fine as well.
