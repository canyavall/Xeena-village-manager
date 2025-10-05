# Running Tests - Quick Reference Guide

## Test Suite Overview

**Total Tests:** 259 tests across 5 test files
**Latest Addition:** GuardSpecialAbilitiesTest.java (93 tests for Tier 4 abilities)

---

## Running Specific Test Files

### Run Tier 4 Abilities Tests Only (93 tests)
```bash
./gradlew test --tests GuardSpecialAbilitiesTest
```

### Run AI Goal Priority Tests (41 tests)
```bash
./gradlew test --tests GuardAIGoalPriorityTest
```

### Run Combat Mechanics Tests (31 tests)
```bash
./gradlew test --tests GuardCombatMechanicsTest
```

### Run Path Specialization Tests (33 tests)
```bash
./gradlew test --tests GuardPathSpecializationTest
```

### Run Rank Stats Tests (28 tests)
```bash
./gradlew test --tests GuardRankStatsTest
```

---

## Running All Tests

### Run Complete Test Suite (259 tests)
```bash
./gradlew test
```

### Run All Tests with Detailed Output
```bash
./gradlew test --info
```

### Run Tests and Generate HTML Report
```bash
./gradlew test
# Open: build/reports/tests/test/index.html
```

---

## Continuous Testing During Development

### Run Tests on File Change (Watch Mode)
```bash
./gradlew test --continuous
```

### Run Tests in Parallel (Faster)
```bash
./gradlew test --parallel
```

---

## Troubleshooting

### Clean and Rebuild Before Testing
```bash
./gradlew clean test
```

### Run Tests with Stack Traces on Failure
```bash
./gradlew test --stacktrace
```

### Run Tests with Full Debug Info
```bash
./gradlew test --debug
```

---

## Test Reports

### HTML Report Location
```
build/reports/tests/test/index.html
```

### XML Report Location (for CI/CD)
```
build/test-results/test/
```

### Problems Report
```
build/reports/problems/problems-report.html
```

---

## CI/CD Integration

### GitHub Actions Example
```yaml
- name: Run Tests
  run: ./gradlew test

- name: Upload Test Report
  if: always()
  uses: actions/upload-artifact@v3
  with:
    name: test-report
    path: build/reports/tests/test/
```

---

## Test File Locations

```
src/test/java/com/xeenaa/villagermanager/tests/
├── GuardAIGoalPriorityTest.java           (41 tests)
├── GuardCombatMechanicsTest.java          (31 tests)
├── GuardPathSpecializationTest.java       (33 tests)
├── GuardRankStatsTest.java                (28 tests)
└── GuardSpecialAbilitiesTest.java         (93 tests) ← NEW
```

---

## Quick Test Status Check

### Run Tests and Show Summary Only
```bash
./gradlew test --console=plain | grep -E "(PASSED|FAILED|BUILD)"
```

### Count Total Tests
```bash
./gradlew test 2>&1 | grep "PASSED" | wc -l
```

---

## Expected Output

When all tests pass, you should see:

```
BUILD SUCCESSFUL in Xs
259 tests completed, 259 PASSED
```

If any tests fail, you'll see:

```
FAILURE: Build failed with an exception.
X tests completed, Y PASSED, Z FAILED
```

---

## Performance Notes

- **Full Test Suite:** ~4-6 seconds
- **Single Test File:** ~2-3 seconds
- **Clean + Test:** ~8-10 seconds

---

## Best Practices

1. **Before Committing:** Always run `./gradlew test` to ensure all tests pass
2. **After Refactoring:** Run tests to catch regressions
3. **When Adding Features:** Write tests first, then implementation
4. **Before Releases:** Run full test suite with `--stacktrace` for detailed validation

---

## Test Coverage Goals

- All public methods should have tests ✓
- All edge cases should be covered ✓
- All user-validated features should have automated tests ✓
- Regression prevention for all bug fixes ✓

---

Current status: **259/259 tests passing** ✓
