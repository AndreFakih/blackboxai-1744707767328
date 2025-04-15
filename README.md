# El Vecha Test Framework

A comprehensive test framework for the El Vecha Wedding Organizer Decision Support System, featuring category-based test execution, detailed reporting, and performance analysis.

## Features

- **Category-Based Testing**: Organize and run tests by category (model, util, ui, etc.)
- **Parallel Execution**: Run tests concurrently for faster execution
- **Detailed Reporting**: Generate comprehensive HTML, PDF, JSON, and XML reports
- **Code Coverage**: Track test coverage with JaCoCo integration
- **Performance Metrics**: Monitor and analyze test performance
- **Baseline Comparison**: Compare performance against established baselines
- **Failure Retry**: Automatically retry failed tests with configurable delays
- **Environment Validation**: Verify system requirements before test execution

## Requirements

- Java 11 or higher
- Maven 3.6 or higher
- Minimum 2GB RAM (4GB recommended)
- 1GB free disk space

## Directory Structure

```
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── elvecha/
│   │               ├── model/
│   │               ├── util/
│   │               └── ui/
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── elvecha/
│       │           ├── model/
│       │           ├── util/
│       │           └── ui/
│       └── resources/
│           ├── test.properties
│           ├── logback-test.xml
│           └── report-template.html
├── test-reports/
├── test-logs/
├── test-temp/
└── test-data/
```

## Test Categories

1. **Model Tests** (`model`)
   - Core business logic
   - Data model validation
   - Business rules verification

2. **Utility Tests** (`util`)
   - Helper functions
   - Common utilities
   - Tool validations

3. **UI Tests** (`ui`)
   - Interface components
   - User interaction flows
   - Layout validations

4. **Integration Tests** (`integration`)
   - Component interactions
   - System integration
   - End-to-end flows

5. **Performance Tests** (`performance`)
   - Response time measurements
   - Resource usage monitoring
   - Scalability verification

## Running Tests

### Using Shell Script (Linux/Mac)

```bash
# Run all tests
./run-tests.sh

# Run specific category
./run-tests.sh -c model

# Run with parallel execution
./run-tests.sh -c util -p true

# Run with increased memory
./run-tests.sh -m 2g

# Run with retry on failure
./run-tests.sh --retry 3 --retry-delay 5

# Run with performance profiling
./run-tests.sh --profile --baseline
```

### Using Batch Script (Windows)

```batch
# Run all tests
run-tests.bat

# Run specific category
run-tests.bat -c model

# Run with parallel execution
run-tests.bat -c util -p true

# Run with increased memory
run-tests.bat -m 2g

# Run with retry on failure
run-tests.bat --retry 3 --retry-delay 5

# Run with performance profiling
run-tests.bat --profile --baseline
```

## Configuration Options

| Option | Description | Default | Values |
|--------|-------------|---------|---------|
| `-c, --category` | Test category | `all` | `model`, `util`, `ui`, `integration`, `performance`, `all` |
| `-p, --parallel` | Parallel execution | `false` | `true`, `false` |
| `-m, --memory` | JVM memory limit | `1024m` | e.g., `1024m`, `2g` |
| `-r, --report-dir` | Report directory | `test-reports` | Any valid path |
| `-l, --log-level` | Log level | `INFO` | `INFO`, `DEBUG`, `TRACE` |
| `-n, --no-coverage` | Disable coverage | `false` | Flag |
| `-d, --debug` | Debug mode | `false` | Flag |
| `--retry` | Retry count | `3` | Integer > 0 |
| `--retry-delay` | Retry delay (seconds) | `5` | Integer > 0 |
| `--timeout` | Test timeout (seconds) | `300` | Integer > 0 |
| `--profile` | Enable profiling | `false` | Flag |
| `--baseline` | Compare baseline | `false` | Flag |
| `--format` | Report formats | `html,pdf` | `html`, `pdf`, `json`, `xml` |

## Test Reports

Reports are generated in the specified report directory (default: `test-reports/`):

- `index.html`: Main HTML report with interactive features
- `report.pdf`: PDF version for documentation
- `report.json`: JSON format for programmatic analysis
- `report.xml`: XML format for CI/CD integration
- `performance.json`: Performance metrics (if profiling enabled)
- `baseline-comparison.json`: Baseline comparison (if enabled)

## Coverage Reports

Coverage reports are generated using JaCoCo and can be found in:
- `target/site/jacoco/index.html`: Main coverage report
- `target/site/jacoco/jacoco.xml`: Coverage data in XML format

## Performance Metrics

When profiling is enabled (`--profile`), the following metrics are collected:

- Execution time per test
- Memory usage
- CPU utilization
- Thread count
- I/O operations
- Database interactions

## Baseline Comparison

When baseline comparison is enabled (`--baseline`), performance metrics are compared against stored baselines:

- Response time deviation
- Memory usage patterns
- Resource utilization trends
- Performance regression detection

## Troubleshooting

1. **Tests fail to start**
   - Verify Java and Maven installation
   - Check memory settings
   - Ensure required directories exist

2. **Out of Memory Errors**
   - Increase memory with `-m` option
   - Reduce parallel execution threads
   - Clean temporary files

3. **Report Generation Fails**
   - Check disk space
   - Verify write permissions
   - Ensure report directory exists

4. **Performance Issues**
   - Disable parallel execution
   - Increase timeout values
   - Monitor system resources

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.
