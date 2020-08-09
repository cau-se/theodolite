import os

def execute():
    with open("last_exp_result.txt", "r") as file:
        result = file.read()
        return int(result) == 1
