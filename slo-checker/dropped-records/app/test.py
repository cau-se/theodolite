import unittest
from main import app, check_service_level_objective
import numpy as np
import json
from fastapi.testclient import TestClient

class TestSloEvaluation(unittest.TestCase):
    client = TestClient(app)

    def test_1_rep(self):
        with open('../resources/test-1-rep-success.json') as json_file:
            data = json.load(json_file)
            response = self.client.post("/dropped-records", json=data)
            self.assertEquals(response.json(), True)

    def test_check_service_level_objective(self):
        list = [ x for x in range(-100, 100) ]

        self.assertEquals(check_service_level_objective(list, 90), False)
        self.assertEquals(check_service_level_objective(list, 110), True)

if __name__ == '__main__':
    unittest.main()