import unittest
from main import app, get_aggr_func, check_result
import json
from fastapi.testclient import TestClient

class TestSloEvaluation(unittest.TestCase):
    client = TestClient(app)

    def test_1_rep(self):
        with open('../resources/test-1-rep-success.json') as json_file:
            data = json.load(json_file)
            response = self.client.post("/", json=data)
            self.assertEqual(response.json(), True)

    def test_get_aggr_func_mean(self):
        self.assertEqual(get_aggr_func('median'), 'median')
    
    def test_get_aggr_func_p99(self):
        self.assertTrue(callable(get_aggr_func('p99')))

    def test_get_aggr_func_p99_9(self):
        self.assertTrue(callable(get_aggr_func('p99.9')))

    def test_get_aggr_func_p99_99(self):
        self.assertTrue(callable(get_aggr_func('p99.99')))

    def test_get_aggr_func_p0_1(self):
        self.assertTrue(callable(get_aggr_func('p0.1')))

    def test_get_aggr_func_p99_(self):
        self.assertRaises(ValueError, get_aggr_func, 'p99.')

    def test_get_aggr_func_p99_(self):
        self.assertRaises(ValueError, get_aggr_func, 'q99')

    def test_get_aggr_func_p99_(self):
        self.assertRaises(ValueError, get_aggr_func, 'mux')
    
    def test_check_result_lt(self):
        self.assertEqual(check_result(100, 'lt', 200), True)
        
    def test_check_result_lte(self):
        self.assertEqual(check_result(200, 'lte', 200), True)
    
    def test_check_result_gt(self):
        self.assertEqual(check_result(100, 'gt', 200), False)

    def test_check_result_gte(self):
        self.assertEqual(check_result(300, 'gte', 200), True)

    def test_check_result_invalid(self):
        self.assertRaises(ValueError, check_result, 100, 'xyz', 200)


if __name__ == '__main__':
    unittest.main()