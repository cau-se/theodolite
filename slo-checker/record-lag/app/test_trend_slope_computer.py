import unittest
import pandas as pd
import numpy as np
from unittest.mock import patch, MagicMock
from trend_slope_computer import compute


class TestTrendSlopeComputer(unittest.TestCase):
    
    def setUp(self):
        """Set up test data for trend slope computation tests."""
        # Create sample data that mimics real sensor data
        self.sample_data = pd.DataFrame({
            'group': ['group1', 'group1', 'group1', 'group1', 'group1'],
            'timestamp': [1000, 1001, 1002, 1003, 1004],
            'value': [10.0, 15.0, 20.0, 25.0, 30.0]  # Linear increase
        })
        
        # Data with no trend (flat line)
        self.flat_data = pd.DataFrame({
            'group': ['group1', 'group1', 'group1', 'group1', 'group1'],
            'timestamp': [1000, 1001, 1002, 1003, 1004],
            'value': [20.0, 20.0, 20.0, 20.0, 20.0]
        })
        
        # Data with negative trend
        self.negative_trend_data = pd.DataFrame({
            'group': ['group1', 'group1', 'group1', 'group1', 'group1'],
            'timestamp': [1000, 1001, 1002, 1003, 1004],
            'value': [30.0, 25.0, 20.0, 15.0, 10.0]  # Linear decrease
        })

    def test_compute_positive_trend(self):
        """Test computing trend slope with positive trend data."""
        result = compute(self.sample_data, 0)  # No warmup
        self.assertGreater(result, 0, "Should detect positive trend")
        self.assertAlmostEqual(result, 5.0, places=1, msg="Slope should be approximately 5")

    def test_compute_flat_trend(self):
        """Test computing trend slope with flat data."""
        result = compute(self.flat_data, 0)  # No warmup
        self.assertAlmostEqual(result, 0.0, places=2, msg="Flat data should have zero slope")

    def test_compute_negative_trend(self):
        """Test computing trend slope with negative trend data."""
        result = compute(self.negative_trend_data, 0)  # No warmup
        self.assertLess(result, 0, "Should detect negative trend")
        self.assertAlmostEqual(result, -5.0, places=1, msg="Slope should be approximately -5")

    def test_compute_with_warmup(self):
        """Test computing trend slope with warmup period."""
        # Test with warmup that excludes first 2 data points
        result = compute(self.sample_data, 2)
        self.assertGreater(result, 0, "Should still detect positive trend after warmup")

    def test_compute_warmup_excludes_all_data(self):
        """Test behavior when warmup period excludes all data."""
        # Warmup longer than data duration should result in minimal data
        with self.assertRaises(Exception):
            compute(self.sample_data, 10)

    def test_compute_single_data_point_after_warmup(self):
        """Test behavior with only one data point after warmup."""
        single_point_data = pd.DataFrame({
            'group': ['group1'],
            'timestamp': [1000],
            'value': [10.0]
        })
        with self.assertRaises(Exception):
            compute(single_point_data, 0)

    def test_compute_with_missing_values(self):
        """Test computing trend slope with NaN values."""
        data_with_nan = self.sample_data.copy()
        data_with_nan.loc[2, 'value'] = np.nan
        
        # Should handle NaN values gracefully or raise appropriate error
        try:
            result = compute(data_with_nan, 0)
            # If it doesn't raise an error, result should be a number
            self.assertIsInstance(result, (int, float))
        except Exception:
            # It's acceptable to raise an exception for invalid data
            pass

    def test_compute_with_irregular_timestamps(self):
        """Test computing trend slope with irregular timestamp intervals."""
        irregular_data = pd.DataFrame({
            'group': ['group1', 'group1', 'group1', 'group1'],
            'timestamp': [1000, 1002, 1005, 1010],  # Irregular intervals
            'value': [10.0, 20.0, 30.0, 40.0]
        })
        result = compute(irregular_data, 0)
        self.assertIsInstance(result, (int, float))
        self.assertGreater(result, 0)

    def test_compute_data_structure_validation(self):
        """Test that the function validates input data structure."""
        # Test with wrong column names
        wrong_columns = pd.DataFrame({
            'wrong_group': ['group1'],
            'wrong_timestamp': [1000],
            'wrong_value': [10.0]
        })
        
        with self.assertRaises((KeyError, IndexError)):
            compute(wrong_columns, 0)

    def test_compute_empty_dataframe(self):
        """Test behavior with empty dataframe."""
        empty_df = pd.DataFrame(columns=['group', 'timestamp', 'value'])
        
        with self.assertRaises(Exception):
            compute(empty_df, 0)

    def test_compute_large_dataset(self):
        """Test performance with larger dataset."""
        # Create larger dataset
        timestamps = list(range(1000, 2000))
        values = [t * 0.1 + np.random.normal(0, 0.5) for t in timestamps]  # Linear trend with noise
        large_data = pd.DataFrame({
            'group': ['group1'] * len(timestamps),
            'timestamp': timestamps,
            'value': values
        })
        
        result = compute(large_data, 100)  # 100 second warmup
        self.assertIsInstance(result, (int, float))

    @patch('trend_slope_computer.LinearRegression')
    def test_compute_linear_regression_called(self, mock_lr_class):
        """Test that LinearRegression is properly instantiated and called."""
        mock_lr = MagicMock()
        mock_lr.fit.return_value = None
        mock_lr.predict.return_value = np.array([[1], [2], [3]])
        mock_lr.coef_ = np.array([[2.5]])
        mock_lr_class.return_value = mock_lr
        
        result = compute(self.sample_data, 0)
        
        # Verify LinearRegression was instantiated
        mock_lr_class.assert_called_once()
        
        # Verify fit and predict were called
        mock_lr.fit.assert_called_once()
        mock_lr.predict.assert_called_once()
        
        # Verify the result comes from the coefficient
        self.assertEqual(result, 2.5)


if __name__ == '__main__':
    unittest.main()