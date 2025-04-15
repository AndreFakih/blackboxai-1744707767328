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
import java.util.Properties;

public class TestConfigValidatorTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    private TestConfigValidator validator;
    private File propertiesFile;
    private Properties testProperties;
    
    @Before
    public void setUp() throws IOException {
        validator = new TestConfigValidator();
        
        // Create test directories
        for (String dir : new String[]{"test-temp", "test-logs", "test-reports", "test-data"}) {
            new File(dir).mkdirs();
        }
        
        // Create test properties
        testProperties = new Properties();
        setupValidProperties();
        
        // Create properties file
        propertiesFile = new File("test.properties");
        saveProperties();
    }
    
    @After
    public void tearDown() {
        // Clean up test directories
        for (String dir : new String[]{"test-temp", "test-logs", "test-reports", "test-data"}) {
            TestUtils.cleanupTestFiles(dir);
        }
        
        // Clean up properties file
        if (propertiesFile.exists()) {
            propertiesFile.delete();
        }
    }
    
    private void setupValidProperties() {
        testProperties.setProperty("test.timeout.default", "5000");
        testProperties.setProperty("test.parallel.threads", 
            String.valueOf(Runtime.getRuntime().availableProcessors()));
        testProperties.setProperty("performance.large.dataset.size", "1000");
        testProperties.setProperty("performance.calculation.timeout", "1000");
        testProperties.setProperty("performance.pdf.timeout", "5000");
        testProperties.setProperty("memory.max.usage.mb", "100");
        testProperties.setProperty("file.cleanup.days", "5");
        testProperties.setProperty("report.output.dir", "test-reports");
    }
    
    private void saveProperties() throws IOException {
        try (FileWriter writer = new FileWriter(propertiesFile)) {
            testProperties.store(writer, "Test Properties");
        }
    }
    
    @Test
    public void testValidConfiguration() throws IOException {
        assertTrue("Valid configuration should pass validation",
            validator.validateEnvironment());
        assertEquals("Should have no validation errors",
            "", validator.getValidationErrors().trim());
    }
    
    @Test
    public void testMissingProperty() throws IOException {
        testProperties.remove("test.timeout.default");
        saveProperties();
        
        assertFalse("Missing property should fail validation",
            validator.validateEnvironment());
        assertTrue("Should report missing property",
            validator.getValidationErrors().contains("Missing required property"));
    }
    
    @Test
    public void testInvalidPropertyValue() throws IOException {
        testProperties.setProperty("test.timeout.default", "-1");
        saveProperties();
        
        assertFalse("Invalid property value should fail validation",
            validator.validateEnvironment());
        assertTrue("Should report invalid value",
            validator.getValidationErrors().contains("Invalid value"));
    }
    
    @Test
    public void testMissingDirectory() {
        // Delete a required directory
        TestUtils.cleanupTestFiles("test-reports");
        
        // Make directory read-only to prevent creation
        File reportsDir = new File("test-reports");
        reportsDir.getParentFile().setWritable(false);
        
        try {
            assertFalse("Missing directory should fail validation",
                validator.validateEnvironment());
            assertTrue("Should report directory creation failure",
                validator.getValidationErrors().contains("Failed to create directory"));
        } finally {
            // Restore permissions
            reportsDir.getParentFile().setWritable(true);
        }
    }
    
    @Test
    public void testInsufficientMemory() throws IOException {
        // Set required memory higher than available
        long maxMemory = Runtime.getRuntime().maxMemory() / (1024 * 1024);
        testProperties.setProperty("memory.max.usage.mb", 
            String.valueOf(maxMemory + 1000));
        saveProperties();
        
        assertFalse("Insufficient memory should fail validation",
            validator.validateEnvironment());
        assertTrue("Should report insufficient memory",
            validator.getValidationErrors().contains("Insufficient memory"));
    }
    
    @Test
    public void testTooManyThreads() throws IOException {
        // Set thread count higher than available processors
        int processors = Runtime.getRuntime().availableProcessors();
        testProperties.setProperty("test.parallel.threads",
            String.valueOf(processors + 1));
        saveProperties();
        
        assertFalse("Too many threads should fail validation",
            validator.validateEnvironment());
        assertTrue("Should report insufficient processors",
            validator.getValidationErrors().contains("Insufficient processors"));
    }
    
    @Test
    public void testInvalidCleanupDays() throws IOException {
        testProperties.setProperty("file.cleanup.days", "-1");
        saveProperties();
        
        assertFalse("Invalid cleanup days should fail validation",
            validator.validateEnvironment());
        assertTrue("Should report invalid cleanup days",
            validator.getValidationErrors().contains("Invalid value"));
    }
    
    @Test
    public void testMissingPropertiesFile() {
        // Delete properties file
        propertiesFile.delete();
        
        assertFalse("Missing properties file should fail validation",
            validator.validateEnvironment());
        assertTrue("Should report missing properties file",
            validator.getValidationErrors().contains("test.properties not found"));
    }
    
    @Test
    public void testDirectoryPermissions() throws IOException {
        File testDir = new File("test-temp");
        testDir.setReadable(false);
        
        try {
            assertFalse("Invalid permissions should fail validation",
                validator.validateEnvironment());
            assertTrue("Should report insufficient permissions",
                validator.getValidationErrors().contains("Insufficient permissions"));
        } finally {
            testDir.setReadable(true);
        }
    }
    
    @Test
    public void testMultipleValidationErrors() throws IOException {
        // Introduce multiple issues
        testProperties.remove("test.timeout.default");
        testProperties.setProperty("memory.max.usage.mb", "-1");
        testProperties.setProperty("test.parallel.threads", "0");
        saveProperties();
        
        assertFalse("Multiple issues should fail validation",
            validator.validateEnvironment());
            
        String errors = validator.getValidationErrors();
        assertTrue("Should report missing property",
            errors.contains("Missing required property"));
        assertTrue("Should report invalid memory",
            errors.contains("Invalid value"));
        assertTrue("Should report invalid threads",
            errors.contains("Invalid value"));
    }
    
    @Test
    public void testValidationWithEmptyValues() throws IOException {
        testProperties.setProperty("test.timeout.default", "");
        testProperties.setProperty("performance.calculation.timeout", " ");
        saveProperties();
        
        assertFalse("Empty values should fail validation",
            validator.validateEnvironment());
        assertTrue("Should report missing values",
            validator.getValidationErrors().contains("Missing required property"));
    }
}
