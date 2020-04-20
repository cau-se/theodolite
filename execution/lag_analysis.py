import sys
import os
import requests
from datetime import datetime, timedelta, timezone
import pandas as pd
import matplotlib.pyplot as plt
import csv

#
exp_id =  sys.argv[1]
benchmark = sys.argv[2]
dim_value = sys.argv[3]
instances = sys.argv[4]
execution_minutes = 5
time_diff_ms = int(os.getenv('CLOCK_DIFF_MS', 0))

#http://localhost:9090/api/v1/query_range?query=sum%20by(job,topic)(kafka_consumer_consumer_fetch_manager_metrics_records_lag)&start=2015-07-01T20:10:30.781Z&end=2020-07-01T20:11:00.781Z&step=15s

now_local = datetime.utcnow().replace(tzinfo=timezone.utc).replace(microsecond=0)
now = now_local - timedelta(milliseconds=time_diff_ms)
print(f"Now Local: {now_local}")
print(f"Now Used: {now}")

end = now
start = now - timedelta(minutes=execution_minutes)

#print(start.isoformat().replace('+00:00', 'Z'))
#print(end.isoformat().replace('+00:00', 'Z'))

response = requests.get('http://kube1.se.internal:32529/api/v1/query_range', params={
    #'query': "sum by(job,topic)(kafka_consumer_consumer_fetch_manager_metrics_records_lag)",
    'query': "sum by(group, topic)(kafka_consumergroup_group_lag > 0)",
    'start': start.isoformat(),
    'end': end.isoformat(),
    'step': '5s'})

#response
#print(response.request.path_url)
#response.content
results = response.json()['data']['result']

d = []

for result in results:
    #print(result['metric']['topic'])
    topic = result['metric']['topic']
    for value in result['values']:
        #print(value)
        d.append({'topic': topic, 'timestamp': int(value[0]), 'value': int(value[1]) if value[1] != 'NaN' else 0})

df = pd.DataFrame(d)

input = df.loc[df['topic'] == "input"]

#input.plot(kind='line',x='timestamp',y='value',color='red')
#plt.show()

from sklearn.linear_model import LinearRegression

X = input.iloc[:, 1].values.reshape(-1, 1)  # values converts it into a numpy array
Y = input.iloc[:, 2].values.reshape(-1, 1)  # -1 means that calculate the dimension of rows, but have 1 column
linear_regressor = LinearRegression()  # create object for the class
linear_regressor.fit(X, Y)  # perform linear regression
Y_pred = linear_regressor.predict(X)  # make predictions

print(linear_regressor.coef_)

#print(Y_pred)

fields=[exp_id, datetime.now(), benchmark, dim_value, instances, linear_regressor.coef_]
print(fields)
with open(r'results.csv', 'a') as f:
    writer = csv.writer(f)
    writer.writerow(fields)

filename = f"exp{exp_id}_{benchmark}_{dim_value}_{instances}"

plt.plot(X, Y)
plt.plot(X, Y_pred, color='red')

plt.savefig(f"{filename}_plot.png")

df.to_csv(f"{filename}_values.csv")


# Load partition count

response = requests.get('http://kube1.se.internal:32529/api/v1/query_range', params={
    'query': "count by(group,topic)(kafka_consumergroup_group_offset > 0)",
    'start': start.isoformat(),
    'end': end.isoformat(),
    'step': '5s'})

results = response.json()['data']['result']

d = []

for result in results:
    #print(result['metric']['topic'])
    topic = result['metric']['topic']
    for value in result['values']:
        #print(value)
        d.append({'topic': topic, 'timestamp': int(value[0]), 'value': int(value[1]) if value[1] != 'NaN' else 0})

df = pd.DataFrame(d)

df.to_csv(f"{filename}_partitions.csv")


# Load instances count

response = requests.get('http://kube1.se.internal:32529/api/v1/query_range', params={
    'query': "count(count (kafka_consumer_consumer_fetch_manager_metrics_records_lag) by(pod))",
    'start': start.isoformat(),
    'end': end.isoformat(),
    'step': '5s'})

results = response.json()['data']['result']

d = []

for result in results:
    for value in result['values']:
        #print(value)
        d.append({'timestamp': int(value[0]), 'value': int(value[1])})

df = pd.DataFrame(d)

df.to_csv(f"{filename}_instances.csv")