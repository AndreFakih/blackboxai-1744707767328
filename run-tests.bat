@echo off
setlocal EnableDelayedExpansion

:: Color codes
set RED=[91m
set GREEN=[92m
set YELLOW=[93m
set BLUE=[94m
set MAGENTA=[95m
set CYAN=[96m
set NC=[0m

:: Default values
set CATEGORY=all
set PARALLEL=false
set MEMORY=1024m
set REPORT_DIR=test-reports
set LOG_LEVEL=INFO
set COVERAGE=true
set DEBUG=false
set RETRY_COUNT=3
set RETRY_DELAY=5
set TIMEOUT=300
set PROFILE=false
set BASELINE=false
set FORMAT=html,pdf

:: Banner
echo %MAGENTA%=============================%NC%
echo %MAGENTA% El Vecha Test Suite Runner %NC%
echo %MAGENTA%=============================%NC%
echo.

:: Parse command line arguments
:parse_args
if "%~1"=="" goto validate_args
if /i "%~1"=="-c" set CATEGORY=%~2& shift & shift & goto parse_args
if /i "%~1"=="--category" set CATEGORY=%~2& shift & shift & goto parse_args
if /i "%~1"=="-p" set PARALLEL=%~2& shift & shift & goto parse_args
if /i "%~1"=="--parallel" set PARALLEL=%~2& shift & shift & goto parse_args
if /i "%~1"=="-m" set MEMORY=%~2& shift & shift & goto parse_args
if /i "%~1"=="--memory" set MEMORY=%~2& shift & shift & goto parse_args
if /i "%~1"=="-r" set REPORT_DIR=%~2& shift & shift & goto parse_args
if /i "%~1"=="--report-dir" set REPORT_DIR=%~2& shift & shift & goto parse_args
if /i "%~1"=="-l" set LOG_LEVEL=%~2& shift & shift & goto parse_args
if /i "%~1"=="--log-level" set LOG_LEVEL=%~2& shift & shift & goto parse_args
if /i "%~1"=="-n" set COVERAGE=false& shift & goto parse_args
if /i "%~1"=="--no-coverage" set COVERAGE=false& shift & goto parse_args
if /i "%~1"=="-d" set DEBUG=true& shift & goto parse_args
if /i "%~1"=="--debug" set DEBUG=true& shift & goto parse_args
if /i "%~1"=="--retry" set RETRY_COUNT=%~2& shift & shift & goto parse_args
if /i "%~1"=="--retry-delay" set RETRY_DELAY=%~2& shift & shift & goto parse_args
if /i "%~1"=="--timeout" set TIMEOUT=%~2& shift & shift & goto parse_args
if /i "%~1"=="--profile" set PROFILE=true& shift & goto parse_args
if /i "%~1"=="--baseline" set BASELINE=true& shift & goto parse_args
if /i "%~1"=="--format" set FORMAT=%~2& shift & shift & goto parse_args
if /i "%~1"=="-h" goto show_help
if /i "%~1"=="--help" goto show_help
echo %RED%Unknown option: %~1%NC%
goto show_help

:show_help
echo Usage: %0 [options]
echo.
echo Options:
echo   -c, --category     Test category (model^|util^|ui^|integration^|performance^|all)
echo   -p, --parallel     Enable parallel execution (true^|false)
echo   -m, --memory       JVM memory limit (e.g., 1024m, 2g)
echo   -r, --report-dir   Report directory
echo   -l, --log-level    Log level (INFO^|DEBUG^|TRACE)
echo   -n, --no-coverage  Disable code coverage
echo   -d, --debug        Enable debug mode
echo   --retry            Number of retries for failed tests
echo   --retry-delay      Delay between retries in seconds
echo   --timeout         Test timeout in seconds
echo   --profile         Enable performance profiling
echo   --baseline        Compare with baseline performance
echo   --format          Report formats (html,pdf,json,xml)
echo   -h, --help         Show this help message
echo.
echo Examples:
echo   %0 -c model                    # Run model tests only
echo   %0 -c util -p true             # Run util tests in parallel
echo   %0 -c all -m 2g                # Run all tests with 2GB memory
echo   %0 --retry 3 --retry-delay 5   # Retry failed tests 3 times
exit /b 1

:validate_args
:: Validate category
echo %CATEGORY%| findstr /i "^model$ ^util$ ^ui$ ^integration$ ^performance$ ^all$" >nul
if errorlevel 1 (
    echo %RED%Invalid category: %CATEGORY%%NC%
    exit /b 1
)

:: Validate log level
echo %LOG_LEVEL%| findstr /i "^INFO$ ^DEBUG$ ^TRACE$" >nul
if errorlevel 1 (
    echo %RED%Invalid log level: %LOG_LEVEL%%NC%
    exit /b 1
)

:: Check Java
java -version >nul 2>&1
if errorlevel 1 (
    echo %RED%Error: Java is not installed%NC%
    exit /b 1
)

:: Check Maven
mvn -v >nul 2>&1
if errorlevel 1 (
    echo %RED%Error: Maven is not installed%NC%
    exit /b 1
)

:: Create directories
echo %BLUE%Creating test directories...%NC%
if not exist %REPORT_DIR% mkdir %REPORT_DIR%
if not exist test-logs mkdir test-logs
if not exist test-temp mkdir test-temp
if not exist test-data mkdir test-data

:: Clean previous results
echo %BLUE%Cleaning previous test results...%NC%
if exist %REPORT_DIR%\* del /Q %REPORT_DIR%\*
if exist test-logs\* del /Q test-logs\*
if exist test-temp\* del /Q test-temp\*

:: Build Maven command
set MVN_CMD=mvn clean test

:: Add configurations
if not "%CATEGORY%"=="all" set MVN_CMD=%MVN_CMD% -Dtest.categories=%CATEGORY%
set MVN_CMD=%MVN_CMD% -Dtest.parallel.execution=%PARALLEL%
set MVN_CMD=%MVN_CMD% -Xmx%MEMORY%
set MVN_CMD=%MVN_CMD% -Dtest.report.dir=%REPORT_DIR%
set MVN_CMD=%MVN_CMD% -Dlog.level=%LOG_LEVEL%
if "%COVERAGE%"=="false" set MVN_CMD=%MVN_CMD% -Djacoco.skip=true
if "%DEBUG%"=="true" set MVN_CMD=%MVN_CMD% -Dmaven.surefire.debug
set MVN_CMD=%MVN_CMD% -Dsurefire.rerunFailingTestsCount=%RETRY_COUNT%
set MVN_CMD=%MVN_CMD% -Dtest.retry.delay=%RETRY_DELAY%
set MVN_CMD=%MVN_CMD% -Dsurefire.timeout=%TIMEOUT%
if "%PROFILE%"=="true" set MVN_CMD=%MVN_CMD% -Dtest.profile=true
if "%BASELINE%"=="true" set MVN_CMD=%MVN_CMD% -Dtest.baseline=true
set MVN_CMD=%MVN_CMD% -Dtest.report.format=%FORMAT%

:: Display configuration
echo %MAGENTA%Test Configuration:%NC%
echo Category: %CATEGORY%
echo Parallel Execution: %PARALLEL%
echo Memory: %MEMORY%
echo Report Directory: %REPORT_DIR%
echo Log Level: %LOG_LEVEL%
echo Coverage Enabled: %COVERAGE%
echo Debug Mode: %DEBUG%
echo Retry Count: %RETRY_COUNT%
echo Retry Delay: %RETRY_DELAY%s
echo Timeout: %TIMEOUT%s
echo Profiling: %PROFILE%
echo Baseline Comparison: %BASELINE%
echo Report Formats: %FORMAT%
echo.

:: Execute tests
echo %YELLOW%Executing tests...%NC%
echo Command: %MVN_CMD%
echo.

set START_TIME=%TIME%

%MVN_CMD%

set END_TIME=%TIME%

:: Calculate duration
for /f "tokens=1-4 delims=:.," %%a in ("%START_TIME%") do set /a START_S=((%%a*60+1%%b %% 100)*60+1%%c %% 100)
for /f "tokens=1-4 delims=:.," %%a in ("%END_TIME%") do set /a END_S=((%%a*60+1%%b %% 100)*60+1%%c %% 100)
set /a DURATION=END_S-START_S
if %DURATION% lss 0 set /a DURATION+=86400

if not errorlevel 1 (
    echo %GREEN%Tests completed successfully%NC%
    echo Duration: %DURATION%s
    
    :: Display test summary
    echo.
    echo %MAGENTA%Test Summary:%NC%
    echo ---------------------
    
    :: Parse test results
    findstr /r /c:"Tests run:" %REPORT_DIR%\*.txt > test-temp\results.txt
    for /f "tokens=3,5,7,9 delims=, " %%a in (test-temp\results.txt) do (
        set /a TOTAL_TESTS=%%a
        set /a FAILURES=%%b
        set /a ERRORS=%%c
        set /a SKIPPED=%%d
    )
    
    echo Total Tests: !TOTAL_TESTS!
    echo Failures: !FAILURES!
    echo Errors: !ERRORS!
    echo Skipped: !SKIPPED!
    
    :: Display coverage if enabled
    if "%COVERAGE%"=="true" (
        echo.
        echo %MAGENTA%Coverage Summary:%NC%
        echo ---------------------
        if exist target\site\jacoco\index.html (
            for /f "tokens=2 delims=>" %%a in ('findstr /c:"Total" target\site\jacoco\index.html') do (
                for /f "tokens=1 delims=%%" %%b in ("%%a") do (
                    echo Total Coverage: %%b%%
                )
            )
        ) else (
            echo Coverage report not found
        )
    )
    
    :: Display performance metrics if enabled
    if "%PROFILE%"=="true" (
        echo.
        echo %MAGENTA%Performance Summary:%NC%
        echo ---------------------
        if exist test-reports\performance.json (
            echo Performance report available in: test-reports\performance.json
        )
    )
    
    :: Display baseline comparison if enabled
    if "%BASELINE%"=="true" (
        echo.
        echo %MAGENTA%Baseline Comparison:%NC%
        echo ---------------------
        if exist test-reports\baseline-comparison.json (
            echo Baseline comparison available in: test-reports\baseline-comparison.json
        )
    )
    
    echo.
    echo %GREEN%Test reports available in: %REPORT_DIR%%NC%
    exit /b 0
) else (
    echo %RED%Tests failed%NC%
    echo Duration: %DURATION%s
    
    :: Display failure summary
    echo.
    echo %RED%Failure Summary:%NC%
    echo ---------------------
    findstr /r /c:"<<< FAILURE!" %REPORT_DIR%\*.txt
    
    exit /b 1
)

endlocal
