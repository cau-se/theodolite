from sklearn.linear_model import LinearRegression
import pandas as pd
import os

def compute(directory, filename, warmup_sec, threshold):
    df = pd.read_csv(os.path.join(directory, filename))
    input = df
    input['sec_start'] = input.loc[0:, 'timestamp'] - input.iloc[0]['timestamp']
    regress = input.loc[input['sec_start'] >= warmup_sec] # Warm-Up

    X = regress.iloc[:, 2].values.reshape(-1, 1)  # values converts it into a numpy array
    Y = regress.iloc[:, 3].values.reshape(-1, 1)  # -1 means that calculate the dimension of rows, but have 1 column
    linear_regressor = LinearRegression()  # create object for the class
    linear_regressor.fit(X, Y)  # perform linear regression
    Y_pred = linear_regressor.predict(X)  # make predictions

    trend_slope = linear_regressor.coef_[0][0]

    return trend_slope