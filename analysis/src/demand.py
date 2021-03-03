import os
from datetime import datetime, timedelta, timezone
import pandas as pd
from sklearn.linear_model import LinearRegression

def demand(exp_id, directory, threshold, warmup_sec):
    raw_runs = []

    # Compute SL, i.e., lag trend, for each tested configuration
    filenames = [filename for filename in os.listdir(directory) if filename.startswith(f"exp{exp_id}") and filename.endswith("totallag.csv")]
    for filename in filenames:
        #print(filename)
        run_params = filename[:-4].split("_")
        dim_value = run_params[2]
        instances = run_params[3]

        df = pd.read_csv(os.path.join(directory, filename))
        #input = df.loc[df['topic'] == "input"]
        input = df
        #print(input)
        input['sec_start'] = input.loc[0:, 'timestamp'] - input.iloc[0]['timestamp']
        #print(input)
        #print(input.iloc[0, 'timestamp'])
        regress = input.loc[input['sec_start'] >= warmup_sec] # Warm-Up
        #regress = input

        #input.plot(kind='line',x='timestamp',y='value',color='red')
        #plt.show()

        X = regress.iloc[:, 2].values.reshape(-1, 1)  # values converts it into a numpy array
        Y = regress.iloc[:, 3].values.reshape(-1, 1)  # -1 means that calculate the dimension of rows, but have 1 column
        linear_regressor = LinearRegression()  # create object for the class
        linear_regressor.fit(X, Y)  # perform linear regression
        Y_pred = linear_regressor.predict(X)  # make predictions

        trend_slope = linear_regressor.coef_[0][0]
        #print(linear_regressor.coef_)

        row = {'load': int(dim_value), 'resources': int(instances), 'trend_slope': trend_slope}
        #print(row)
        raw_runs.append(row)

    runs = pd.DataFrame(raw_runs)

    # Set suitable = True if SLOs are met, i.e., lag trend is below threshold
    runs["suitable"] =  runs.apply(lambda row: row['trend_slope'] < threshold, axis=1)

    # Sort results table (unsure if required)
    runs.columns = runs.columns.str.strip()
    runs.sort_values(by=["load", "resources"])

    # Filter only suitable configurations
    filtered = runs[runs.apply(lambda x: x['suitable'], axis=1)]

    # Compute demand per load intensity
    grouped = filtered.groupby(['load'])['resources'].min()
    demand_per_load = grouped.to_frame().reset_index()

    return demand_per_load
