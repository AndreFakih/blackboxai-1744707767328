package com.elvecha.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * Validates test properties configuration
 */
public class TestPropertiesValidator {
    private final Properties properties;
    private final List<String> errors;
    private final TestLogger logger;
    
    public TestPropertiesValidator() {
        this.properties = new Properties();
        this.errors = new ArrayList<>();
        this.logger = new TestLogger();
    }
    
    /**
     * Loads and validates test properties
     */
    public boolean validate() {
        try {
            // Load properties
            loadProperties();
            
            // Validate all property sections
            validateExecutionProperties();
            validateCategoryProperties();
            validateEnvironmentProperties();
            validateReportingProperties();
            validateThresholdProperties();
            validateDataProperties();
            validateLoggingProperties();
            validateDebugProperties();
            validateMonitoringProperties();
            validateDependencyProperties();
            validateSecurityProperties();
            validateNotificationProperties();
            validatePerformanceProperties();
            validateCoverageProperties();
            
            return errors.isEmpty();
            
        } catch (Exception e) {
            logger.logError("Failed to validate properties", e);
            errors.add("Property validation failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets validation errors if any
     */
    public List<String> getErrors() {
        return new ArrayList<>(errors);
    }
    
    private void loadProperties() throws IOException {
        try (FileInputStream fis = new FileInputStream("src/test/resources/test.properties")) {
            properties.load(fis);
        }
    }
    
    private void validateExecutionProperties() {
        validateBoolean("test.parallel.enabled", true);
        validatePositiveInt("test.parallel.threads", 1);
        validatePositiveInt("test.timeout.global", 1000);
        validatePositiveInt("test.timeout.test", 1000);
        validateNonNegativeInt("test.retry.count", 0);
        validatePositiveInt("test.retry.delay", 100);
    }
    
    private void validateCategoryProperties() {
        String[] categories = {"model", "util", "ui", "framework", "integration", "performance"};
        for (String category : categories) {
            validateBoolean("test.category." + category + ".enabled", true);
            validateBoolean("test.category." + category + ".parallel", false);
        }
    }
    
    private void validateEnvironmentProperties() {
        validateBoolean("test.env.cleanup.enabled", true);
        validatePositiveInt("test.env.cleanup.days", 1);
        validateDirectory("test.env.temp.dir");
        validateDirectory("test.env.logs.dir");
        validateDirectory("test.env.reports.dir");
        validateDirectory("test.env.data.dir");
    }
    
    private void validateReportingProperties() {
        validateReportFormats("test.report.format");
        validateDetailLevel("test.report.detail.level");
        validateBoolean("test.report.screenshots", true);
        validateBoolean("test.report.console.output", true);
        validateBoolean("test.report.stacktrace", true);
        validateBoolean("test.report.metrics", true);
        validateBoolean("test.report.environment", true);
        validateBoolean("test.report.categories", true);
    }
    
    private void validateThresholdProperties() {
        validatePercentage("test.threshold.pass.rate");
        validatePercentage("test.threshold.skip.rate");
        validatePositiveInt("test.threshold.duration", 100);
        validatePercentage("test.threshold.coverage");
        validatePositiveInt("test.threshold.memory", 64);
    }
    
    private void validateDataProperties() {
        validateDataSet("test.data.set");
        validatePositiveInt("test.data.seed", 1);
        validatePositiveInt("test.data.size.small", 1);
        validatePositiveInt("test.data.size.medium", 10);
        validatePositiveInt("test.data.size.large", 100);
    }
    
    private void validateLoggingProperties() {
        validateLogLevel("test.log.level");
        validateBoolean("test.log.console", true);
        validateBoolean("test.log.file", true);
        validateLogFormat("test.log.format");
        validateBoolean("test.log.timestamp", true);
        validateBoolean("test.log.memory", true);
        validateBoolean("test.log.performance", true);
    }
    
    private void validateDebugProperties() {
        validateBoolean("test.debug.enabled", false);
        validateBoolean("test.debug.breakpoints", false);
        validateBoolean("test.debug.console", true);
        validatePort("test.debug.remote.port");
        validateBoolean("test.debug.suspend", false);
    }
    
    private void validateMonitoringProperties() {
        validateBoolean("test.monitor.memory", true);
        validateBoolean("test.monitor.cpu", true);
        validateBoolean("test.monitor.threads", true);
        validatePositiveInt("test.monitor.interval", 100);
        validatePercentage("test.monitor.threshold.memory");
        validatePercentage("test.monitor.threshold.cpu");
    }
    
    private void validateDependencyProperties() {
        validateBoolean("test.deps.validate", true);
        validateBoolean("test.deps.resolve", true);
        validateBoolean("test.deps.cleanup", true);
        validateBoolean("test.deps.cache", true);
        validatePositiveInt("test.deps.timeout", 1000);
    }
    
    private void validateSecurityProperties() {
        validateBoolean("test.security.enabled", true);
        validateBoolean("test.security.auth", true);
        validateBoolean("test.security.encryption", true);
        validateBoolean("test.security.ssl", true);
        validatePositiveInt("test.security.timeout", 1000);
    }
    
    private void validateNotificationProperties() {
        validateBoolean("test.notify.enabled", true);
        validateBoolean("test.notify.success", true);
        validateBoolean("test.notify.failure", true);
        validatePercentage("test.notify.threshold");
        validateBoolean("test.notify.email", false);
        validateBoolean("test.notify.slack", false);
    }
    
    private void validatePerformanceProperties() {
        validateBoolean("test.perf.baseline.enabled", true);
        validatePercentage("test.perf.baseline.tolerance");
        validatePositiveInt("test.perf.baseline.samples", 1);
        validateBoolean("test.perf.baseline.update", false);
        validateBoolean("test.perf.profile", true);
        validateBoolean("test.perf.trace", true);
    }
    
    private void validateCoverageProperties() {
        validateBoolean("test.coverage.enabled", true);
        validateBoolean("test.coverage.classes", true);
        validateBoolean("test.coverage.methods", true);
        validateBoolean("test.coverage.lines", true);
        validateBoolean("test.coverage.branches", true);
        validatePercentage("test.coverage.threshold");
    }
    
    private void validateBoolean(String property, boolean defaultValue) {
        String value = properties.getProperty(property);
        if (value != null && !value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
            errors.add(String.format("Property '%s' must be true or false", property));
        }
    }
    
    private void validatePositiveInt(String property, int minValue) {
        try {
            String value = properties.getProperty(property);
            if (value != null) {
                int intValue = Integer.parseInt(value);
                if (intValue < minValue) {
                    errors.add(String.format("Property '%s' must be >= %d", property, minValue));
                }
            }
        } catch (NumberFormatException e) {
            errors.add(String.format("Property '%s' must be a valid integer", property));
        }
    }
    
    private void validateNonNegativeInt(String property, int minValue) {
        try {
            String value = properties.getProperty(property);
            if (value != null) {
                int intValue = Integer.parseInt(value);
                if (intValue < minValue) {
                    errors.add(String.format("Property '%s' must be >= %d", property, minValue));
                }
            }
        } catch (NumberFormatException e) {
            errors.add(String.format("Property '%s' must be a valid integer", property));
        }
    }
    
    private void validatePercentage(String property) {
        try {
            String value = properties.getProperty(property);
            if (value != null) {
                double doubleValue = Double.parseDouble(value);
                if (doubleValue < 0.0 || doubleValue > 1.0) {
                    errors.add(String.format("Property '%s' must be between 0.0 and 1.0", property));
                }
            }
        } catch (NumberFormatException e) {
            errors.add(String.format("Property '%s' must be a valid decimal", property));
        }
    }
    
    private void validateDirectory(String property) {
        String value = properties.getProperty(property);
        if (value != null && value.trim().isEmpty()) {
            errors.add(String.format("Property '%s' must not be empty", property));
        }
    }
    
    private void validatePort(String property) {
        try {
            String value = properties.getProperty(property);
            if (value != null) {
                int port = Integer.parseInt(value);
                if (port < 1024 || port > 65535) {
                    errors.add(String.format("Property '%s' must be between 1024 and 65535", property));
                }
            }
        } catch (NumberFormatException e) {
            errors.add(String.format("Property '%s' must be a valid port number", property));
        }
    }
    
    private void validateReportFormats(String property) {
        String value = properties.getProperty(property);
        if (value != null) {
            Set<String> validFormats = new HashSet<>(Arrays.asList("html", "pdf", "json", "xml"));
            String[] formats = value.split(",");
            for (String format : formats) {
                if (!validFormats.contains(format.trim().toLowerCase())) {
                    errors.add(String.format("Invalid report format '%s' in property '%s'", format, property));
                }
            }
        }
    }
    
    private void validateDetailLevel(String property) {
        String value = properties.getProperty(property);
        if (value != null) {
            Set<String> validLevels = new HashSet<>(Arrays.asList("minimal", "normal", "full", "detailed"));
            if (!validLevels.contains(value.trim().toLowerCase())) {
                errors.add(String.format("Invalid detail level '%s' in property '%s'", value, property));
            }
        }
    }
    
    private void validateLogLevel(String property) {
        String value = properties.getProperty(property);
        if (value != null) {
            Set<String> validLevels = new HashSet<>(Arrays.asList("ERROR", "WARN", "INFO", "DEBUG", "TRACE"));
            if (!validLevels.contains(value.trim().toUpperCase())) {
                errors.add(String.format("Invalid log level '%s' in property '%s'", value, property));
            }
        }
    }
    
    private void validateLogFormat(String property) {
        String value = properties.getProperty(property);
        if (value != null) {
            Set<String> validFormats = new HashSet<>(Arrays.asList("simple", "detailed", "json"));
            if (!validFormats.contains(value.trim().toLowerCase())) {
                errors.add(String.format("Invalid log format '%s' in property '%s'", value, property));
            }
        }
    }
    
    private void validateDataSet(String property) {
        String value = properties.getProperty(property);
        if (value != null) {
            Set<String> validSets = new HashSet<>(Arrays.asList(
                "minimal", "typical", "comprehensive", "edge-cases", "stress"));
            if (!validSets.contains(value.trim().toLowerCase())) {
                errors.add(String.format("Invalid data set '%s' in property '%s'", value, property));
            }
        }
    }
}
