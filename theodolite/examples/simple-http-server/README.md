# Sample HTTP Webserver

This directory provides a sample setup for an HTTP webserver with enabled monitoring and the like as well as an associated load generator.

## Webserver

### Deployment

The webserver deployment `nginx-deployment.yaml` consists of pods with 4 containers:
* The "real" work is done inside the `httpbin` container. It provides an HTTP GET endpoint `/delay/1` which replies to requests after a delay of 1 second. The returned HTTP status code is 200. We're not providing too much computing resource to this container, so we're actually able to push it to its limits.
* The `nginx` container serves as a reverse proxy, which forwards every GET request at `/` to `/delay/1`. Only this reverse proxy  (not the real webserver (`httpbin`)) is accessible from outside these pods. If forwards to `httpbin` exceed a timeout of 2 seconds, a 502 HTTP status code is returned. Otherwise, the response of `httpbin`. This container should have enough resources to not become the bottleneck of our setup.
* The `nginx-log-exporter` container is responsible for collecting and providing metrics about the `nginx` container.
* The `nginx-status-exporter` collects additional metrics. You probably don't need it if you use the `nginx-log-exporter`.

### ConfigMaps

Configuration of the `nginx` and `nginx-log-exporter` container is done via ConfigMaps.

### Service

The `nginx-service` provides access to the "real" endpoint at port 80 and to the metrics at port 9113 and 4040.

### ServiceMonitor

The `nginx-service-monitor` is responsible for providing all our metrics to Prometheus.

## Load Generator

The load generator is configurable by an environment variable `SLEEP_SECONDS`. It requests the configured service every `SLEEP_SECONDS` second.

**It is important to note that the requests are made in the background.** Otherwise, we wouldn't make any new requests before the previous ones have completed.

## Monitoring the Setup

Once Prometheus has picked up the ServiceMonitor, you can request the corresponding metrics of the webserver deployment. A good metric to get started with is:

```
nginx_http_response_count_total
```

It provides the total amount of requests to nginx. You can watch this metrics via the Prometheus UI by entering this metric as *query*, hitting *execute* and selecting the *Graph* view.

In Prometheus terms, this metric is a *counter*, meaning that this value never decreases. Probably you are more interested in getting something *relative*. For this purpose, you can use the *rate* function:

```
rate(nginx_http_response_count_total[1m])
```

To get the amount of successful requests among all your instances, you can query something like:

```
sum(rate(nginx_http_response_count_total{status="200"}[1m]))
```

Similarly, to get the number of failed requests:

```
sum(rate(nginx_http_response_count_total{status="502"}[1m]))
```

### Defining SLOs

Based on the previous metrics, you should now be able to define a reasonable SLO for your benchmark. To get started: 

1. Increase (or decrease) the load on your webserver. What happens to the metrics?
2. Increase (or decrease) the number of webserver instances/resources. Again, what happens to your metrics?
