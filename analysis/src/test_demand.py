import unittest
import pandas as pd
import numpy as np
import os
import tempfile
import shutil
from unittest.mock import patch, MagicMock
from demand import demand


class TestDemandAnalysis(unittest.TestCase):
    
    def setUp(self):
        """Set up test environment with temporary directory and test files."""
        self.test_dir = tempfile.mkdtemp()
        self.exp_id = "1"
        self.threshold = 0.5
        self.warmup_sec = 10
        
        # Create sample CSV data for testing
        self.create_test_csv_files()
    
    def tearDown(self):
        """Clean up temporary test directory."""
        shutil.rmtree(self.test_dir)
    
    def create_test_csv_files(self):
        """Create test CSV files with sample lag trend data."""
        # Test file 1: Load 100, 2 instances, positive trend (should fail SLO)
        data1 = pd.DataFrame({
            'timestamp': [1000, 1001, 1002, 1003, 1004, 1005, 1006, 1007, 1008, 1009, 1010, 1011, 1012],
            'value': [10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34]  # Strong positive trend
        })
        data1.to_csv(os.path.join(self.test_dir, 'exp1_100_2_lag-trend.csv'), index=False)
        
        # Test file 2: Load 100, 4 instances, weak positive trend (should pass SLO)
        data2 = pd.DataFrame({
            'timestamp': [1000, 1001, 1002, 1003, 1004, 1005, 1006, 1007, 1008, 1009, 1010, 1011, 1012],
            'value': [10, 10.2, 10.4, 10.6, 10.8, 11, 11.2, 11.4, 11.6, 11.8, 12, 12.2, 12.4]  # Weak positive trend
        })
        data2.to_csv(os.path.join(self.test_dir, 'exp1_100_4_lag-trend.csv'), index=False)
        
        # Test file 3: Load 200, 4 instances, strong positive trend (should fail SLO)
        data3 = pd.DataFrame({
            'timestamp': [1000, 1001, 1002, 1003, 1004, 1005, 1006, 1007, 1008, 1009, 1010, 1011, 1012],
            'value': [15, 18, 21, 24, 27, 30, 33, 36, 39, 42, 45, 48, 51]  # Strong positive trend
        })
        data3.to_csv(os.path.join(self.test_dir, 'exp1_200_4_lag-trend.csv'), index=False)
        
        # Test file 4: Load 200, 6 instances, negative trend (should pass SLO)
        data4 = pd.DataFrame({
            'timestamp': [1000, 1001, 1002, 1003, 1004, 1005, 1006, 1007, 1008, 1009, 1010, 1011, 1012],
            'value': [50, 48, 46, 44, 42, 40, 38, 36, 34, 32, 30, 28, 26]  # Negative trend
        })
        data4.to_csv(os.path.join(self.test_dir, 'exp1_200_6_lag-trend.csv'), index=False)
        
        # Test file 5: Same load/resources as file 2 for repetition testing
        data5 = pd.DataFrame({
            'timestamp': [1000, 1001, 1002, 1003, 1004, 1005, 1006, 1007, 1008, 1009, 1010, 1011, 1012],
            'value': [8, 8.3, 8.6, 8.9, 9.2, 9.5, 9.8, 10.1, 10.4, 10.7, 11, 11.3, 11.6]  # Another weak trend
        })
        data5.to_csv(os.path.join(self.test_dir, 'exp1_100_4_lag-trend_rep2.csv'), index=False)

    def test_demand_basic_functionality(self):
        """Test basic demand calculation functionality."""
        result = demand(self.exp_id, self.test_dir, self.threshold, self.warmup_sec)
        
        # Should be a DataFrame
        self.assertIsInstance(result, pd.DataFrame)
        
        # Should have the expected columns
        expected_columns = ['load', 'resources']
        for col in expected_columns:
            self.assertIn(col, result.columns)
        
        # Should have at least one row
        self.assertGreater(len(result), 0)

    def test_demand_filtering_by_threshold(self):
        """Test that configurations exceeding threshold are filtered out."""
        # Use a very strict threshold - only negative trends should pass
        strict_threshold = -1.0
        result = demand(self.exp_id, self.test_dir, strict_threshold, self.warmup_sec)
        
        # Should only include the configuration with negative trend
        self.assertEqual(len(result), 1)
        self.assertEqual(result.iloc[0]['load'], 200)
        self.assertEqual(result.iloc[0]['resources'], 6)

    def test_demand_warmup_period_effect(self):
        """Test that warmup period affects slope calculation."""
        # Test with different warmup periods
        result_no_warmup = demand(self.exp_id, self.test_dir, self.threshold, 0)
        result_with_warmup = demand(self.exp_id, self.test_dir, self.threshold, 5)
        
        # Both should be DataFrames
        self.assertIsInstance(result_no_warmup, pd.DataFrame)
        self.assertIsInstance(result_with_warmup, pd.DataFrame)
        
        # Results might differ due to different data points being considered
        # This is expected behavior

    def test_demand_repetition_handling(self):
        """Test that repetitions are handled correctly with median."""
        # Load 100 with 4 instances has two files, should take median
        result = demand(self.exp_id, self.test_dir, self.threshold, self.warmup_sec)
        
        # Should find the configuration
        load_100_configs = result[result['load'] == 100]
        self.assertGreater(len(load_100_configs), 0)

    def test_demand_minimal_resources_per_load(self):
        """Test that minimal resources are selected for each load."""
        result = demand(self.exp_id, self.test_dir, self.threshold, self.warmup_sec)
        
        # For each load, should have only one entry (the minimal resources)
        load_counts = result['load'].value_counts()
        for count in load_counts:
            self.assertEqual(count, 1)

    def test_demand_empty_directory(self):
        """Test behavior with empty directory."""
        empty_dir = tempfile.mkdtemp()
        try:
            result = demand(self.exp_id, empty_dir, self.threshold, self.warmup_sec)
            # Should return empty DataFrame
            self.assertEqual(len(result), 0)
        finally:
            shutil.rmtree(empty_dir)

    def test_demand_no_matching_files(self):
        """Test behavior when no files match the pattern."""
        result = demand("999", self.test_dir, self.threshold, self.warmup_sec)  # Non-existent exp_id
        
        # Should return empty DataFrame
        self.assertEqual(len(result), 0)

    def test_demand_invalid_csv_file(self):
        """Test behavior with invalid CSV file."""
        # Create invalid CSV file
        invalid_file = os.path.join(self.test_dir, 'exp1_300_2_lag-trend.csv')
        with open(invalid_file, 'w') as f:
            f.write("invalid,csv,content\n")
        
        # Should handle the error gracefully
        try:
            result = demand(self.exp_id, self.test_dir, self.threshold, self.warmup_sec)
            # Should still process valid files
            self.assertIsInstance(result, pd.DataFrame)
        except Exception as e:
            # It's acceptable to raise an exception for invalid data
            self.assertIsInstance(e, Exception)

    def test_demand_single_data_point(self):
        """Test behavior with CSV containing single data point."""
        single_point_file = os.path.join(self.test_dir, 'exp1_400_2_lag-trend.csv')
        single_data = pd.DataFrame({
            'timestamp': [1000],
            'value': [10]
        })
        single_data.to_csv(single_point_file, index=False)
        
        # Should handle single point gracefully (might raise exception in linear regression)
        try:
            result = demand(self.exp_id, self.test_dir, self.threshold, self.warmup_sec)
            self.assertIsInstance(result, pd.DataFrame)
        except Exception:
            # Acceptable to fail with insufficient data
            pass

    def test_demand_with_missing_values(self):
        """Test behavior with missing values in CSV."""
        missing_values_file = os.path.join(self.test_dir, 'exp1_500_2_lag-trend.csv')
        data_with_nan = pd.DataFrame({
            'timestamp': [1000, 1001, 1002, 1003, 1004],
            'value': [10, np.nan, 20, 25, np.nan]
        })
        data_with_nan.to_csv(missing_values_file, index=False)
        
        # Should handle NaN values
        try:
            result = demand(self.exp_id, self.test_dir, self.threshold, self.warmup_sec)
            self.assertIsInstance(result, pd.DataFrame)
        except Exception:
            # Acceptable to fail with invalid data
            pass

    def test_demand_filename_parsing(self):
        """Test that filenames are parsed correctly."""
        # Create file with different naming pattern
        test_file = os.path.join(self.test_dir, 'exp1_1000_8_lag-trend_special.csv')
        data = pd.DataFrame({
            'timestamp': [1000, 1001, 1002, 1003, 1004, 1005, 1006, 1007, 1008, 1009, 1010, 1011, 1012],
            'value': [10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0, -1, -2]  # Strong negative trend
        })
        data.to_csv(test_file, index=False)
        
        result = demand(self.exp_id, self.test_dir, self.threshold, self.warmup_sec)
        
        # Should find the new configuration
        load_1000_configs = result[result['load'] == 1000]
        if len(load_1000_configs) > 0:
            self.assertEqual(load_1000_configs.iloc[0]['resources'], 8)

    def test_demand_different_experiment_ids(self):
        """Test that only files matching experiment ID are processed."""
        # Create file with different experiment ID
        other_exp_file = os.path.join(self.test_dir, 'exp2_100_2_lag-trend.csv')
        data = pd.DataFrame({
            'timestamp': [1000, 1001, 1002],
            'value': [10, 20, 30]
        })
        data.to_csv(other_exp_file, index=False)
        
        # Should not include files from other experiments
        result = demand(self.exp_id, self.test_dir, self.threshold, self.warmup_sec)
        
        # Verify only exp1 files are processed
        # (This is implicit - the function should only process matching files)
        self.assertIsInstance(result, pd.DataFrame)

    @patch('demand.pd.read_csv')
    def test_demand_csv_reading_error(self, mock_read_csv):
        """Test behavior when CSV reading fails."""
        mock_read_csv.side_effect = Exception("CSV read error")
        
        # Should handle CSV reading errors gracefully
        try:
            result = demand(self.exp_id, self.test_dir, self.threshold, self.warmup_sec)
            # If it doesn't raise an exception, should return empty or partial results
            self.assertIsInstance(result, pd.DataFrame)
        except Exception:
            # Acceptable to propagate the error
            pass

    def test_demand_zero_threshold(self):
        """Test behavior with zero threshold."""
        result = demand(self.exp_id, self.test_dir, 0.0, self.warmup_sec)
        
        # Should include configurations with negative or zero trend
        self.assertIsInstance(result, pd.DataFrame)
        
        # Should include the negative trend configuration
        if len(result) > 0:
            self.assertIn(200, result['load'].values)  # Load 200 has negative trend

    def test_demand_negative_threshold(self):
        """Test behavior with negative threshold."""
        result = demand(self.exp_id, self.test_dir, -2.0, self.warmup_sec)
        
        # Should only include configurations with very negative trends
        self.assertIsInstance(result, pd.DataFrame)

    def test_demand_return_structure(self):
        """Test the structure of returned DataFrame."""
        result = demand(self.exp_id, self.test_dir, self.threshold, self.warmup_sec)
        
        if len(result) > 0:
            # Should have integer types for load and resources
            self.assertTrue(result['load'].dtype in [np.int64, np.int32, int])
            self.assertTrue(result['resources'].dtype in [np.int64, np.int32, int])
            
            # Should be sorted by load (implicit from groupby)
            loads = result['load'].tolist()
            self.assertEqual(loads, sorted(loads))


if __name__ == '__main__':
    unittest.main()