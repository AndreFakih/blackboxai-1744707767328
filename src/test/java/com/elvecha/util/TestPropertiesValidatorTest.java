package com.elvecha.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * Tests for TestPropertiesValidator
 */
public class TestPropertiesValidatorTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    private TestPropertiesValidator validator;
    private File propertiesFile;
    
    @Before
    public void setUp() throws IOException {
        validator = new TestPropertiesValidator();
        propertiesFile = new File("src/test/resources/test.properties");
    }
    
    @Test
    public void testValidConfiguration() throws IOException {
        // Valid configuration should pass validation
        assertTrue("Valid configuration should pass validation", validator.validate());
        assertTrue("No errors should be reported", validator.getErrors().isEmpty());
    }
    
    @Test
    public void testInvalidBooleanProperty() throws IOException {
        createTestProperties("test.parallel.enabled=invalid");
        
        assertFalse("Invalid boolean should fail validation", validator.validate());
        List<String> errors = validator.getErrors();
        assertTrue("Should report boolean error",
            errors.stream().anyMatch(e -> e.contains("must be true or false")));
    }
    
    @Test
    public void testInvalidIntegerProperty() throws IOException {
        createTestProperties("test.parallel.threads=invalid");
        
        assertFalse("Invalid integer should fail validation", validator.validate());
        List<String> errors = validator.getErrors();
        assertTrue("Should report integer error",
            errors.stream().anyMatch(e -> e.contains("must be a valid integer")));
    }
    
    @Test
    public void testInvalidPercentageProperty() throws IOException {
        createTestProperties("test.threshold.pass.rate=1.5");
        
        assertFalse("Invalid percentage should fail validation", validator.validate());
        List<String> errors = validator.getErrors();
        assertTrue("Should report percentage error",
            errors.stream().anyMatch(e -> e.contains("must be between 0.0 and 1.0")));
    }
    
    @Test
    public void testInvalidPortNumber() throws IOException {
        createTestProperties("test.debug.remote.port=70000");
        
        assertFalse("Invalid port should fail validation", validator.validate());
        List<String> errors = validator.getErrors();
        assertTrue("Should report port error",
            errors.stream().anyMatch(e -> e.contains("must be between 1024 and 65535")));
    }
    
    @Test
    public void testInvalidReportFormat() throws IOException {
        createTestProperties("test.report.format=invalid,html");
        
        assertFalse("Invalid format should fail validation", validator.validate());
        List<String> errors = validator.getErrors();
        assertTrue("Should report format error",
            errors.stream().anyMatch(e -> e.contains("Invalid report format")));
    }
    
    @Test
    public void testInvalidLogLevel() throws IOException {
        createTestProperties("test.log.level=INVALID");
        
        assertFalse("Invalid log level should fail validation", validator.validate());
        List<String> errors = validator.getErrors();
        assertTrue("Should report log level error",
            errors.stream().anyMatch(e -> e.contains("Invalid log level")));
    }
    
    @Test
    public void testInvalidDataSet() throws IOException {
        createTestProperties("test.data.set=invalid");
        
        assertFalse("Invalid data set should fail validation", validator.validate());
        List<String> errors = validator.getErrors();
        assertTrue("Should report data set error",
            errors.stream().anyMatch(e -> e.contains("Invalid data set")));
    }
    
    @Test
    public void testMissingRequiredProperty() throws IOException {
        Properties props = new Properties();
        // Don't set any properties
        writeProperties(props);
        
        assertTrue("Missing optional properties should pass validation", validator.validate());
        assertTrue("No errors should be reported", validator.getErrors().isEmpty());
    }
    
    @Test
    public void testMultipleErrors() throws IOException {
        createTestProperties(
            "test.parallel.enabled=invalid\n" +
            "test.parallel.threads=invalid\n" +
            "test.threshold.pass.rate=1.5"
        );
        
        assertFalse("Multiple errors should fail validation", validator.validate());
        List<String> errors = validator.getErrors();
        assertEquals("Should report all errors", 3, errors.size());
    }
    
    @Test
    public void testValidRanges() throws IOException {
        createTestProperties(
            "test.parallel.threads=4\n" +
            "test.threshold.pass.rate=0.9\n" +
            "test.debug.remote.port=5000"
        );
        
        assertTrue("Valid ranges should pass validation", validator.validate());
        assertTrue("No errors should be reported", validator.getErrors().isEmpty());
    }
    
    @Test
    public void testValidEnums() throws IOException {
        createTestProperties(
            "test.report.format=html,pdf,json\n" +
            "test.log.level=INFO\n" +
            "test.data.set=comprehensive"
        );
        
        assertTrue("Valid enums should pass validation", validator.validate());
        assertTrue("No errors should be reported", validator.getErrors().isEmpty());
    }
    
    @Test
    public void testEmptyProperties() throws IOException {
        writeProperties(new Properties());
        
        assertTrue("Empty properties should pass validation", validator.validate());
        assertTrue("No errors should be reported", validator.getErrors().isEmpty());
    }
    
    @Test
    public void testNonExistentFile() {
        propertiesFile.delete();
        
        assertFalse("Non-existent file should fail validation", validator.validate());
        List<String> errors = validator.getErrors();
        assertTrue("Should report file error",
            errors.stream().anyMatch(e -> e.contains("Property validation failed")));
    }
    
    @Test
    public void testCategoryValidation() throws IOException {
        createTestProperties(
            "test.category.model.enabled=true\n" +
            "test.category.model.parallel=true\n" +
            "test.category.invalid.enabled=true"
        );
        
        assertTrue("Valid category should pass validation", validator.validate());
        assertTrue("No errors should be reported", validator.getErrors().isEmpty());
    }
    
    @Test
    public void testThresholdValidation() throws IOException {
        createTestProperties(
            "test.threshold.pass.rate=0.9\n" +
            "test.threshold.skip.rate=0.1\n" +
            "test.threshold.duration=5000\n" +
            "test.threshold.coverage=0.8\n" +
            "test.threshold.memory=512"
        );
        
        assertTrue("Valid thresholds should pass validation", validator.validate());
        assertTrue("No errors should be reported", validator.getErrors().isEmpty());
    }
    
    private void createTestProperties(String content) throws IOException {
        Properties props = new Properties();
        for (String line : content.split("\n")) {
            String[] parts = line.split("=");
            if (parts.length == 2) {
                props.setProperty(parts[0].trim(), parts[1].trim());
            }
        }
        writeProperties(props);
    }
    
    private void writeProperties(Properties props) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(propertiesFile)) {
            props.store(fos, "Test Properties");
        }
    }
}
