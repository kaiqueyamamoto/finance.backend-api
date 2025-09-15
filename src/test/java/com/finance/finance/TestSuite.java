package com.finance.finance;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/**
 * Test Suite for Finance Backend API
 * 
 * This suite runs all tests in the following packages:
 * - Controller tests (unit tests for REST controllers)
 * - Service tests (unit tests for business logic)
 * - Integration tests (end-to-end tests)
 */
@Suite
@SelectPackages({
    "com.finance.finance.controller",
    "com.finance.finance.service", 
    "com.finance.finance.integration"
})
public class TestSuite {
    // This class serves as a test suite configuration
    // All tests in the specified packages will be executed
}
