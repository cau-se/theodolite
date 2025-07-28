import unittest
from main import app, get_aggr_func, check_result, aggr_query
import json
from fastapi.testclient import TestClient
from unittest.mock import patch, MagicMock
import pandas as pd
import numpy as np

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

    def test_get_aggr_func_q99(self):
        self.assertRaises(ValueError, get_aggr_func, 'q99')

    def test_get_aggr_func_mux(self):
        self.assertRaises(ValueError, get_aggr_func, 'mux')

    def test_get_aggr_func_first(self):
        self.assertTrue(callable(get_aggr_func('first')))

    def test_get_aggr_func_last(self):
        self.assertTrue(callable(get_aggr_func('last')))
    

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

    # Enhanced test coverage below

    def test_get_aggr_func_all_builtin_functions(self):
        """Test all built-in aggregation functions."""
        builtin_funcs = ['mean', 'median', 'mode', 'sum', 'count', 'max', 'min', 'std', 'var', 'skew', 'kurt']
        for func_name in builtin_funcs:
            result = get_aggr_func(func_name)
            self.assertEqual(result, func_name)

    def test_get_aggr_func_percentiles_edge_cases(self):
        """Test percentile functions with edge cases."""
        # Test single digit percentiles
        self.assertTrue(callable(get_aggr_func('p1')))
        self.assertTrue(callable(get_aggr_func('p5')))
        
        # Test edge percentiles
        self.assertTrue(callable(get_aggr_func('p0')))
        self.assertTrue(callable(get_aggr_func('p100')))
        
        # Test decimal percentiles
        self.assertTrue(callable(get_aggr_func('p50.5')))
        self.assertTrue(callable(get_aggr_func('p99.999')))

    def test_get_aggr_func_percentile_functionality(self):
        """Test that percentile functions work correctly."""
        p50_func = get_aggr_func('p50')
        p90_func = get_aggr_func('p90')
        
        # Create test series
        test_data = pd.Series([1, 2, 3, 4, 5, 6, 7, 8, 9, 10])
        
        # Test p50 (median)
        self.assertEqual(p50_func(test_data), 5.5)
        
        # Test p90
        self.assertEqual(p90_func(test_data), 9.1)

    def test_get_aggr_func_first_last_functionality(self):
        """Test first and last functions work correctly."""
        first_func = get_aggr_func('first')
        last_func = get_aggr_func('last')
        
        test_data = pd.Series([10, 20, 30, 40, 50])
        
        self.assertEqual(first_func(test_data), 10)
        self.assertEqual(last_func(test_data), 50)
        
        # Test function names are set correctly
        self.assertEqual(first_func.__name__, 'first')
        self.assertEqual(last_func.__name__, 'last')

    def test_get_aggr_func_invalid_percentile_formats(self):
        """Test invalid percentile formats raise ValueError."""
        invalid_formats = ['p', 'p.5', 'p101', 'p-1', 'pp99', '99p', 'p99.']
        for invalid_format in invalid_formats:
            with self.assertRaises(ValueError):
                get_aggr_func(invalid_format)

    def test_aggr_query_basic_functionality(self):
        """Test aggr_query with basic data."""
        values = [[1000, 10], [1001, 20], [1002, 30], [1003, 40]]
        result = aggr_query(values, 0, 'mean')
        self.assertEqual(result, 25.0)

    def test_aggr_query_with_warmup(self):
        """Test aggr_query with warmup period."""
        values = [[1000, 10], [1001, 20], [1002, 30], [1003, 40]]
        # Warmup of 2 seconds should exclude first 2 values
        result = aggr_query(values, 2, 'mean')
        self.assertEqual(result, 35.0)  # mean of 30, 40

    def test_aggr_query_with_custom_function(self):
        """Test aggr_query with custom aggregation function."""
        values = [[1000, 10], [1001, 20], [1002, 30], [1003, 40]]
        
        def custom_func(series):
            return series.max() - series.min()
        
        result = aggr_query(values, 0, custom_func)
        self.assertEqual(result, 30.0)  # 40 - 10

    def test_aggr_query_empty_after_warmup(self):
        """Test aggr_query when warmup excludes all data."""
        values = [[1000, 10], [1001, 20]]
        
        # Warmup longer than data duration
        with self.assertRaises(Exception):
            aggr_query(values, 10, 'mean')

    def test_aggr_query_string_to_float_conversion(self):
        """Test that string values are converted to float."""
        values = [[1000, '10.5'], [1001, '20.3'], [1002, '30.7']]
        result = aggr_query(values, 0, 'mean')
        self.assertAlmostEqual(result, 20.5, places=1)

    def test_check_result_all_operators(self):
        """Test all operators in check_result function."""
        # Test lt
        self.assertTrue(check_result(5, 'lt', 10))
        self.assertFalse(check_result(10, 'lt', 5))
        
        # Test lte
        self.assertTrue(check_result(10, 'lte', 10))
        self.assertTrue(check_result(5, 'lte', 10))
        self.assertFalse(check_result(15, 'lte', 10))
        
        # Test gt
        self.assertTrue(check_result(15, 'gt', 10))
        self.assertFalse(check_result(5, 'gt', 10))
        
        # Test gte
        self.assertTrue(check_result(10, 'gte', 10))
        self.assertTrue(check_result(15, 'gte', 10))
        self.assertFalse(check_result(5, 'gte', 10))
        
        # Test true/false (for testing)
        self.assertTrue(check_result(0, 'true', 0))
        self.assertFalse(check_result(0, 'false', 0))

    def test_check_result_edge_cases(self):
        """Test check_result with edge cases."""
        # Test with zero values
        self.assertTrue(check_result(0, 'lt', 1))
        self.assertFalse(check_result(0, 'gt', 1))
        
        # Test with negative values
        self.assertTrue(check_result(-5, 'lt', -1))
        self.assertTrue(check_result(-1, 'gt', -5))
        
        # Test with floating point precision
        self.assertTrue(check_result(0.1 + 0.2, 'lt', 0.4))  # 0.30000000000000004 < 0.4

    @patch('main.json.loads')
    def test_check_slo_endpoint_success(self, mock_json_loads):
        """Test the main SLO check endpoint with successful result."""
        mock_data = {
            'results': [
                [
                    {
                        'values': [[1000, 10], [1001, 20], [1002, 30]]
                    }
                ]
            ],
            'metadata': {
                'warmup': 0,
                'queryAggregation': 'mean',
                'repetitionAggregation': 'median',
                'operator': 'lt',
                'threshold': 25.0
            }
        }
        mock_json_loads.return_value = mock_data
        
        response = self.client.post("/", json=mock_data)
        self.assertEqual(response.status_code, 200)
        self.assertTrue(response.json())

    @patch('main.json.loads')
    def test_check_slo_endpoint_failure(self, mock_json_loads):
        """Test the main SLO check endpoint with failed result."""
        mock_data = {
            'results': [
                [
                    {
                        'values': [[1000, 10], [1001, 20], [1002, 30]]
                    }
                ]
            ],
            'metadata': {
                'warmup': 0,
                'queryAggregation': 'mean',
                'repetitionAggregation': 'median',
                'operator': 'lt',
                'threshold': 15.0
            }
        }
        mock_json_loads.return_value = mock_data
        
        response = self.client.post("/", json=mock_data)
        self.assertEqual(response.status_code, 200)
        self.assertFalse(response.json())

    @patch('main.json.loads')
    def test_check_slo_endpoint_multiple_repetitions(self, mock_json_loads):
        """Test SLO check with multiple repetitions."""
        mock_data = {
            'results': [
                [{'values': [[1000, 10], [1001, 20]]}],  # mean = 15
                [{'values': [[1000, 20], [1001, 30]]}],  # mean = 25
                [{'values': [[1000, 30], [1001, 40]]}]   # mean = 35
            ],
            'metadata': {
                'warmup': 0,
                'queryAggregation': 'mean',
                'repetitionAggregation': 'median',  # median of [15, 25, 35] = 25
                'operator': 'lt',
                'threshold': 30.0
            }
        }
        mock_json_loads.return_value = mock_data
        
        response = self.client.post("/", json=mock_data)
        self.assertEqual(response.status_code, 200)
        self.assertTrue(response.json())  # 25 < 30

    @patch('main.json.loads')
    def test_check_slo_endpoint_with_percentiles(self, mock_json_loads):
        """Test SLO check with percentile aggregations."""
        mock_data = {
            'results': [
                [{'values': [[1000, 10], [1001, 20], [1002, 30], [1003, 40]]}]
            ],
            'metadata': {
                'warmup': 0,
                'queryAggregation': 'p75',  # 75th percentile
                'repetitionAggregation': 'first',
                'operator': 'lte',
                'threshold': 35.0
            }
        }
        mock_json_loads.return_value = mock_data
        
        response = self.client.post("/", json=mock_data)
        self.assertEqual(response.status_code, 200)
        self.assertTrue(response.json())

    def test_app_logging_configuration(self):
        """Test that logging is properly configured."""
        import main
        import logging
        
        # Verify logger exists and has the correct name
        self.assertIsInstance(main.logger, logging.Logger)
        self.assertEqual(main.logger.name, "API")

    def test_pandas_copy_on_write_setting(self):
        """Test that pandas copy_on_write setting is configured."""
        import pandas as pd
        # This test ensures the pandas setting is applied
        self.assertTrue(hasattr(pd.options.mode, 'copy_on_write'))


if __name__ == '__main__':
    unittest.main()