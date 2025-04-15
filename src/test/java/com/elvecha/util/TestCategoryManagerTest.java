package com.elvecha.util;

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Map;

public class TestCategoryManagerTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    private Properties testConfig;
    private File configFile;
    
    @Before
    public void setUp() throws IOException {
        testConfig = new Properties();
        setupTestConfiguration();
        
        // Create test configuration file
        configFile = new File(tempFolder.getRoot(), "test-categories.properties");
        saveConfiguration();
    }
    
    private void setupTestConfiguration() {
        // Model Tests
        testConfig.setProperty("model.tests.enabled", "true");
        testConfig.setProperty("model.tests.timeout", "5000");
        testConfig.setProperty("model.tests.parallel", "false");
        testConfig.setProperty("model.tests.classes", 
            "com.elvecha.model.CriteriaTest,com.elvecha.model.AlternativeTest");
        testConfig.setProperty("model.tests.dependencies", "");
        
        // Utility Tests
        testConfig.setProperty("util.tests.enabled", "true");
        testConfig.setProperty("util.tests.timeout", "10000");
        testConfig.setProperty("util.tests.parallel", "true");
        testConfig.setProperty("util.tests.classes",
            "com.elvecha.util.SAWCalculatorTest");
        testConfig.setProperty("util.tests.dependencies", "model.tests");
        
        // Integration Tests
        testConfig.setProperty("integration.tests.enabled", "true");
        testConfig.setProperty("integration.tests.timeout", "30000");
        testConfig.setProperty("integration.tests.parallel", "false");
        testConfig.setProperty("integration.tests.classes",
            "com.elvecha.integration.SystemIntegrationTest");
        testConfig.setProperty("integration.tests.dependencies", 
            "model.tests,util.tests");
    }
    
    private void saveConfiguration() throws IOException {
        try (FileWriter writer = new FileWriter(configFile)) {
            testConfig.store(writer, "Test Categories Configuration");
        }
    }
    
    @Test
    public void testLoadValidConfiguration() {
        TestCategoryManager manager = new TestCategoryManager();
        List<TestCategoryManager.TestCategory> categories = manager.getOrderedCategories();
        
        assertNotNull("Categories should not be null", categories);
        assertFalse("Categories should not be empty", categories.isEmpty());
    }
    
    @Test
    public void testCategoryOrder() {
        TestCategoryManager manager = new TestCategoryManager();
        List<TestCategoryManager.TestCategory> ordered = manager.getOrderedCategories();
        
        // Verify order respects dependencies
        boolean modelBeforeUtil = false;
        boolean utilBeforeIntegration = false;
        
        for (int i = 0; i < ordered.size(); i++) {
            String name = ordered.get(i).getName();
            if (name.equals("model")) {
                modelBeforeUtil = true;
            } else if (name.equals("util") && modelBeforeUtil) {
                utilBeforeIntegration = true;
            } else if (name.equals("integration")) {
                assertTrue("Util tests should come before integration tests",
                    utilBeforeIntegration);
            }
        }
    }
    
    @Test(expected = RuntimeException.class)
    public void testCyclicDependencies() throws IOException {
        // Create cyclic dependency
        testConfig.setProperty("model.tests.dependencies", "util.tests");
        saveConfiguration();
        
        new TestCategoryManager(); // Should throw exception
    }
    
    @Test
    public void testCategorySettings() {
        TestCategoryManager manager = new TestCategoryManager();
        
        assertTrue("Model tests should be enabled",
            manager.isCategoryEnabled("model"));
        assertEquals("Model tests should have correct timeout",
            5000, manager.getCategoryTimeout("model"));
        assertFalse("Model tests should not support parallel execution",
            manager.isParallelExecutionSupported("model"));
    }
    
    @Test
    public void testGetTestClasses() {
        TestCategoryManager manager = new TestCategoryManager();
        Set<Class<?>> modelClasses = manager.getTestClasses("model");
        
        assertNotNull("Test classes should not be null", modelClasses);
        assertEquals("Model category should have correct number of test classes",
            2, modelClasses.size());
    }
    
    @Test
    public void testDisabledCategory() throws IOException {
        testConfig.setProperty("util.tests.enabled", "false");
        saveConfiguration();
        
        TestCategoryManager manager = new TestCategoryManager();
        assertFalse("Util tests should be disabled",
            manager.isCategoryEnabled("util"));
    }
    
    @Test
    public void testCategorySettings() {
        TestCategoryManager manager = new TestCategoryManager();
        Map<String, String> settings = manager.getCategorySettings("model");
        
        assertNotNull("Settings should not be null", settings);
        assertEquals("Should have correct timeout setting",
            "5000", settings.get("tests.timeout"));
    }
    
    @Test
    public void testMissingCategory() {
        TestCategoryManager manager = new TestCategoryManager();
        
        assertFalse("Non-existent category should be disabled",
            manager.isCategoryEnabled("nonexistent"));
        assertEquals("Non-existent category should have default timeout",
            5000, manager.getCategoryTimeout("nonexistent"));
        assertTrue("Non-existent category should have empty test classes",
            manager.getTestClasses("nonexistent").isEmpty());
    }
    
    @Test
    public void testParallelExecution() {
        TestCategoryManager manager = new TestCategoryManager();
        
        assertTrue("Util tests should support parallel execution",
            manager.isParallelExecutionSupported("util"));
        assertFalse("Integration tests should not support parallel execution",
            manager.isParallelExecutionSupported("integration"));
    }
    
    @Test
    public void testDependencyValidation() throws IOException {
        // Add invalid dependency
        testConfig.setProperty("util.tests.dependencies", "nonexistent");
        saveConfiguration();
        
        try {
            new TestCategoryManager();
            fail("Should throw exception for invalid dependency");
        } catch (RuntimeException e) {
            assertTrue("Should mention missing dependency",
                e.getMessage().contains("Missing dependency"));
        }
    }
    
    @Test
    public void testEmptyConfiguration() throws IOException {
        // Clear all properties
        testConfig.clear();
        saveConfiguration();
        
        TestCategoryManager manager = new TestCategoryManager();
        List<TestCategoryManager.TestCategory> categories = manager.getOrderedCategories();
        
        assertTrue("Empty configuration should result in empty categories",
            categories.isEmpty());
    }
    
    @Test
    public void testInvalidTestClass() throws IOException {
        testConfig.setProperty("model.tests.classes", "com.nonexistent.TestClass");
        saveConfiguration();
        
        try {
            new TestCategoryManager();
            fail("Should throw exception for invalid test class");
        } catch (RuntimeException e) {
            assertTrue("Should mention class not found",
                e.getMessage().contains("Test class not found"));
        }
    }
}
