package com.elvecha.util;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages test categories and their configurations
 * Handles category dependencies, resource requirements, and execution settings
 */
public class TestCategoryManager {
    private static final String CATEGORIES_FILE = "/test-categories.properties";
    private final Properties categoryConfig;
    private final Map<String, TestCategory> categories;
    private final TestLogger logger;
    
    public TestCategoryManager() {
        this.categoryConfig = new Properties();
        this.categories = new HashMap<>();
        this.logger = new TestLogger();
        loadConfiguration();
    }
    
    /**
     * Represents a test category with its configuration and dependencies
     */
    public static class TestCategory {
        private final String name;
        private final boolean enabled;
        private final int timeout;
        private final boolean parallel;
        private final Set<String> dependencies;
        private final Set<Class<?>> testClasses;
        private final Map<String, String> settings;
        
        private TestCategory(String name, Properties config) {
            this.name = name;
            this.enabled = Boolean.parseBoolean(config.getProperty(name + ".tests.enabled", "true"));
            this.timeout = Integer.parseInt(config.getProperty(name + ".tests.timeout", "5000"));
            this.parallel = Boolean.parseBoolean(config.getProperty(name + ".tests.parallel", "false"));
            this.dependencies = parseDependencies(config.getProperty(name + ".tests.dependencies", ""));
            this.testClasses = parseTestClasses(config.getProperty(name + ".tests.classes", ""));
            this.settings = extractSettings(name, config);
        }
        
        private Set<String> parseDependencies(String deps) {
            return Arrays.stream(deps.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
        }
        
        private Set<Class<?>> parseTestClasses(String classes) {
            return Arrays.stream(classes.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(className -> {
                    try {
                        return Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException("Test class not found: " + className, e);
                    }
                })
                .collect(Collectors.toSet());
        }
        
        private Map<String, String> extractSettings(String category, Properties config) {
            return config.stringPropertyNames().stream()
                .filter(key -> key.startsWith(category + "."))
                .collect(Collectors.toMap(
                    key -> key.substring(category.length() + 1),
                    config::getProperty
                ));
        }
        
        public String getName() { return name; }
        public boolean isEnabled() { return enabled; }
        public int getTimeout() { return timeout; }
        public boolean isParallel() { return parallel; }
        public Set<String> getDependencies() { return dependencies; }
        public Set<Class<?>> getTestClasses() { return testClasses; }
        public Map<String, String> getSettings() { return settings; }
    }
    
    /**
     * Loads and validates category configuration
     */
    private void loadConfiguration() {
        try {
            try (InputStream in = getClass().getResourceAsStream(CATEGORIES_FILE)) {
                if (in == null) {
                    throw new RuntimeException("Categories configuration file not found: " + CATEGORIES_FILE);
                }
                categoryConfig.load(in);
            }
            
            // Extract categories
            Set<String> categoryNames = categoryConfig.stringPropertyNames().stream()
                .filter(key -> key.endsWith(".tests.enabled"))
                .map(key -> key.substring(0, key.length() - ".tests.enabled".length()))
                .collect(Collectors.toSet());
                
            // Create category objects
            for (String name : categoryNames) {
                categories.put(name, new TestCategory(name, categoryConfig));
            }
            
            // Validate dependencies
            validateDependencies();
            
        } catch (Exception e) {
            logger.logError("Failed to load test categories", e);
            throw new RuntimeException("Failed to initialize test categories", e);
        }
    }
    
    /**
     * Validates category dependencies to detect cycles and missing dependencies
     */
    private void validateDependencies() {
        for (TestCategory category : categories.values()) {
            for (String dep : category.getDependencies()) {
                if (!categories.containsKey(dep)) {
                    throw new RuntimeException(
                        "Missing dependency '" + dep + "' for category '" + 
                        category.getName() + "'");
                }
            }
            
            // Check for dependency cycles
            detectCycles(category.getName(), new HashSet<>());
        }
    }
    
    private void detectCycles(String categoryName, Set<String> visited) {
        if (!visited.add(categoryName)) {
            throw new RuntimeException("Dependency cycle detected involving category '" + 
                categoryName + "'");
        }
        
        TestCategory category = categories.get(categoryName);
        if (category != null) {
            for (String dep : category.getDependencies()) {
                detectCycles(dep, new HashSet<>(visited));
            }
        }
    }
    
    /**
     * Gets ordered list of test categories respecting dependencies
     */
    public List<TestCategory> getOrderedCategories() {
        List<TestCategory> ordered = new ArrayList<>();
        Set<String> processed = new HashSet<>();
        
        for (TestCategory category : categories.values()) {
            if (category.isEnabled()) {
                addWithDependencies(category.getName(), ordered, processed);
            }
        }
        
        return ordered;
    }
    
    private void addWithDependencies(String categoryName, 
                                   List<TestCategory> ordered, 
                                   Set<String> processed) {
        if (processed.contains(categoryName)) {
            return;
        }
        
        TestCategory category = categories.get(categoryName);
        if (category == null) {
            return;
        }
        
        for (String dep : category.getDependencies()) {
            addWithDependencies(dep, ordered, processed);
        }
        
        ordered.add(category);
        processed.add(categoryName);
    }
    
    /**
     * Gets test classes for a category
     */
    public Set<Class<?>> getTestClasses(String category) {
        TestCategory cat = categories.get(category);
        return cat != null ? cat.getTestClasses() : Collections.emptySet();
    }
    
    /**
     * Gets category settings
     */
    public Map<String, String> getCategorySettings(String category) {
        TestCategory cat = categories.get(category);
        return cat != null ? cat.getSettings() : Collections.emptyMap();
    }
    
    /**
     * Checks if a category is enabled
     */
    public boolean isCategoryEnabled(String category) {
        TestCategory cat = categories.get(category);
        return cat != null && cat.isEnabled();
    }
    
    /**
     * Gets execution timeout for a category
     */
    public int getCategoryTimeout(String category) {
        TestCategory cat = categories.get(category);
        return cat != null ? cat.getTimeout() : 5000;
    }
    
    /**
     * Checks if a category supports parallel execution
     */
    public boolean isParallelExecutionSupported(String category) {
        TestCategory cat = categories.get(category);
        return cat != null && cat.isParallel();
    }
}
