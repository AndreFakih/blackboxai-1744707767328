package com.elvecha.util;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.Properties;
import java.io.InputStream;

/**
 * JUnit Rule that automatically generates test reports
 * Usage: @Rule public TestReportRule reportRule = new TestReportRule();
 */
public class TestReportRule implements TestRule {
    private static final TestReportGenerator generator;
    private static final TestLogger logger;
    private static Properties config;
    
    static {
        try {
            // Load test configuration
            config = new Properties();
            try (InputStream in = TestReportRule.class
                .getResourceAsStream("/test.properties")) {
                if (in != null) {
                    config.load(in);
                }
            }
            
            // Initialize generator and logger
            generator = new TestReportGenerator(
                config.getProperty("report.title", "Test Execution Report"),
                config.getProperty("report.output.dir", "test-reports")
            );
            
            logger = new TestLogger();
            
            // Register shutdown hook to generate report
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    generator.generateReport();
                    logger.log("Test report generated successfully");
                } catch (Exception e) {
                    logger.logError("Failed to generate test report", e);
                }
            }));
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize TestReportRule", e);
        }
    }
    
    @Override
    public Statement apply(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                String category = description.getTestClass().getSimpleName();
                long startTime = System.nanoTime();
                Throwable failure = null;
                
                logger.log(String.format("Starting test: %s.%s",
                    category, description.getMethodName()));
                
                try {
                    base.evaluate();
                } catch (Throwable t) {
                    failure = t;
                    throw t;
                } finally {
                    long duration = System.nanoTime() - startTime;
                    
                    // Record test result
                    generator.recordTestResult(
                        category,
                        description,
                        failure == null,
                        duration,
                        failure != null ? new org.junit.runner.notification.Failure(
                            description, failure) : null
                    );
                    
                    // Log test completion
                    String result = failure == null ? "PASSED" : "FAILED";
                    logger.log(String.format("Test %s.%s %s (%.2f ms)",
                        category,
                        description.getMethodName(),
                        result,
                        duration / 1_000_000.0));
                    
                    // Log memory usage if configured
                    if (Boolean.parseBoolean(config.getProperty(
                        "test.log.memory", "true"))) {
                        logger.logMemoryUsage(description.getMethodName());
                    }
                }
            }
        };
    }
    
    /**
     * Gets the test report generator instance
     */
    public static TestReportGenerator getGenerator() {
        return generator;
    }
    
    /**
     * Gets the test logger instance
     */
    public static TestLogger getLogger() {
        return logger;
    }
    
    /**
     * Gets a configuration property
     */
    public static String getProperty(String key, String defaultValue) {
        return config.getProperty(key, defaultValue);
    }
    
    /**
     * Sets a configuration property
     */
    public static void setProperty(String key, String value) {
        config.setProperty(key, value);
    }
    
    /**
     * Forces immediate report generation
     */
    public static void generateReport() {
        generator.generateReport();
    }
    
    /**
     * Records a custom test result
     */
    public static void recordCustomResult(String category, String testName,
                                        boolean success, long duration,
                                        Throwable error) {
        Description description = Description.createTestDescription(
            TestReportRule.class, testName);
        
        generator.recordTestResult(
            category,
            description,
            success,
            duration,
            error != null ? new org.junit.runner.notification.Failure(
                description, error) : null
        );
    }
    
    /**
     * Adds a custom section to the report
     * Useful for adding additional information or metrics
     */
    public static void addCustomSection(String category, String title,
                                      String content) {
        recordCustomResult(
            category,
            title,
            true,
            0,
            null
        );
    }
}
