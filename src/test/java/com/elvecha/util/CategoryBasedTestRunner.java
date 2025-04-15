package com.elvecha.util;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Custom test runner that executes tests based on categories
 * Supports parallel execution and dependency management
 */
public class CategoryBasedTestRunner extends ParentRunner<Runner> {
    private final List<Runner> runners;
    private final TestCategoryManager categoryManager;
    private final TestLogger logger;
    private final Map<String, ExecutorService> categoryExecutors;
    
    public CategoryBasedTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        this.categoryManager = new TestCategoryManager();
        this.logger = new TestLogger();
        this.categoryExecutors = new HashMap<>();
        this.runners = initializeRunners();
    }
    
    private List<Runner> initializeRunners() throws InitializationError {
        try {
            List<Runner> categoryRunners = new ArrayList<>();
            RunnerBuilder builder = new RunnerBuilder() {
                @Override
                public Runner runnerForClass(Class<?> testClass) throws Throwable {
                    return org.junit.runners.BlockJUnit4ClassRunner.class
                        .getConstructor(Class.class)
                        .newInstance(testClass);
                }
            };
            
            // Get ordered categories
            List<TestCategoryManager.TestCategory> categories = 
                categoryManager.getOrderedCategories();
            
            // Create runners for each category
            for (TestCategoryManager.TestCategory category : categories) {
                if (category.isEnabled()) {
                    categoryRunners.addAll(
                        createRunnersForCategory(category, builder));
                }
            }
            
            return categoryRunners;
            
        } catch (Exception e) {
            logger.logError("Failed to initialize runners", e);
            throw new InitializationError(e);
        }
    }
    
    private List<Runner> createRunnersForCategory(
            TestCategoryManager.TestCategory category,
            RunnerBuilder builder) throws Exception {
        List<Runner> runners = new ArrayList<>();
        
        // Create executor for parallel execution if needed
        if (category.isParallel()) {
            int threads = Integer.parseInt(
                category.getSettings().getOrDefault("thread.pool", "4"));
            categoryExecutors.put(category.getName(),
                Executors.newFixedThreadPool(threads));
        }
        
        // Create runners for each test class
        for (Class<?> testClass : category.getTestClasses()) {
            runners.add(builder.runnerForClass(testClass));
        }
        
        return runners;
    }
    
    @Override
    protected List<Runner> getChildren() {
        return runners;
    }
    
    @Override
    protected Description describeChild(Runner child) {
        return child.getDescription();
    }
    
    @Override
    protected void runChild(Runner runner, RunNotifier notifier) {
        String category = getCategoryForRunner(runner);
        ExecutorService executor = categoryExecutors.get(category);
        
        if (executor != null) {
            // Parallel execution
            executor.submit(() -> runChildWithTimeout(runner, notifier, category));
        } else {
            // Sequential execution
            runChildWithTimeout(runner, notifier, category);
        }
    }
    
    private void runChildWithTimeout(Runner runner, RunNotifier notifier, 
                                   String category) {
        TestCategoryManager.TestCategory categoryConfig = 
            categoryManager.getOrderedCategories().stream()
                .filter(c -> c.getName().equals(category))
                .findFirst()
                .orElse(null);
                
        if (categoryConfig != null) {
            int timeout = categoryConfig.getTimeout();
            Timer timer = new Timer(true);
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    logger.logError(String.format(
                        "Test timeout after %d ms in category %s",
                        timeout, category), null);
                }
            };
            
            try {
                timer.schedule(task, timeout);
                runner.run(notifier);
            } finally {
                timer.cancel();
            }
        } else {
            runner.run(notifier);
        }
    }
    
    private String getCategoryForRunner(Runner runner) {
        String className = runner.getDescription().getTestClass().getName();
        return categoryManager.getOrderedCategories().stream()
            .filter(category -> category.getTestClasses().stream()
                .anyMatch(testClass -> testClass.getName().equals(className)))
            .findFirst()
            .map(TestCategoryManager.TestCategory::getName)
            .orElse("unknown");
    }
    
    @Override
    public void run(RunNotifier notifier) {
        try {
            logger.log("Starting category-based test execution");
            
            // Run tests
            super.run(notifier);
            
            // Wait for parallel executions to complete
            shutdownExecutors();
            
            logger.log("Category-based test execution completed");
            
        } catch (Exception e) {
            logger.logError("Test execution failed", e);
            throw new RuntimeException("Test execution failed", e);
        }
    }
    
    private void shutdownExecutors() {
        for (Map.Entry<String, ExecutorService> entry : 
             categoryExecutors.entrySet()) {
            ExecutorService executor = entry.getValue();
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                    logger.logError(String.format(
                        "Timeout waiting for category %s to complete",
                        entry.getKey()), null);
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.logError(String.format(
                    "Interrupted while waiting for category %s",
                    entry.getKey()), e);
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}
