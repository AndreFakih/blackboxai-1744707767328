package com.elvecha;

import com.elvecha.util.*;
import com.elvecha.model.*;
import com.elvecha.ui.models.*;
import com.elvecha.ui.renderers.*;
import com.elvecha.integration.*;
import com.elvecha.performance.*;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Comprehensive test suite for El Vecha Wedding Organizer DSS
 * Uses category-based test execution with complete test tracking and reporting
 * 
 * Test Categories:
 * - Model: Core business logic tests
 * - Utility: Helper and tool tests
 * - UI: User interface component tests
 * - Framework: Test infrastructure tests
 * - Integration: Component interaction tests
 * - Performance: System performance tests
 * 
 * Test Priorities:
 * - CRITICAL (1): Must pass for build
 * - HIGH (2): Essential functionality
 * - MEDIUM (3): Important features
 * - LOW (4): Nice-to-have features
 * 
 * Test Groups:
 * - FAST: Quick execution (<1s)
 * - SLOW: Long execution (>1s)
 * - ISOLATED: Must run alone
 * 
 * Test Dependencies:
 * - MODEL: Core model tests
 * - UTIL: Utility tests
 * - UI: Interface tests
 * - FRAMEWORK: Infrastructure tests
 * - INTEGRATION: System tests
 * - PERFORMANCE: Performance tests
 */
@RunWith(CategoryBasedTestRunner.class)
@SuiteClasses({
    // Model Tests
    CriteriaTest.class,
    AlternativeTest.class,
    
    // Utility Tests
    SAWCalculatorTest.class,
    DummyDataGeneratorTest.class,
    PDFExporterTest.class,
    TestUtilsTest.class,
    
    // UI Model Tests
    CustomTableModelTest.class,
    CustomTableRendererTest.class,
    
    // Test Framework Tests
    TestLoggerTest.class,
    TestReportGeneratorTest.class,
    TestReportBuilderTest.class,
    TestReportExporterTest.class,
    TestReportComparatorTest.class,
    TestReportAnalyzerTest.class,
    TestReportRuleTest.class,
    TestExecutionListenerTest.class,
    TestConfigValidatorTest.class,
    TestCategoryManagerTest.class,
    CategoryBasedTestRunnerTest.class,
    
    // Integration Tests
    SystemIntegrationTest.class,
    
    // Performance Tests
    PerformanceTest.class
})
public class ElVechaTestSuite {
    @ClassRule
    public static TestReportRule reportRule = new TestReportRule();
    
    private static final String[] REQUIRED_DIRS = {
        "test-temp", "test-logs", "test-reports", "test-data"
    };
    
    private static TestExecutionListener executionListener;
    private static TestLogger logger;
    private static TestCategoryManager categoryManager;
    private static TestConfigValidator configValidator;
    private static final String SESSION_ID = new SimpleDateFormat("yyyyMMdd_HHmmss")
        .format(new Date());
    private static long suiteStartTime;

    /**
     * Test categories for organization
     */
    public static class Categories {
        public interface ModelTests {}
        public interface UtilityTests {}
        public interface UITests {}
        public interface FrameworkTests {}
        public interface IntegrationTests {}
        public interface PerformanceTests {}
    }
    
    /**
     * Test priorities for execution order
     */
    public static class Priorities {
        public static final int CRITICAL = 1;
        public static final int HIGH = 2;
        public static final int MEDIUM = 3;
        public static final int LOW = 4;
    }
    
    /**
     * Test execution groups
     */
    public static class Groups {
        public static final String FAST = "fast";
        public static final String SLOW = "slow";
        public static final String ISOLATED = "isolated";
    }
    
    /**
     * Test dependencies
     */
    public static class Dependencies {
        public static final String MODEL = "model";
        public static final String UTIL = "util";
        public static final String UI = "ui";
        public static final String FRAMEWORK = "framework";
        public static final String INTEGRATION = "integration";
        public static final String PERFORMANCE = "performance";
    }
    
    /**
     * Test data sets
     */
    public static class DataSets {
        public static final String MINIMAL = "minimal";
        public static final String TYPICAL = "typical";
        public static final String COMPREHENSIVE = "comprehensive";
        public static final String EDGE_CASES = "edge-cases";
        public static final String STRESS = "stress";
    }
    
    /**
     * Test environments
     */
    public static class Environments {
        public static final String UNIT = "unit";
        public static final String INTEGRATION = "integration";
        public static final String PERFORMANCE = "performance";
        public static final String STRESS = "stress";
    }
    
    /**
     * Test configurations
     */
    public static class Configurations {
        public static final String DEFAULT = "default";
        public static final String MINIMAL = "minimal";
        public static final String FULL = "full";
        public static final String DEBUG = "debug";
    }
    
    /**
     * Test report formats
     */
    public static class ReportFormats {
        public static final String HTML = "html";
        public static final String PDF = "pdf";
        public static final String JSON = "json";
        public static final String XML = "xml";
    }
    
    /**
     * Test metrics
     */
    public static class Metrics {
        public static final String EXECUTION_TIME = "execution-time";
        public static final String MEMORY_USAGE = "memory-usage";
        public static final String CPU_USAGE = "cpu-usage";
        public static final String TEST_COVERAGE = "test-coverage";
    }
    
    /**
     * Test thresholds
     */
    public static class Thresholds {
        public static final double MIN_PASS_RATE = 0.9;
        public static final double MAX_SKIP_RATE = 0.1;
        public static final long MAX_TEST_DURATION = 5000;
        public static final double MIN_COVERAGE = 0.8;
    }
    
    @BeforeClass
    public static void setUp() {
        try {
            suiteStartTime = System.currentTimeMillis();
            
            // Initialize components
            initializeComponents();
            
            // Validate environment
            validateEnvironment();
            
            // Configure test execution
            configureTestExecution();
            
            // Log suite initialization
            logSuiteStart();
            
        } catch (Exception e) {
            handleInitializationError(e);
        }
    }
    
    @AfterClass
    public static void tearDown() {
        try {
            // Log suite completion
            logSuiteCompletion();
            
            // Generate final reports
            generateFinalReports();
            
            // Clean up environment
            cleanupEnvironment();
            
        } catch (Exception e) {
            handleCleanupError(e);
        }
    }
    
    private static void initializeComponents() {
        logger = new TestLogger();
        logger.log("Initializing test components");
        
        categoryManager = new TestCategoryManager();
        configValidator = new TestConfigValidator();
        executionListener = new TestExecutionListener();
        
        createRequiredDirectories();
    }
    
    private static void createRequiredDirectories() {
        for (String dir : REQUIRED_DIRS) {
            File directory = new File(dir);
            if (!directory.exists() && !directory.mkdirs()) {
                throw new RuntimeException("Failed to create directory: " + dir);
            }
        }
    }
    
    private static void validateEnvironment() {
        logger.log("Validating test environment");
        
        if (!configValidator.validateEnvironment()) {
            String errors = configValidator.getValidationErrors();
            logger.logError("Environment validation failed", null);
            logger.log(errors);
            
            reportRule.addCustomSection(
                "Environment Validation",
                "Validation Errors",
                errors
            );
            
            throw new RuntimeException("Environment validation failed:\n" + errors);
        }
        
        logger.log("Environment validation passed");
    }
    
    private static void configureTestExecution() {
        // Get test categories
        List<TestCategoryManager.TestCategory> categories = 
            categoryManager.getOrderedCategories();
            
        // Log category configuration
        logCategoryConfiguration(categories);
        
        // Add environment info to report
        reportRule.addCustomSection(
            "Test Environment",
            "Configuration",
            generateEnvironmentInfo(categories)
        );
    }
    
    private static void logCategoryConfiguration(
            List<TestCategoryManager.TestCategory> categories) {
        logger.log("\nTest Categories Configuration:");
        for (TestCategoryManager.TestCategory category : categories) {
            logger.log(String.format(
                "- %s:\n  Enabled: %s\n  Parallel: %s\n  Classes: %d\n",
                category.getName(),
                category.isEnabled(),
                category.isParallel(),
                category.getTestClasses().size()
            ));
        }
        logger.log("");
    }
    
    private static void logSuiteStart() {
        logger.logTestInfo("El Vecha Test Suite",
            "Starting category-based test execution");
        logger.log("Test Session ID: " + SESSION_ID);
        logger.logMemoryUsage("Suite Start");
    }
    
    private static void logSuiteCompletion() {
        logger.logTestComplete("El Vecha Test Suite", true);
        logger.logMemoryUsage("Suite End");
    }
    
    private static void generateFinalReports() {
        reportRule.addCustomSection(
            "Test Results",
            "Final Metrics",
            generateFinalMetrics()
        );
        
        TestReportRule.generateReport();
    }
    
    private static void cleanupEnvironment() {
        for (String dir : REQUIRED_DIRS) {
            if (!dir.equals("test-reports")) {
                TestUtils.cleanupTestFiles(dir);
            }
        }
        
        int retentionDays = Integer.parseInt(
            TestReportRule.getProperty("file.cleanup.days", "5"));
        TestLogger.cleanupOldLogs(retentionDays);
    }
    
    private static void handleInitializationError(Exception e) {
        String error = "Failed to initialize test suite: " + e.getMessage();
        System.err.println(error);
        if (logger != null) {
            logger.logError(error, e);
        }
        throw new RuntimeException(error, e);
    }
    
    private static void handleCleanupError(Exception e) {
        String error = "Failed to cleanup test suite: " + e.getMessage();
        System.err.println(error);
        if (logger != null) {
            logger.logError(error, e);
        }
    }
    
    private static String generateEnvironmentInfo(
            List<TestCategoryManager.TestCategory> categories) {
        StringBuilder info = new StringBuilder();
        info.append("Test Environment Information:\n\n");
        info.append("Session ID: ").append(SESSION_ID).append("\n");
        info.append("Java Version: ").append(System.getProperty("java.version")).append("\n");
        info.append("OS: ").append(System.getProperty("os.name")).append("\n");
        info.append("Memory: ").append(Runtime.getRuntime().maxMemory() / (1024*1024)).append("MB\n");
        info.append("Processors: ").append(Runtime.getRuntime().availableProcessors()).append("\n\n");
        
        info.append("Test Categories:\n");
        for (TestCategoryManager.TestCategory category : categories) {
            info.append(String.format("- %s:\n", category.getName()));
            info.append(String.format("  Enabled: %s\n", category.isEnabled()));
            info.append(String.format("  Parallel: %s\n", category.isParallel()));
            info.append(String.format("  Test Classes: %d\n", 
                category.getTestClasses().size()));
            info.append(String.format("  Dependencies: %s\n",
                String.join(", ", category.getDependencies())));
            info.append("\n");
        }
        
        return info.toString();
    }
    
    private static String generateFinalMetrics() {
        StringBuilder metrics = new StringBuilder();
        metrics.append("Test Execution Metrics:\n\n");
        
        // Execution time
        long duration = System.currentTimeMillis() - suiteStartTime;
        metrics.append(String.format("Total Duration: %.2f seconds\n",
            duration / 1000.0));
        
        // Memory usage
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / (1024*1024);
        metrics.append(String.format("Peak Memory Usage: %d MB\n", usedMemory));
        
        // Category metrics
        List<TestCategoryManager.TestCategory> categories = 
            categoryManager.getOrderedCategories();
        metrics.append(String.format("\nEnabled Categories: %d\n",
            categories.stream()
                .filter(TestCategoryManager.TestCategory::isEnabled)
                .count()));
        
        metrics.append(String.format("Total Test Classes: %d\n",
            categories.stream()
                .filter(TestCategoryManager.TestCategory::isEnabled)
                .mapToInt(c -> c.getTestClasses().size())
                .sum()));
        
        return metrics.toString();
    }
}
