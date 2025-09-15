#!/bin/bash

# Finance API Test Suite Runner
# This script runs all tests and generates comprehensive reports

set -e

echo "🚀 Starting Finance API Test Suite..."
echo "======================================"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Create test reports directory
mkdir -p test-reports

echo -e "${BLUE}📋 Test Categories:${NC}"
echo "1. Authentication Tests"
echo "2. CashFlow Integration Tests"
echo "3. Category Integration Tests"
echo "4. Dashboard Integration Tests"
echo "5. Performance Tests"
echo "6. Security Tests"
echo ""

# Function to run tests with reporting
run_test_suite() {
    local test_name=$1
    local test_class=$2
    local description=$3
    
    echo -e "${YELLOW}🧪 Running $test_name...${NC}"
    echo "Description: $description"
    echo "----------------------------------------"
    
    if mvn test -Dtest=$test_class -Dspring.profiles.active=test > test-reports/${test_name,,}.log 2>&1; then
        echo -e "${GREEN}✅ $test_name PASSED${NC}"
        return 0
    else
        echo -e "${RED}❌ $test_name FAILED${NC}"
        echo "Check test-reports/${test_name,,}.log for details"
        return 1
    fi
}

# Function to run all tests
run_all_tests() {
    local failed_tests=0
    
    echo -e "${BLUE}🔄 Running All Tests...${NC}"
    echo "======================================"
    
    if mvn test -Dspring.profiles.active=test > test-reports/all-tests.log 2>&1; then
        echo -e "${GREEN}✅ All Tests PASSED${NC}"
    else
        echo -e "${RED}❌ Some Tests FAILED${NC}"
        failed_tests=1
    fi
    
    return $failed_tests
}

# Function to run specific test categories
run_specific_tests() {
    local failed_tests=0
    
    # Authentication Tests
    if ! run_test_suite "Authentication" "AuthIntegrationTest" "Tests for login, registration, token validation, and logout"; then
        failed_tests=$((failed_tests + 1))
    fi
    
    # CashFlow Tests
    if ! run_test_suite "CashFlow" "CashFlowIntegrationTest" "Tests for cash flow CRUD operations, search, and filtering"; then
        failed_tests=$((failed_tests + 1))
    fi
    
    # Category Tests
    if ! run_test_suite "Category" "CategoryIntegrationTest" "Tests for category management and operations"; then
        failed_tests=$((failed_tests + 1))
    fi
    
    # Dashboard Tests
    if ! run_test_suite "Dashboard" "DashboardIntegrationTest" "Tests for dashboard overview and statistics"; then
        failed_tests=$((failed_tests + 1))
    fi
    
    # Performance Tests
    if ! run_test_suite "Performance" "PerformanceTest" "Tests for concurrent access, large datasets, and performance metrics"; then
        failed_tests=$((failed_tests + 1))
    fi
    
    # Security Tests
    if ! run_test_suite "Security" "SecurityTest" "Tests for security vulnerabilities, authorization, and input validation"; then
        failed_tests=$((failed_tests + 1))
    fi
    
    return $failed_tests
}

# Function to generate test report
generate_report() {
    echo -e "${BLUE}📊 Generating Test Report...${NC}"
    echo "======================================"
    
    local report_file="test-reports/test-summary.md"
    
    cat > $report_file << EOF
# Finance API Test Report
Generated on: $(date)

## Test Summary

### Test Categories
- **Authentication Tests**: Tests for login, registration, token validation, and logout
- **CashFlow Tests**: Tests for cash flow CRUD operations, search, and filtering  
- **Category Tests**: Tests for category management and operations
- **Dashboard Tests**: Tests for dashboard overview and statistics
- **Performance Tests**: Tests for concurrent access, large datasets, and performance metrics
- **Security Tests**: Tests for security vulnerabilities, authorization, and input validation

### Test Results
EOF

    # Count test results
    local total_tests=$(find test-reports -name "*.log" | wc -l)
    local passed_tests=$(grep -l "PASSED" test-reports/*.log 2>/dev/null | wc -l)
    local failed_tests=$((total_tests - passed_tests))
    
    echo "- Total Test Suites: $total_tests" >> $report_file
    echo "- Passed: $passed_tests" >> $report_file
    echo "- Failed: $failed_tests" >> $report_file
    echo "" >> $report_file
    
    echo "### Detailed Results" >> $report_file
    echo "" >> $report_file
    
    # Add individual test results
    for log_file in test-reports/*.log; do
        if [ -f "$log_file" ]; then
            local test_name=$(basename "$log_file" .log)
            echo "#### $test_name" >> $report_file
            echo "" >> $report_file
            echo "\`\`\`" >> $report_file
            tail -20 "$log_file" >> $report_file
            echo "\`\`\`" >> $report_file
            echo "" >> $report_file
        fi
    done
    
    echo -e "${GREEN}📄 Test report generated: $report_file${NC}"
}

# Function to run code coverage
run_coverage() {
    echo -e "${BLUE}📈 Running Code Coverage Analysis...${NC}"
    echo "======================================"
    
    if mvn test jacoco:report -Dspring.profiles.active=test > test-reports/coverage.log 2>&1; then
        echo -e "${GREEN}✅ Coverage report generated${NC}"
        echo "Check target/site/jacoco/index.html for detailed coverage report"
    else
        echo -e "${RED}❌ Coverage analysis failed${NC}"
    fi
}

# Function to clean up
cleanup() {
    echo -e "${BLUE}🧹 Cleaning up...${NC}"
    mvn clean
    echo -e "${GREEN}✅ Cleanup completed${NC}"
}

# Main execution
main() {
    local start_time=$(date +%s)
    
    # Parse command line arguments
    case "${1:-all}" in
        "auth")
            run_test_suite "Authentication" "AuthIntegrationTest" "Tests for authentication functionality"
            ;;
        "cashflow")
            run_test_suite "CashFlow" "CashFlowIntegrationTest" "Tests for cash flow functionality"
            ;;
        "category")
            run_test_suite "Category" "CategoryIntegrationTest" "Tests for category functionality"
            ;;
        "dashboard")
            run_test_suite "Dashboard" "DashboardIntegrationTest" "Tests for dashboard functionality"
            ;;
        "performance")
            run_test_suite "Performance" "PerformanceTest" "Tests for performance and scalability"
            ;;
        "security")
            run_test_suite "Security" "SecurityTest" "Tests for security vulnerabilities"
            ;;
        "specific")
            run_specific_tests
            ;;
        "all"|*)
            run_all_tests
            ;;
    esac
    
    local end_time=$(date +%s)
    local duration=$((end_time - start_time))
    
    echo ""
    echo -e "${BLUE}⏱️  Total execution time: ${duration}s${NC}"
    
    # Generate report
    generate_report
    
    # Run coverage if requested
    if [ "${2:-}" = "coverage" ]; then
        run_coverage
    fi
    
    # Cleanup if requested
    if [ "${3:-}" = "clean" ]; then
        cleanup
    fi
    
    echo ""
    echo -e "${GREEN}🎉 Test execution completed!${NC}"
    echo "Check test-reports/ directory for detailed results"
}

# Show usage if help requested
if [ "${1:-}" = "help" ] || [ "${1:-}" = "-h" ] || [ "${1:-}" = "--help" ]; then
    echo "Finance API Test Suite Runner"
    echo ""
    echo "Usage: $0 [test_type] [options]"
    echo ""
    echo "Test Types:"
    echo "  all        Run all tests (default)"
    echo "  auth       Run authentication tests only"
    echo "  cashflow   Run cash flow tests only"
    echo "  category   Run category tests only"
    echo "  dashboard  Run dashboard tests only"
    echo "  performance Run performance tests only"
    echo "  security   Run security tests only"
    echo "  specific   Run tests by category with individual reporting"
    echo ""
    echo "Options:"
    echo "  coverage   Generate code coverage report"
    echo "  clean      Clean up after tests"
    echo ""
    echo "Examples:"
    echo "  $0                    # Run all tests"
    echo "  $0 auth               # Run authentication tests only"
    echo "  $0 all coverage       # Run all tests with coverage"
    echo "  $0 specific clean     # Run specific tests and clean up"
    exit 0
fi

# Run main function
main "$@"
