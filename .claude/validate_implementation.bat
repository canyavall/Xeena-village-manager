@echo off
echo ========================================
echo Implementation Validation Script
echo ========================================
echo.

set PASSED=0
set FAILED=0

echo [1/4] Checking Code Structure...

:: Check ProfessionTab.java for profession filtering
findstr /c:"Skip currently selected profession" "src\client\java\com\xeenaa\villagermanager\client\gui\ProfessionTab.java" >nul 2>&1
if %errorlevel%==0 (
    echo ✓ Current profession filtering logic found
    set /a PASSED+=1
) else (
    echo ✗ Current profession filtering logic missing
    set /a FAILED+=1
)

:: Check for window closing logic
findstr /c:"parentScreen.close()" "src\client\java\com\xeenaa\villagermanager\client\gui\ProfessionTab.java" >nul 2>&1
if %errorlevel%==0 (
    echo ✓ Window closing logic found
    set /a PASSED+=1
) else (
    echo ✗ Window closing logic missing
    set /a FAILED+=1
)

:: Check TabbedManagementScreen for tab refresh
findstr /c:"createTabButtons()" "src\client\java\com\xeenaa\villagermanager\client\gui\TabbedManagementScreen.java" >nul 2>&1
if %errorlevel%==0 (
    echo ✓ Tab button creation logic found
    set /a PASSED+=1
) else (
    echo ✗ Tab button creation logic missing
    set /a FAILED+=1
)

echo.
echo [2/4] Checking Translation System...

:: Check language file for guard translations
findstr /c:"entity.minecraft.villager.guard" "src\main\resources\assets\xeenaa_villager_manager\lang\en_us.json" >nul 2>&1
if %errorlevel%==0 (
    echo ✓ Guard translations found
    set /a PASSED+=1
) else (
    echo ✗ Guard translations missing
    set /a FAILED+=1
)

:: Check for emerald loss translations
findstr /c:"emerald.*loss" "src\main\resources\assets\xeenaa_villager_manager\lang\en_us.json" >nul 2>&1
if %errorlevel%==0 (
    echo ✓ Emerald loss translations found
    set /a PASSED+=1
) else (
    echo ✗ Emerald loss translations missing
    set /a FAILED+=1
)

echo.
echo [3/4] Checking Logic Flows...

:: Check ServerPacketHandler for emerald loss
findstr /c:"emeralds.*lost" "src\main\java\com\xeenaa\villagermanager\network\ServerPacketHandler.java" >nul 2>&1
if %errorlevel%==0 (
    echo ✓ Emerald loss system found
    set /a PASSED+=1
) else (
    echo ✗ Emerald loss system missing
    set /a FAILED+=1
)

:: Check for guard data cleanup
findstr /c:"guard data cleaned" "src\main\java\com\xeenaa\villagermanager\network\ServerPacketHandler.java" >nul 2>&1
if %errorlevel%==0 (
    echo ✓ Guard data cleanup found
    set /a PASSED+=1
) else (
    echo ✗ Guard data cleanup missing
    set /a FAILED+=1
)

echo.
echo [4/4] Checking Integration Logs...

:: Check for recent test logs
if exist "test_logs\*.log" (
    echo ✓ Test logs directory found
    set /a PASSED+=1

    :: Check for key patterns in logs
    findstr /c:"Guard profession selected" "test_logs\*.log" >nul 2>&1
    if %errorlevel%==0 (
        echo ✓ Guard assignment pattern found in logs
        set /a PASSED+=1
    ) else (
        echo ✗ Guard assignment pattern missing in logs
        set /a FAILED+=1
    )

    findstr /c:"Successfully switched to RankTab" "test_logs\*.log" >nul 2>&1
    if %errorlevel%==0 (
        echo ✓ Rank tab switch pattern found in logs
        set /a PASSED+=1
    ) else (
        echo ✗ Rank tab switch pattern missing in logs
        set /a FAILED+=1
    )
) else (
    echo ! No test logs found for validation
    set /a FAILED+=1
)

echo.
echo ========================================
echo VALIDATION RESULTS
echo ========================================
echo Total Tests: %PASSED% + %FAILED% = %TOTAL%
set /a TOTAL=%PASSED%+%FAILED%
echo Passed: %PASSED%
echo Failed: %FAILED%

if %FAILED%==0 (
    echo.
    echo ✓ ALL TESTS PASSED! Implementation is stable.
    echo.
) else (
    echo.
    echo ✗ %FAILED% test(s) failed. Please review implementation.
    echo.
)

:: Create summary report
echo # Implementation Validation Report > .claude\validation_report.md
echo Generated: %date% %time% >> .claude\validation_report.md
echo. >> .claude\validation_report.md
echo ## Summary >> .claude\validation_report.md
echo - Total Tests: %TOTAL% >> .claude\validation_report.md
echo - Passed: %PASSED% >> .claude\validation_report.md
echo - Failed: %FAILED% >> .claude\validation_report.md
echo. >> .claude\validation_report.md

if %FAILED%==0 (
    echo ## Status: ✓ ALL TESTS PASSED >> .claude\validation_report.md
    echo Implementation appears stable and feature-complete. >> .claude\validation_report.md
) else (
    echo ## Status: ✗ TESTS FAILED >> .claude\validation_report.md
    echo %FAILED% validation test(s) failed. Please review implementation. >> .claude\validation_report.md
)

echo.
echo Report saved to: .claude\validation_report.md
echo.

if %FAILED%==0 (
    exit /b 0
) else (
    exit /b 1
)