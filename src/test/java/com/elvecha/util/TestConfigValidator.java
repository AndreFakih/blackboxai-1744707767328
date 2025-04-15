package com.elvecha.util;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;

/**
 * Validates test configuration and environment setup
 * Ensures all required properties and resources are available
 */
public class TestConfigValidator {
    private static final Set<String> REQUIRED_PROPERTIES = new HashSet<>(Arrays.asList(
        "test.timeout.default",
        "test.parallel.threads",
        "performance.large.dataset.size",
        "performance.calculation.timeout",
        "performance.pdf.timeout",
        "memory.max.usage.mb",
        "file.cleanup.days",
        "report.output.dir"
    ));
    
    private static final Set<String> REQUIRED_DIRECTORIES = new HashSet<>(Arrays.asList(
        "test-temp",
        "test-logs",
        "test-reports",
        "test-data"
    ));
    
    private static final Map<String, ValueValidator> PROPERTY_VALIDATORS = new HashMap<>();
    
    static {
        // Initialize property validators
        PROPERTY_VALIDATORS.put("test.timeout.default", 
            value -> Integer.parseInt(value) > 0);
        PROPERTY_VALIDATORS.put("test.parallel.threads",
            value -> Integer.parseInt(value) > 0 && 
                     Integer.parseInt(value) <= Runtime.getRuntime().availableProcessors());
        PROPERTY_VALIDATORS.put("performance.large.dataset.size",
            value -> Integer.parseInt(value) > 0);
        PROPERTY_VALIDATORS.put("performance.calculation.timeout",
            value -> Integer.parseInt(value) > 0);
        PROPERTY_VALIDATORS.put("performance.pdf.timeout",
            value -> Integer.parseInt(value) > 0);
        PROPERTY_VALIDATORS.put("memory.max.usage.mb",
            value -> Integer.parseInt(value) > 0 && 
                     Integer.parseInt(value) <= Runtime.getRuntime().maxMemory()/(1024*1024));
        PROPERTY_VALIDATORS.put("file.cleanup.days",
            value -> Integer.parseInt(value) >= 0);
    }
    
    private final Properties config;
    private final TestLogger logger;
    private final StringBuilder validationErrors;
    
    public TestConfigValidator() {
        this.config = new Properties();
        this.logger = new TestLogger();
        this.validationErrors = new StringBuilder();
    }
    
    /**
     * Validates the complete test environment
     * @return true if validation passes, false otherwise
     */
    public boolean validateEnvironment() {
        boolean isValid = true;
        
        try {
            // Load and validate properties
            isValid &= loadAndValidateProperties();
            
            // Validate directories
            isValid &= validateDirectories();
            
            // Validate system resources
            isValid &= validateSystemResources();
            
            // Log validation results
            logValidationResults(isValid);
            
        } catch (Exception e) {
            handleValidationError("Validation failed", e);
            isValid = false;
        }
        
        return isValid;
    }
    
    /**
     * Gets validation error messages
     */
    public String getValidationErrors() {
        return validationErrors.toString();
    }
    
    private boolean loadAndValidateProperties() {
        boolean isValid = true;
        
        try {
            // Load properties
            try (InputStream in = getClass().getResourceAsStream("/test.properties")) {
                if (in == null) {
                    addValidationError("test.properties not found");
                    return false;
                }
                config.load(in);
            }
            
            // Check required properties
            for (String required : REQUIRED_PROPERTIES) {
                String value = config.getProperty(required);
                if (value == null || value.trim().isEmpty()) {
                    addValidationError("Missing required property: " + required);
                    isValid = false;
                    continue;
                }
                
                // Validate property value
                ValueValidator validator = PROPERTY_VALIDATORS.get(required);
                if (validator != null) {
                    try {
                        if (!validator.isValid(value.trim())) {
                            addValidationError("Invalid value for " + required + ": " + value);
                            isValid = false;
                        }
                    } catch (Exception e) {
                        addValidationError("Error validating " + required + ": " + e.getMessage());
                        isValid = false;
                    }
                }
            }
            
        } catch (Exception e) {
            handleValidationError("Failed to load properties", e);
            isValid = false;
        }
        
        return isValid;
    }
    
    private boolean validateDirectories() {
        boolean isValid = true;
        
        for (String dirName : REQUIRED_DIRECTORIES) {
            File dir = new File(dirName);
            
            // Check directory exists or can be created
            if (!dir.exists()) {
                if (!dir.mkdirs()) {
                    addValidationError("Failed to create directory: " + dirName);
                    isValid = false;
                    continue;
                }
            }
            
            // Check directory permissions
            if (!dir.canRead() || !dir.canWrite()) {
                addValidationError("Insufficient permissions for directory: " + dirName);
                isValid = false;
            }
        }
        
        return isValid;
    }
    
    private boolean validateSystemResources() {
        boolean isValid = true;
        
        // Validate memory
        long maxMemory = Runtime.getRuntime().maxMemory() / (1024 * 1024);
        int requiredMemory = Integer.parseInt(
            config.getProperty("memory.max.usage.mb", "100"));
        
        if (maxMemory < requiredMemory) {
            addValidationError(String.format(
                "Insufficient memory: %dMB available, %dMB required",
                maxMemory, requiredMemory));
            isValid = false;
        }
        
        // Validate processors
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        int requiredThreads = Integer.parseInt(
            config.getProperty("test.parallel.threads", "4"));
        
        if (availableProcessors < requiredThreads) {
            addValidationError(String.format(
                "Insufficient processors: %d available, %d required",
                availableProcessors, requiredThreads));
            isValid = false;
        }
        
        return isValid;
    }
    
    private void addValidationError(String error) {
        validationErrors.append("- ").append(error).append("\n");
    }
    
    private void handleValidationError(String message, Exception e) {
        String error = message + ": " + e.getMessage();
        addValidationError(error);
        logger.logError(error, e);
    }
    
    private void logValidationResults(boolean isValid) {
        if (isValid) {
            logger.log("Environment validation passed");
        } else {
            logger.log("Environment validation failed:\n" + getValidationErrors());
        }
    }
    
    @FunctionalInterface
    private interface ValueValidator {
        boolean isValid(String value);
    }
}
