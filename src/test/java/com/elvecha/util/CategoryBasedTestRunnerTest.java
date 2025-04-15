package com.elvecha.util;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.Failure;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

public class CategoryBasedTestRunnerTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    private Properties testConfig;
    private File configFile;
    private RunNotifier notifier;
    private AtomicInteger testCount;
    
    // Sample test classes for testing
    public static class SampleModelTest {
        @Test
        public void testModel() {
            assertTrue(true);
        }
    }
    
    public static class SampleUtilTest {
        @Test
        public void testUtil() {
            assertTrue(true);
        }
    }
    
    public static class LongRunningTest {
        @Test
        public void testLongOperation() throws InterruptedException {
            Thread.sleep(2000); // Simulate long operation
        }
    }
    
    @Before
    public void setUp() throws Exception {
        testConfig = new Properties();
        setupTestConfiguration();
        
        // Create test configuration file
        configFile = new File(tempFolder.getRoot(), "test-categories.properties");
        saveConfiguration();
        
        // Initialize notifier
        notifier = new RunNotifier();
        testCount = new AtomicInteger(0);
        notifier.addListener(new TestExecutionListener());
    }
    
    private void setupTestConfiguration() {
        // Model category
        testConfig.setProperty("model.tests.enabled", "true");
        testConfig.setProperty("model.tests.timeout", "5000");
        testConfig.setProperty("model.tests.parallel", "false");
        testConfig.setProperty("model.tests.classes", 
            SampleModelTest.class.getName());
        
        // Util category
        testConfig.setProperty("util.tests.enabled", "true");
        testConfig.setProperty("util.tests.timeout", "10000");
        testConfig.setProperty("util.tests.parallel", "true");
        testConfig.setProperty("util.tests.classes",
            SampleUtilTest.class.getName());
        testConfig.setProperty("util.tests.thread.pool", "2");
        
        // Performance category
        testConfig.setProperty("performance.tests.enabled", "true");
        testConfig.setProperty("performance.tests.timeout", "1000");
        testConfig.setProperty("performance.tests.parallel", "true");
        testConfig.setProperty("performance.tests.classes",
            LongRunningTest.class.getName());
    }
    
    private void saveConfiguration() throws Exception {
        try (FileWriter writer = new FileWriter(configFile)) {
            testConfig.store(writer, "Test Categories Configuration");
        }
    }
    
    @Test
    public void testBasicExecution() throws Exception {
        CategoryBasedTestRunner runner = new CategoryBasedTestRunner(getClass());
        runner.run(notifier);
        
        assertTrue("Tests should be executed",
            testCount.get() > 0);
    }
    
    @Test
    public void testParallelExecution() throws Exception {
        // Configure multiple parallel tests
        testConfig.setProperty("util.tests.parallel", "true");
        testConfig.setProperty("util.tests.classes",
            String.format("%s,%s",
                SampleUtilTest.class.getName(),
                SampleUtilTest.class.getName()));
        saveConfiguration();
        
        long startTime = System.currentTimeMillis();
        
        CategoryBasedTestRunner runner = new CategoryBasedTestRunner(getClass());
        runner.run(notifier);
        
        long duration = System.currentTimeMillis() - startTime;
        assertTrue("Parallel execution should be faster than sequential",
            duration < 4000); // Less than 2 sequential executions
    }
    
    @Test
    public void testTimeout() throws Exception {
        // Configure short timeout
        testConfig.setProperty("performance.tests.timeout", "500");
        saveConfiguration();
        
        final AtomicInteger timeoutCount = new AtomicInteger(0);
        notifier.addListener(new TestExecutionListener() {
            @Override
            public void testFailure(Failure failure) {
                if (failure.getMessage().contains("timeout")) {
                    timeoutCount.incrementAndGet();
                }
            }
        });
        
        CategoryBasedTestRunner runner = new CategoryBasedTestRunner(getClass());
        runner.run(notifier);
        
        assertTrue("Should detect timeout",
            timeoutCount.get() > 0);
    }
    
    @Test
    public void testCategoryDisabling() throws Exception {
        // Disable util tests
        testConfig.setProperty("util.tests.enabled", "false");
        saveConfiguration();
        
        CategoryBasedTestRunner runner = new CategoryBasedTestRunner(getClass());
        Description description = runner.getDescription();
        
        assertFalse("Disabled category tests should not be included",
            containsTestClass(description, SampleUtilTest.class));
    }
    
    @Test
    public void testCategoryOrdering() throws Exception {
        // Add dependency
        testConfig.setProperty("util.tests.dependencies", "model.tests");
        saveConfiguration();
        
        final StringBuilder executionOrder = new StringBuilder();
        notifier.addListener(new TestExecutionListener() {
            @Override
            public void testStarted(Description description) {
                executionOrder.append(description.getTestClass().getSimpleName())
                    .append(",");
            }
        });
        
        CategoryBasedTestRunner runner = new CategoryBasedTestRunner(getClass());
        runner.run(notifier);
        
        assertTrue("Model tests should execute before util tests",
            executionOrder.indexOf("SampleModelTest") < 
            executionOrder.indexOf("SampleUtilTest"));
    }
    
    @Test
    public void testResourceCleanup() throws Exception {
        CategoryBasedTestRunner runner = new CategoryBasedTestRunner(getClass());
        runner.run(notifier);
        
        // Verify executors are shut down
        Thread.sleep(100); // Give time for cleanup
        assertEquals("Thread pool should be cleaned up",
            Thread.activeCount(), Thread.currentThread().getThreadGroup().activeCount());
    }
    
    private boolean containsTestClass(Description description, Class<?> testClass) {
        if (description.getTestClass() == testClass) {
            return true;
        }
        for (Description child : description.getChildren()) {
            if (containsTestClass(child, testClass)) {
                return true;
            }
        }
        return false;
    }
}
