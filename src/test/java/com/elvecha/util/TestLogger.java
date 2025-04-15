package com.elvecha.util;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Logger utility for testing purposes
 * Provides methods for logging test execution, performance metrics, and errors
 */
public class TestLogger {
    private static final String LOG_DIR = "test-logs";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");
    private static final Map<String, Long> timers = new ConcurrentHashMap<>();
    private static PrintWriter logWriter;
    private static boolean initialized = false;

    static {
        try {
            initialize();
        } catch (Exception e) {
            System.err.println("Failed to initialize TestLogger: " + e.getMessage());
        }
    }

    private static void initialize() throws Exception {
        if (!initialized) {
            // Create logs directory
            File logDir = new File(LOG_DIR);
            if (!logDir.exists()) {
                logDir.mkdirs();
            }

            // Create log file with timestamp
            String timestamp = DATE_FORMAT.format(new Date());
            File logFile = new File(logDir, "test_" + timestamp + ".log");
            logWriter = new PrintWriter(new FileWriter(logFile, true));

            // Add shutdown hook to close writer
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (logWriter != null) {
                    logWriter.close();
                }
            }));

            initialized = true;
            log("TestLogger initialized");
        }
    }

    /**
     * Logs a message with timestamp
     */
    public static void log(String message) {
        try {
            if (!initialized) {
                initialize();
            }

            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                .format(new Date());
            String threadName = Thread.currentThread().getName();
            String logMessage = String.format("[%s] [%s] %s",
                timestamp, threadName, message);

            logWriter.println(logMessage);
            logWriter.flush();
        } catch (Exception e) {
            System.err.println("Failed to log message: " + e.getMessage());
        }
    }

    /**
     * Logs an error message and stack trace
     */
    public static void logError(String message, Throwable error) {
        log("ERROR: " + message);
        if (error != null) {
            error.printStackTrace(logWriter);
            logWriter.flush();
        }
    }

    /**
     * Starts a timer with the given name
     */
    public static void startTimer(String name) {
        timers.put(name, System.nanoTime());
    }

    /**
     * Stops the timer and logs the elapsed time
     */
    public static long stopTimer(String name) {
        Long startTime = timers.remove(name);
        if (startTime == null) {
            log("Warning: Timer '" + name + "' was not started");
            return 0;
        }

        long elapsed = System.nanoTime() - startTime;
        log(String.format("Timer '%s' elapsed: %.3f ms",
            name, elapsed / 1_000_000.0));
        return elapsed;
    }

    /**
     * Measures and logs execution time of a code block
     */
    public static <T> T measureTime(String operation, Supplier<T> code) {
        startTimer(operation);
        try {
            return code.get();
        } finally {
            stopTimer(operation);
        }
    }

    /**
     * Logs memory usage information
     */
    public static void logMemoryUsage(String context) {
        Runtime runtime = Runtime.getRuntime();
        long total = runtime.totalMemory();
        long free = runtime.freeMemory();
        long used = total - free;
        long max = runtime.maxMemory();

        log(String.format("Memory Usage [%s]:", context));
        log(String.format("  Used: %d MB", used / 1024 / 1024));
        log(String.format("  Free: %d MB", free / 1024 / 1024));
        log(String.format("  Total: %d MB", total / 1024 / 1024));
        log(String.format("  Max: %d MB", max / 1024 / 1024));
    }

    /**
     * Logs test execution information
     */
    public static void logTestInfo(String testName, String description) {
        log("\n=== Test: " + testName + " ===");
        log("Description: " + description);
        logMemoryUsage("Test Start");
    }

    /**
     * Logs test completion status
     */
    public static void logTestComplete(String testName, boolean success) {
        log(String.format("Test '%s' %s", testName,
            success ? "PASSED" : "FAILED"));
        logMemoryUsage("Test End");
        log("=== End Test: " + testName + " ===\n");
    }

    /**
     * Cleans up old log files (older than specified days)
     */
    public static void cleanupOldLogs(int daysToKeep) {
        File logDir = new File(LOG_DIR);
        if (logDir.exists()) {
            File[] logFiles = logDir.listFiles();
            if (logFiles != null) {
                long cutoffTime = System.currentTimeMillis() - 
                    (daysToKeep * 24L * 60L * 60L * 1000L);
                
                for (File file : logFiles) {
                    if (file.lastModified() < cutoffTime) {
                        if (file.delete()) {
                            log("Deleted old log file: " + file.getName());
                        }
                    }
                }
            }
        }
    }

    /**
     * Closes the log writer
     */
    public static void close() {
        if (logWriter != null) {
            logWriter.close();
            initialized = false;
        }
    }
}
