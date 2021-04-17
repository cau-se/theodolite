from sklearn.linear_model import LinearRegression
import pandas as pd
import os

def compute(data, warmup_sec):
    data['sec_start'] = data.loc[0:, 'timestamp'] - data.iloc[0]['timestamp']
    print(data)
    regress = data.loc[data['sec_start'] >= warmup_sec] # Warm-Up

    print(regress)

    X = regress.iloc[:, 'timestamp'].values.reshape(-1, 1)  # values converts it into a numpy array
    print(X)
    Y = regress.iloc[:, 'value'].values.reshape(-1, 1)  # -1 means that calculate the dimension of rows, but have 1 column
    print(Y)
    linear_regressor = LinearRegression()  # create object for the class
    linear_regressor.fit(X, Y)  # perform linear regression
    Y_pred = linear_regressor.predict(X)  # make predictions

    trend_slope = linear_regressor.coef_[0][0]

    return trend_slope
