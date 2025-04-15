#!/bin/bash

# Test Execution Script for El Vecha Test Framework
# Provides various options for running tests with different configurations

# Default values
CATEGORY="all"
PARALLEL="false"
MEMORY="1024m"
REPORT_DIR="test-reports"
LOG_LEVEL="INFO"
COVERAGE="true"
DEBUG="false"

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Help function
show_help() {
    echo "Usage: $0 [options]"
    echo
    echo "Options:"
    echo "  -c, --category     Test category to run (model|util|ui|integration|performance|all)"
    echo "  -p, --parallel     Enable parallel execution (true|false)"
    echo "  -m, --memory       JVM memory limit (e.g., 1024m, 2g)"
    echo "  -r, --report-dir   Report directory"
    echo "  -l, --log-level    Log level (INFO|DEBUG|TRACE)"
    echo "  -n, --no-coverage  Disable code coverage"
    echo "  -d, --debug        Enable debug mode"
    echo "  -h, --help         Show this help message"
    echo
    echo "Examples:"
    echo "  $0 -c model                    # Run model tests only"
    echo "  $0 -c util -p true             # Run util tests in parallel"
    echo "  $0 -c all -m 2g                # Run all tests with 2GB memory"
    echo "  $0 -c performance -l DEBUG     # Run performance tests with debug logging"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -c|--category)
            CATEGORY="$2"
            shift 2
            ;;
        -p|--parallel)
            PARALLEL="$2"
            shift 2
            ;;
        -m|--memory)
            MEMORY="$2"
            shift 2
            ;;
        -r|--report-dir)
            REPORT_DIR="$2"
            shift 2
            ;;
        -l|--log-level)
            LOG_LEVEL="$2"
            shift 2
            ;;
        -n|--no-coverage)
            COVERAGE="false"
            shift
            ;;
        -d|--debug)
            DEBUG="true"
            shift
            ;;
        -h|--help)
            show_help
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            show_help
            exit 1
            ;;
    esac
done

# Validate category
valid_categories=("model" "util" "ui" "integration" "performance" "all")
if [[ ! " ${valid_categories[@]} " =~ " ${CATEGORY} " ]]; then
    echo -e "${RED}Invalid category: ${CATEGORY}${NC}"
    exit 1
fi

# Prepare directories
echo -e "${YELLOW}Preparing test environment...${NC}"
mkdir -p "${REPORT_DIR}"
mkdir -p test-logs
mkdir -p test-temp

# Clean previous test files
echo -e "${YELLOW}Cleaning previous test files...${NC}"
rm -rf "${REPORT_DIR}"/*
rm -rf test-logs/*
rm -rf test-temp/*

# Build Maven command
MVN_CMD="mvn clean test"

# Add category configuration
if [ "$CATEGORY" != "all" ]; then
    MVN_CMD="$MVN_CMD -Dtest.categories=$CATEGORY"
fi

# Add parallel execution configuration
MVN_CMD="$MVN_CMD -Dtest.parallel.execution=$PARALLEL"

# Add memory configuration
MVN_CMD="$MVN_CMD -Xmx$MEMORY"

# Add report directory configuration
MVN_CMD="$MVN_CMD -Dtest.report.dir=$REPORT_DIR"

# Add log level configuration
MVN_CMD="$MVN_CMD -Dlog.level=$LOG_LEVEL"

# Add coverage configuration
if [ "$COVERAGE" = "false" ]; then
    MVN_CMD="$MVN_CMD -Djacoco.skip=true"
fi

# Add debug configuration
if [ "$DEBUG" = "true" ]; then
    MVN_CMD="$MVN_CMD -Dmaven.surefire.debug"
fi

# Display configuration
echo -e "${YELLOW}Test Configuration:${NC}"
echo "Category: $CATEGORY"
echo "Parallel Execution: $PARALLEL"
echo "Memory: $MEMORY"
echo "Report Directory: $REPORT_DIR"
echo "Log Level: $LOG_LEVEL"
echo "Coverage Enabled: $COVERAGE"
echo "Debug Mode: $DEBUG"
echo

# Execute tests
echo -e "${YELLOW}Executing tests...${NC}"
echo "Command: $MVN_CMD"
echo

if eval "$MVN_CMD"; then
    echo -e "${GREEN}Tests completed successfully${NC}"
    
    # Generate test summary
    echo -e "\n${YELLOW}Test Summary:${NC}"
    echo "----------------------------------------"
    
    # Count test results
    TOTAL_TESTS=$(grep -r "Tests run:" "${REPORT_DIR}" | awk '{sum += $3} END {print sum}')
    FAILURES=$(grep -r "Failures:" "${REPORT_DIR}" | awk '{sum += $5} END {print sum}')
    ERRORS=$(grep -r "Errors:" "${REPORT_DIR}" | awk '{sum += $7} END {print sum}')
    SKIPPED=$(grep -r "Skipped:" "${REPORT_DIR}" | awk '{sum += $9} END {print sum}')
    
    echo "Total Tests: $TOTAL_TESTS"
    echo "Failures: $FAILURES"
    echo "Errors: $ERRORS"
    echo "Skipped: $SKIPPED"
    
    # Display coverage if enabled
    if [ "$COVERAGE" = "true" ]; then
        echo -e "\n${YELLOW}Coverage Summary:${NC}"
        echo "----------------------------------------"
        COVERAGE_FILE="target/site/jacoco/index.html"
        if [ -f "$COVERAGE_FILE" ]; then
            TOTAL_COVERAGE=$(grep -A 1 "Total" "$COVERAGE_FILE" | grep "%" | awk -F">" '{print $2}' | awk -F"%" '{print $1}')
            echo "Total Coverage: $TOTAL_COVERAGE%"
        else
            echo "Coverage report not found"
        fi
    fi
    
    echo -e "\n${GREEN}Test reports available in: ${REPORT_DIR}${NC}"
    exit 0
else
    echo -e "${RED}Tests failed${NC}"
    exit 1
fi
