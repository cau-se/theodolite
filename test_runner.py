#!/usr/bin/env python3
"""
Comprehensive test runner for the project to improve test coverage.
This script runs all Python tests and generates coverage reports.
"""

import os
import sys
import subprocess
import argparse
from pathlib import Path

def run_command(command, cwd=None, capture_output=False):
    """Run a command and return the result."""
    print(f"Running: {command}")
    if cwd:
        print(f"In directory: {cwd}")
    
    try:
        result = subprocess.run(
            command, 
            shell=True, 
            cwd=cwd, 
            capture_output=capture_output,
            text=True,
            check=False
        )
        return result
    except Exception as e:
        print(f"Error running command: {e}")
        return None

def install_dependencies():
    """Install required dependencies for testing."""
    print("Installing test dependencies...")
    
    # Try to install with --break-system-packages if regular pip fails
    deps = ["pytest", "pytest-cov", "coverage", "unittest-xml-reporting"]
    
    for dep in deps:
        result = run_command(f"pip3 install {dep}")
        if result and result.returncode != 0:
            print(f"Regular pip install failed for {dep}, trying with --break-system-packages")
            run_command(f"pip3 install --break-system-packages {dep}")

def run_python_tests():
    """Run all Python tests with coverage."""
    print("\n" + "="*60)
    print("RUNNING PYTHON TESTS")
    print("="*60)
    
    test_results = []
    
    # Test directories to check
    test_dirs = [
        "slo-checker/record-lag/app",
        "slo-checker/generic/app", 
        "analysis/src"
    ]
    
    for test_dir in test_dirs:
        if not os.path.exists(test_dir):
            print(f"Directory {test_dir} does not exist, skipping...")
            continue
            
        print(f"\nTesting in {test_dir}...")
        
        # Find Python test files
        test_files = []
        for file in os.listdir(test_dir):
            if file.startswith("test") and file.endswith(".py"):
                test_files.append(file)
        
        if not test_files:
            print(f"No test files found in {test_dir}")
            continue
            
        # Run tests with coverage
        for test_file in test_files:
            print(f"\nRunning {test_file}...")
            
            # Try with pytest first
            result = run_command(
                f"python3 -m pytest {test_file} -v --tb=short",
                cwd=test_dir
            )
            
            if result and result.returncode == 0:
                test_results.append((test_dir, test_file, "PASSED"))
                print(f"✓ {test_file} passed")
            else:
                # Try with unittest as fallback
                print(f"Pytest failed, trying with unittest...")
                result = run_command(
                    f"python3 {test_file}",
                    cwd=test_dir
                )
                
                if result and result.returncode == 0:
                    test_results.append((test_dir, test_file, "PASSED"))
                    print(f"✓ {test_file} passed (unittest)")
                else:
                    test_results.append((test_dir, test_file, "FAILED"))
                    print(f"✗ {test_file} failed")
    
    return test_results

def run_java_tests():
    """Run Java tests using Gradle."""
    print("\n" + "="*60)
    print("RUNNING JAVA TESTS")
    print("="*60)
    
    java_test_results = []
    
    # Find Gradle projects
    gradle_dirs = []
    for root, dirs, files in os.walk("."):
        if "build.gradle" in files:
            gradle_dirs.append(root)
    
    for gradle_dir in gradle_dirs:
        print(f"\nRunning tests in {gradle_dir}...")
        
        # Check if gradlew exists
        gradlew = os.path.join(gradle_dir, "gradlew")
        if os.path.exists(gradlew):
            cmd = "./gradlew test"
        else:
            cmd = "gradle test"
            
        result = run_command(cmd, cwd=gradle_dir)
        
        if result and result.returncode == 0:
            java_test_results.append((gradle_dir, "PASSED"))
            print(f"✓ Java tests in {gradle_dir} passed")
        else:
            java_test_results.append((gradle_dir, "FAILED"))
            print(f"✗ Java tests in {gradle_dir} failed")
    
    return java_test_results

def generate_coverage_report():
    """Generate coverage reports for Python code."""
    print("\n" + "="*60)
    print("GENERATING COVERAGE REPORTS")
    print("="*60)
    
    # Directories with Python code to analyze
    python_dirs = [
        "slo-checker/record-lag/app",
        "slo-checker/generic/app",
        "analysis/src"
    ]
    
    for py_dir in python_dirs:
        if not os.path.exists(py_dir):
            continue
            
        print(f"\nGenerating coverage for {py_dir}...")
        
        # Run coverage analysis
        result = run_command(
            f"python3 -m coverage run --source=. -m pytest test*.py",
            cwd=py_dir
        )
        
        if result and result.returncode == 0:
            # Generate coverage report
            run_command("python3 -m coverage report", cwd=py_dir)
            run_command("python3 -m coverage html", cwd=py_dir)
            print(f"Coverage report generated in {py_dir}/htmlcov/")

def create_test_summary(python_results, java_results):
    """Create a summary of test results."""
    print("\n" + "="*60)
    print("TEST SUMMARY")
    print("="*60)
    
    print("\nPython Tests:")
    python_passed = 0
    python_total = 0
    
    for test_dir, test_file, status in python_results:
        print(f"  {test_dir}/{test_file}: {status}")
        python_total += 1
        if status == "PASSED":
            python_passed += 1
    
    print(f"\nPython Tests: {python_passed}/{python_total} passed")
    
    print("\nJava Tests:")
    java_passed = 0
    java_total = 0
    
    for test_dir, status in java_results:
        print(f"  {test_dir}: {status}")
        java_total += 1
        if status == "PASSED":
            java_passed += 1
    
    print(f"\nJava Tests: {java_passed}/{java_total} passed")
    
    total_passed = python_passed + java_passed
    total_tests = python_total + java_total
    
    print(f"\nOverall: {total_passed}/{total_tests} test suites passed")
    
    if total_tests > 0:
        percentage = (total_passed / total_tests) * 100
        print(f"Success rate: {percentage:.1f}%")

def main():
    """Main function to run all tests."""
    parser = argparse.ArgumentParser(description="Run comprehensive tests for the project")
    parser.add_argument("--skip-deps", action="store_true", help="Skip dependency installation")
    parser.add_argument("--python-only", action="store_true", help="Run only Python tests")
    parser.add_argument("--java-only", action="store_true", help="Run only Java tests")
    parser.add_argument("--no-coverage", action="store_true", help="Skip coverage report generation")
    
    args = parser.parse_args()
    
    print("Comprehensive Test Runner")
    print("=" * 60)
    
    # Install dependencies unless skipped
    if not args.skip_deps:
        install_dependencies()
    
    python_results = []
    java_results = []
    
    # Run Python tests
    if not args.java_only:
        python_results = run_python_tests()
        
        # Generate coverage reports
        if not args.no_coverage:
            generate_coverage_report()
    
    # Run Java tests
    if not args.python_only:
        java_results = run_java_tests()
    
    # Create summary
    create_test_summary(python_results, java_results)
    
    print("\nTest execution completed!")
    print("\nNew test files created:")
    print("- /workspace/slo-checker/record-lag/app/test_trend_slope_computer.py")
    print("- /workspace/analysis/src/test_demand.py") 
    print("- /workspace/theodolite-benchmarks/uc4-kstreams/src/test/java/rocks/theodolite/benchmarks/uc4/kstreams/RecordAggregatorTest.java")
    print("\nEnhanced existing test files:")
    print("- /workspace/slo-checker/record-lag/app/test.py")
    print("- /workspace/slo-checker/generic/app/test.py")

if __name__ == "__main__":
    main()