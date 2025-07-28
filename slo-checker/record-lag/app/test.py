import unittest
from main import app, check_service_level_objective, calculate_slope_trend
import json
from fastapi.testclient import TestClient
from unittest.mock import patch, MagicMock
import pandas as pd

class TestSloEvaluation(unittest.TestCase):
    client = TestClient(app)

    def test_1_rep(self):
        with open('../resources/test-1-rep-success.json') as json_file:
            data = json.load(json_file)
            response = self.client.post("/evaluate-slope", json=data)
            self.assertEqual(response.json(), True)

    def test_3_rep(self):
        with open('../resources/test-3-rep-success.json') as json_file:
            data = json.load(json_file)
            response = self.client.post("/evaluate-slope", json=data)
            self.assertEqual(response.json(), True)

    def test_check_service_level_objective(self):
        list = [1,2,3,4]
        self.assertEqual(check_service_level_objective(list, 2), False)
        self.assertEqual(check_service_level_objective(list, 3), True)
        list = [1,2,3,4,5]
        self.assertEqual(check_service_level_objective(list, 2), False)
        self.assertEqual(check_service_level_objective(list, 4), True)

    def test_check_service_level_objective_edge_cases(self):
        """Test edge cases for service level objective checking."""
        # Test with single value
        self.assertTrue(check_service_level_objective([5], 10))
        self.assertFalse(check_service_level_objective([15], 10))
        
        # Test with two values (median of even number)
        self.assertTrue(check_service_level_objective([1, 3], 2.5))  # median is 2
        self.assertFalse(check_service_level_objective([3, 5], 3.5))  # median is 4
        
        # Test with identical values
        self.assertTrue(check_service_level_objective([5, 5, 5], 6))
        self.assertFalse(check_service_level_objective([5, 5, 5], 4))
        
        # Test with threshold exactly equal to median
        self.assertFalse(check_service_level_objective([1, 2, 3], 2))  # median = threshold = 2

    def test_check_service_level_objective_empty_list(self):
        """Test behavior with empty results list."""
        with self.assertRaises(Exception):
            check_service_level_objective([], 5)

    def test_calculate_slope_trend_basic(self):
        """Test calculate_slope_trend with basic data."""
        test_results = [
            {
                'metric': {'consumergroup': 'test-group'},
                'values': [
                    ['1000', '10.0'],
                    ['1001', '15.0'],
                    ['1002', '20.0']
                ]
            }
        ]
        
        with patch('main.trend_slope_computer.compute') as mock_compute:
            mock_compute.return_value = 5.0
            result = calculate_slope_trend(test_results, 0)
            self.assertEqual(result, 5.0)
            mock_compute.assert_called_once()

    def test_calculate_slope_trend_with_nan_values(self):
        """Test calculate_slope_trend with NaN values."""
        test_results = [
            {
                'metric': {'consumergroup': 'test-group'},
                'values': [
                    ['1000', '10.0'],
                    ['1001', 'NaN'],
                    ['1002', '20.0']
                ]
            }
        ]
        
        with patch('main.trend_slope_computer.compute') as mock_compute:
            mock_compute.return_value = 3.0
            result = calculate_slope_trend(test_results, 0)
            self.assertEqual(result, 3.0)
            
            # Verify that NaN was converted to 0
            call_args = mock_compute.call_args[0][0]  # Get the DataFrame passed to compute
            self.assertEqual(call_args.loc[call_args['value'] == 0].shape[0], 1)

    def test_calculate_slope_trend_missing_consumergroup(self):
        """Test calculate_slope_trend when consumergroup is missing."""
        test_results = [
            {
                'metric': {},  # No consumergroup
                'values': [
                    ['1000', '10.0'],
                    ['1001', '15.0']
                ]
            }
        ]
        
        with patch('main.trend_slope_computer.compute') as mock_compute:
            mock_compute.return_value = 2.0
            result = calculate_slope_trend(test_results, 0)
            self.assertEqual(result, 2.0)
            
            # Verify default group was used
            call_args = mock_compute.call_args[0][0]
            self.assertTrue((call_args['group'] == 'default').all())

    def test_calculate_slope_trend_computation_error(self):
        """Test calculate_slope_trend when computation fails."""
        test_results = [
            {
                'metric': {'consumergroup': 'test-group'},
                'values': [['1000', '10.0']]
            }
        ]
        
        with patch('main.trend_slope_computer.compute') as mock_compute:
            mock_compute.side_effect = Exception("Computation failed")
            result = calculate_slope_trend(test_results, 0)
            self.assertEqual(result, float('inf'))

    def test_calculate_slope_trend_multiple_groups(self):
        """Test calculate_slope_trend with multiple consumer groups."""
        test_results = [
            {
                'metric': {'consumergroup': 'group1'},
                'values': [['1000', '10.0'], ['1001', '15.0']]
            },
            {
                'metric': {'consumergroup': 'group2'},
                'values': [['1000', '20.0'], ['1001', '25.0']]
            }
        ]
        
        with patch('main.trend_slope_computer.compute') as mock_compute:
            mock_compute.return_value = 4.0
            result = calculate_slope_trend(test_results, 0)
            self.assertEqual(result, 4.0)
            
            # Verify DataFrame contains both groups
            call_args = mock_compute.call_args[0][0]
            self.assertIn('group1', call_args['group'].values)
            self.assertIn('group2', call_args['group'].values)

    @patch('main.json.loads')
    def test_evaluate_slope_endpoint_success(self, mock_json_loads):
        """Test the /evaluate-slope endpoint with successful evaluation."""
        mock_data = {
            'results': [
                [
                    {
                        'metric': {'consumergroup': 'test'},
                        'values': [['1000', '10.0'], ['1001', '15.0']]
                    }
                ]
            ],
            'metadata': {
                'warmup': 0,
                'threshold': 10.0
            }
        }
        mock_json_loads.return_value = mock_data
        
        with patch('main.calculate_slope_trend') as mock_calc, \
             patch('main.check_service_level_objective') as mock_check:
            mock_calc.return_value = 5.0
            mock_check.return_value = True
            
            response = self.client.post("/evaluate-slope", json=mock_data)
            self.assertEqual(response.status_code, 200)
            self.assertTrue(response.json())

    @patch('main.json.loads')
    def test_evaluate_slope_endpoint_failure(self, mock_json_loads):
        """Test the /evaluate-slope endpoint with failed evaluation."""
        mock_data = {
            'results': [
                [
                    {
                        'metric': {'consumergroup': 'test'},
                        'values': [['1000', '10.0'], ['1001', '25.0']]
                    }
                ]
            ],
            'metadata': {
                'warmup': 0,
                'threshold': 5.0
            }
        }
        mock_json_loads.return_value = mock_data
        
        with patch('main.calculate_slope_trend') as mock_calc, \
             patch('main.check_service_level_objective') as mock_check:
            mock_calc.return_value = 15.0
            mock_check.return_value = False
            
            response = self.client.post("/evaluate-slope", json=mock_data)
            self.assertEqual(response.status_code, 200)
            self.assertFalse(response.json())

    def test_evaluate_slope_endpoint_multiple_results(self):
        """Test the /evaluate-slope endpoint with multiple result sets."""
        test_data = {
            'results': [
                [
                    {
                        'metric': {'consumergroup': 'test1'},
                        'values': [['1000', '10.0'], ['1001', '15.0']]
                    }
                ],
                [
                    {
                        'metric': {'consumergroup': 'test2'},
                        'values': [['1000', '20.0'], ['1001', '25.0']]
                    }
                ]
            ],
            'metadata': {
                'warmup': 0,
                'threshold': 7.0
            }
        }
        
        with patch('main.calculate_slope_trend') as mock_calc, \
             patch('main.check_service_level_objective') as mock_check:
            mock_calc.side_effect = [5.0, 8.0]  # Different slopes for each result
            mock_check.return_value = False
            
            response = self.client.post("/evaluate-slope", json=test_data)
            self.assertEqual(response.status_code, 200)
            self.assertFalse(response.json())
            
            # Verify calculate_slope_trend was called twice
            self.assertEqual(mock_calc.call_count, 2)
            # Verify check_service_level_objective was called with both results
            mock_check.assert_called_once_with(results=[5.0, 8.0], threshold=7.0)

    def test_app_logging_configuration(self):
        """Test that logging is properly configured."""
        import main
        import logging
        
        # Verify logger exists and has the correct name
        self.assertIsInstance(main.logger, logging.Logger)
        self.assertEqual(main.logger.name, "API")

if __name__ == '__main__':
    unittest.main()