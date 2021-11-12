import os
from datetime import datetime, timedelta, timezone
import pandas as pd
from pandas.core.frame import DataFrame
from sklearn.linear_model import LinearRegression

def demand(exp_id, directory, threshold, warmup_sec):
    raw_runs = []

    # Compute SLI, i.e., lag trend, for each tested configuration
    filenames = [filename for filename in os.listdir(directory) if filename.startswith(f"exp{exp_id}") and "lag-trend" in filename and filename.endswith(".csv")]
    for filename in filenames:
        run_params = filename[:-4].split("_")
        dim_value = run_params[1]
        instances = run_params[2]

        df = pd.read_csv(os.path.join(directory, filename))
        input = df

        input['sec_start'] = input.loc[0:, 'timestamp'] - input.iloc[0]['timestamp']
    
        regress = input.loc[input['sec_start'] >= warmup_sec] # Warm-Up

        X = regress.iloc[:, 1].values.reshape(-1, 1)  # values converts it into a numpy array
        Y = regress.iloc[:, 2].values.reshape(-1, 1)  # -1 means that calculate the dimension of rows, but have 1 column

        linear_regressor = LinearRegression()  # create object for the class
        linear_regressor.fit(X, Y)  # perform linear regression
        Y_pred = linear_regressor.predict(X)  # make predictions

        trend_slope = linear_regressor.coef_[0][0]

        row = {'load': int(dim_value), 'resources': int(instances), 'trend_slope': trend_slope}
        raw_runs.append(row)

    runs = pd.DataFrame(raw_runs)

    # Group by the load and resources to handle repetitions, and take from the reptitions the median
    # for even reptitions, the mean of the two middle values is used
    medians = runs.groupby(by=['load', 'resources'], as_index=False).median()

    # Set suitable = True if SLOs are met, i.e., lag trend slope is below threshold
    medians["suitable"] =  medians.apply(lambda row: row['trend_slope'] < threshold, axis=1)

    suitable = medians[medians.apply(lambda x: x['suitable'], axis=1)]
    
    # Compute minimal demand per load intensity
    demand_per_load = suitable.groupby(by=['load'], as_index=False)['resources'].min()
    
    return demand_per_load

