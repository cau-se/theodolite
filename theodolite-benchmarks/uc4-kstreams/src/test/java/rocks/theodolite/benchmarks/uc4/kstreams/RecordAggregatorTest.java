package rocks.theodolite.benchmarks.uc4.kstreams;

import static org.junit.Assert.*;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.kstream.internals.TimeWindow;
import org.junit.Before;
import org.junit.Test;
import rocks.theodolite.benchmarks.commons.model.records.ActivePowerRecord;
import rocks.theodolite.benchmarks.commons.model.records.AggregatedActivePowerRecord;

public class RecordAggregatorTest {

    private RecordAggregator aggregator;
    private Windowed<String> testIdentifier;
    private ActivePowerRecord testRecord;
    private AggregatedActivePowerRecord existingAggregated;

    @Before
    public void setUp() {
        aggregator = new RecordAggregator();
        
        // Create test windowed identifier
        TimeWindow window = new TimeWindow(1000L, 2000L);
        testIdentifier = new Windowed<>("test-sensor", window);
        
        // Create test active power record
        testRecord = new ActivePowerRecord("test-sensor", 1500L, 100.0);
        
        // Create existing aggregated record
        existingAggregated = new AggregatedActivePowerRecord("test-sensor", 1400L, 2L, 300.0, 150.0);
    }

    @Test
    public void testAddToNull() {
        // Test adding to null aggregated record (first record)
        AggregatedActivePowerRecord result = aggregator.add(testIdentifier, testRecord, null);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Identifier should match", "test-sensor", result.getIdentifier());
        assertEquals("Timestamp should match record timestamp", 1500L, result.getTimestamp());
        assertEquals("Count should be 1", 1L, result.getCount());
        assertEquals("Sum should equal record value", 100.0, result.getSumInW(), 0.001);
        assertEquals("Average should equal record value", 100.0, result.getAverageInW(), 0.001);
    }

    @Test
    public void testAddToExisting() {
        // Test adding to existing aggregated record
        AggregatedActivePowerRecord result = aggregator.add(testIdentifier, testRecord, existingAggregated);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Identifier should match", "test-sensor", result.getIdentifier());
        assertEquals("Timestamp should match record timestamp", 1500L, result.getTimestamp());
        assertEquals("Count should be incremented", 3L, result.getCount());
        assertEquals("Sum should be updated", 400.0, result.getSumInW(), 0.001);
        assertEquals("Average should be recalculated", 400.0/3.0, result.getAverageInW(), 0.001);
    }

    @Test
    public void testAddZeroValue() {
        // Test adding record with zero value
        ActivePowerRecord zeroRecord = new ActivePowerRecord("test-sensor", 1500L, 0.0);
        AggregatedActivePowerRecord result = aggregator.add(testIdentifier, zeroRecord, existingAggregated);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Count should be incremented", 3L, result.getCount());
        assertEquals("Sum should remain the same", 300.0, result.getSumInW(), 0.001);
        assertEquals("Average should be recalculated", 100.0, result.getAverageInW(), 0.001);
    }

    @Test
    public void testAddNegativeValue() {
        // Test adding record with negative value
        ActivePowerRecord negativeRecord = new ActivePowerRecord("test-sensor", 1500L, -50.0);
        AggregatedActivePowerRecord result = aggregator.add(testIdentifier, negativeRecord, existingAggregated);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Count should be incremented", 3L, result.getCount());
        assertEquals("Sum should be reduced", 250.0, result.getSumInW(), 0.001);
        assertEquals("Average should be recalculated", 250.0/3.0, result.getAverageInW(), 0.001);
    }

    @Test
    public void testAddLargeValue() {
        // Test adding record with very large value
        ActivePowerRecord largeRecord = new ActivePowerRecord("test-sensor", 1500L, Double.MAX_VALUE);
        AggregatedActivePowerRecord result = aggregator.add(testIdentifier, largeRecord, null);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Count should be 1", 1L, result.getCount());
        assertEquals("Sum should equal large value", Double.MAX_VALUE, result.getSumInW(), 0.001);
        assertEquals("Average should equal large value", Double.MAX_VALUE, result.getAverageInW(), 0.001);
    }

    @Test
    public void testSubtract() {
        // Test subtracting from existing aggregated record
        AggregatedActivePowerRecord result = aggregator.substract(testIdentifier, testRecord, existingAggregated);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Identifier should match", "test-sensor", result.getIdentifier());
        assertEquals("Timestamp should be -1", -1L, result.getTimestamp());
        assertEquals("Count should be decremented", 1L, result.getCount());
        assertEquals("Sum should be reduced", 200.0, result.getSumInW(), 0.001);
        assertEquals("Average should be recalculated", 200.0, result.getAverageInW(), 0.001);
    }

    @Test
    public void testSubtractToZeroCount() {
        // Test subtracting when result count becomes zero
        AggregatedActivePowerRecord singleRecord = new AggregatedActivePowerRecord("test-sensor", 1400L, 1L, 100.0, 100.0);
        AggregatedActivePowerRecord result = aggregator.substract(testIdentifier, testRecord, singleRecord);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Count should be zero", 0L, result.getCount());
        assertEquals("Sum should be zero", 0.0, result.getSumInW(), 0.001);
        assertEquals("Average should be zero when count is zero", 0.0, result.getAverageInW(), 0.001);
    }

    @Test
    public void testSubtractZeroValue() {
        // Test subtracting record with zero value
        ActivePowerRecord zeroRecord = new ActivePowerRecord("test-sensor", 1500L, 0.0);
        AggregatedActivePowerRecord result = aggregator.substract(testIdentifier, zeroRecord, existingAggregated);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Count should be decremented", 1L, result.getCount());
        assertEquals("Sum should remain the same", 300.0, result.getSumInW(), 0.001);
        assertEquals("Average should be recalculated", 300.0, result.getAverageInW(), 0.001);
    }

    @Test
    public void testSubtractNegativeValue() {
        // Test subtracting record with negative value (should increase sum)
        ActivePowerRecord negativeRecord = new ActivePowerRecord("test-sensor", 1500L, -50.0);
        AggregatedActivePowerRecord result = aggregator.substract(testIdentifier, negativeRecord, existingAggregated);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Count should be decremented", 1L, result.getCount());
        assertEquals("Sum should be increased", 350.0, result.getSumInW(), 0.001);
        assertEquals("Average should be recalculated", 350.0, result.getAverageInW(), 0.001);
    }

    @Test
    public void testSubtractLargerThanSum() {
        // Test subtracting value larger than current sum
        ActivePowerRecord largeRecord = new ActivePowerRecord("test-sensor", 1500L, 500.0);
        AggregatedActivePowerRecord result = aggregator.substract(testIdentifier, largeRecord, existingAggregated);
        
        assertNotNull("Result should not be null", result);
        assertEquals("Count should be decremented", 1L, result.getCount());
        assertEquals("Sum should be negative", -200.0, result.getSumInW(), 0.001);
        assertEquals("Average should be negative", -200.0, result.getAverageInW(), 0.001);
    }

    @Test
    public void testAddSubtractSequence() {
        // Test a sequence of add and subtract operations
        AggregatedActivePowerRecord step1 = aggregator.add(testIdentifier, testRecord, null);
        
        ActivePowerRecord record2 = new ActivePowerRecord("test-sensor", 1600L, 200.0);
        AggregatedActivePowerRecord step2 = aggregator.add(testIdentifier, record2, step1);
        
        // After adding two records: count=2, sum=300, average=150
        assertEquals("Step 2 count", 2L, step2.getCount());
        assertEquals("Step 2 sum", 300.0, step2.getSumInW(), 0.001);
        assertEquals("Step 2 average", 150.0, step2.getAverageInW(), 0.001);
        
        // Subtract the first record
        AggregatedActivePowerRecord step3 = aggregator.substract(testIdentifier, testRecord, step2);
        
        // After subtracting: count=1, sum=200, average=200
        assertEquals("Step 3 count", 1L, step3.getCount());
        assertEquals("Step 3 sum", 200.0, step3.getSumInW(), 0.001);
        assertEquals("Step 3 average", 200.0, step3.getAverageInW(), 0.001);
        assertEquals("Step 3 timestamp should be -1", -1L, step3.getTimestamp());
    }

    @Test
    public void testDifferentIdentifiers() {
        // Test with different windowed identifiers
        TimeWindow window2 = new TimeWindow(2000L, 3000L);
        Windowed<String> identifier2 = new Windowed<>("sensor-2", window2);
        
        AggregatedActivePowerRecord result = aggregator.add(identifier2, testRecord, null);
        
        assertEquals("Should use key from windowed identifier", "sensor-2", result.getIdentifier());
        assertEquals("Should use timestamp from record", 1500L, result.getTimestamp());
    }

    @Test
    public void testPrecisionWithSmallValues() {
        // Test precision with very small values
        ActivePowerRecord smallRecord = new ActivePowerRecord("test-sensor", 1500L, 0.001);
        AggregatedActivePowerRecord result = aggregator.add(testIdentifier, smallRecord, null);
        
        assertEquals("Small value should be preserved", 0.001, result.getSumInW(), 0.0001);
        assertEquals("Small average should be preserved", 0.001, result.getAverageInW(), 0.0001);
    }

    @Test
    public void testMultipleAdditions() {
        // Test multiple sequential additions
        AggregatedActivePowerRecord current = null;
        
        for (int i = 1; i <= 10; i++) {
            ActivePowerRecord record = new ActivePowerRecord("test-sensor", 1000L + i, i * 10.0);
            current = aggregator.add(testIdentifier, record, current);
        }
        
        // Sum should be 10 + 20 + 30 + ... + 100 = 550
        assertEquals("Final count should be 10", 10L, current.getCount());
        assertEquals("Final sum should be 550", 550.0, current.getSumInW(), 0.001);
        assertEquals("Final average should be 55", 55.0, current.getAverageInW(), 0.001);
    }
}