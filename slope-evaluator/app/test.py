import unittest
from main import app, check_service_level_objective
import json
from fastapi.testclient import TestClient

class TestSloEvaluation(unittest.TestCase):
    client = TestClient(app)

    def test_1_rep(self):
        with open('../resources/test-1-rep-success.json') as json_file:
            data = json.load(json_file)
            response = self.client.post("/evaluate-slope", json=data)
            self.assertEquals(response.json(), True)

    def test_3_rep(self):
        with open('../resources/test-3-rep-success.json') as json_file:
            data = json.load(json_file)
            response = self.client.post("/evaluate-slope", json=data)
            self.assertEquals(response.json(), True)
        
    def test_check_service_level_objective(self):
        list = [1,2,3,4]
        self.assertEquals(check_service_level_objective(list, 2), False)
        self.assertEquals(check_service_level_objective(list, 3), True)
        list = [1,2,3,4,5]
        self.assertEquals(check_service_level_objective(list, 2), False)
        self.assertEquals(check_service_level_objective(list, 4), True)

if __name__ == '__main__':
    unittest.main()