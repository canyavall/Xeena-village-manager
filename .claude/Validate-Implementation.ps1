# Implementation Validation Script for Xeenaa Villager Manager
# PowerShell version for Windows systems

param(
    [string]$ProjectRoot = $PWD,
    [switch]$Verbose
)

class ValidationResult {
    [string]$TestName
    [bool]$Passed
    [string]$Message
    [string[]]$Logs

    ValidationResult([string]$testName, [bool]$passed, [string]$message) {
        $this.TestName = $testName
        $this.Passed = $passed
        $this.Message = $message
        $this.Logs = @()
    }
}

function Test-CodeStructure {
    param([string]$ProjectRoot)

    $results = @()

    Write-Host "🔍 Validating Code Structure..." -ForegroundColor Cyan

    # Check ProfessionTab implementation
    $professionTabPath = Join-Path $ProjectRoot "src\client\java\com\xeenaa\villagermanager\client\gui\ProfessionTab.java"
    if (Test-Path $professionTabPath) {
        $content = Get-Content $professionTabPath -Raw

        # Check for profession filtering
        if ($content -match "Skip currently selected profession") {
            $results += [ValidationResult]::new("profession_filtering_code", $true, "✅ Current profession filtering logic found")
        } else {
            $results += [ValidationResult]::new("profession_filtering_code", $false, "❌ Current profession filtering logic missing")
        }

        # Check for window closing logic
        if ($content -match "parentScreen\.close\(\)") {
            $results += [ValidationResult]::new("window_closing_code", $true, "✅ Window closing logic found")
        } else {
            $results += [ValidationResult]::new("window_closing_code", $false, "❌ Window closing logic missing")
        }

        # Check for guard data identification
        if ($content -match "currentProfessionId") {
            $results += [ValidationResult]::new("profession_identification", $true, "✅ Current profession identification logic found")
        } else {
            $results += [ValidationResult]::new("profession_identification", $false, "❌ Current profession identification missing")
        }
    } else {
        $results += [ValidationResult]::new("profession_tab_exists", $false, "❌ ProfessionTab.java not found")
    }

    # Check TabbedManagementScreen implementation
    $tabbedScreenPath = Join-Path $ProjectRoot "src\client\java\com\xeenaa\villagermanager\client\gui\TabbedManagementScreen.java"
    if (Test-Path $tabbedScreenPath) {
        $content = Get-Content $tabbedScreenPath -Raw

        # Check for tab button recreation
        if (($content -match "createTabButtons\(\)") -and ($content -match "refreshTabs\(\)")) {
            $results += [ValidationResult]::new("tab_refresh_code", $true, "✅ Tab refresh and recreation logic found")
        } else {
            $results += [ValidationResult]::new("tab_refresh_code", $false, "❌ Tab refresh logic incomplete")
        }

        # Check for proper tab clearing
        if ($content -match "clearChildren\(\)") {
            $results += [ValidationResult]::new("tab_clearing_code", $true, "✅ Tab clearing logic found")
        } else {
            $results += [ValidationResult]::new("tab_clearing_code", $false, "❌ Tab clearing logic missing")
        }
    }

    return $results
}

function Test-TranslationSystem {
    param([string]$ProjectRoot)

    $results = @()

    Write-Host "🌐 Validating Translation System..." -ForegroundColor Cyan

    # Check language file
    $langPath = Join-Path $ProjectRoot "src\main\resources\assets\xeenaa_villager_manager\lang\en_us.json"
    if (Test-Path $langPath) {
        try {
            $translations = Get-Content $langPath -Raw | ConvertFrom-Json

            # Check for guard translations
            $guardTranslations = @(
                "entity.minecraft.villager.xeenaa_villager_manager.guard",
                "entity.minecraft.villager.guard"
            )

            $foundTranslations = 0
            foreach ($key in $guardTranslations) {
                if ($translations.PSObject.Properties.Name -contains $key) {
                    $foundTranslations++
                }
            }

            if ($foundTranslations -ge 1) {
                $results += [ValidationResult]::new("guard_translations", $true, "✅ Guard translations found ($foundTranslations/$($guardTranslations.Count))")
            } else {
                $results += [ValidationResult]::new("guard_translations", $false, "❌ Guard translations missing")
            }

            # Check for emerald loss translations
            $emeraldKeys = $translations.PSObject.Properties.Name | Where-Object { $_ -like "*emerald*loss*" }
            if ($emeraldKeys.Count -gt 0) {
                $results += [ValidationResult]::new("emerald_loss_translations", $true, "✅ Emerald loss translations found")
            } else {
                $results += [ValidationResult]::new("emerald_loss_translations", $false, "❌ Emerald loss translations missing")
            }

        } catch {
            $results += [ValidationResult]::new("lang_file_format", $false, "❌ Language file has invalid JSON format")
        }
    } else {
        $results += [ValidationResult]::new("lang_file_exists", $false, "❌ Language file not found")
    }

    return $results
}

function Test-LogicFlows {
    param([string]$ProjectRoot)

    $results = @()

    Write-Host "⚙️ Validating Logic Flows..." -ForegroundColor Cyan

    # Check ServerPacketHandler for emerald loss system
    $serverHandlerPath = Join-Path $ProjectRoot "src\main\java\com\xeenaa\villagermanager\network\ServerPacketHandler.java"
    if (Test-Path $serverHandlerPath) {
        $content = Get-Content $serverHandlerPath -Raw

        # Check for emerald loss logic (not refund)
        if (($content -match "emeralds.*lost") -and ($content -notmatch "refund")) {
            $results += [ValidationResult]::new("emerald_loss_system", $true, "✅ Emerald loss system implemented (no refunds)")
        } else {
            $results += [ValidationResult]::new("emerald_loss_system", $false, "❌ Emerald loss system not properly implemented")
        }

        # Check for guard data cleanup
        if (($content -match "guard data cleaned") -or ($content -match "removeGuardData")) {
            $results += [ValidationResult]::new("guard_data_cleanup", $true, "✅ Guard data cleanup implemented")
        } else {
            $results += [ValidationResult]::new("guard_data_cleanup", $false, "❌ Guard data cleanup missing")
        }

        # Check for profession change confirmation
        if ($content -match "confirmed.*profession.*change") {
            $results += [ValidationResult]::new("profession_change_confirmation", $true, "✅ Profession change confirmation logic found")
        } else {
            $results += [ValidationResult]::new("profession_change_confirmation", $false, "❌ Profession change confirmation missing")
        }
    }

    return $results
}

function Test-IntegrationLogs {
    param([string]$ProjectRoot)

    $results = @()

    Write-Host "📋 Validating Integration Logs..." -ForegroundColor Cyan

    # Look for recent test logs
    $testLogsDir = Join-Path $ProjectRoot "test_logs"
    if (Test-Path $testLogsDir) {
        $logFiles = Get-ChildItem $testLogsDir -Filter "*.log" | Sort-Object LastWriteTime -Descending

        if ($logFiles.Count -gt 0) {
            $latestLog = $logFiles[0]
            $content = Get-Content $latestLog.FullName -Raw

            # Define expected patterns
            $patterns = @{
                "guard_assignment" = "Guard profession selected.*refreshing tabs"
                "tab_creation" = "Initialized VillagerManagementScreen with 2 tabs"
                "rank_switch" = "Successfully switched to RankTab"
                "guard_removal" = "guard data cleaned up"
                "emerald_loss" = "emeralds.*lost.*penalty"
                "profession_filtering" = "Loaded.*available professions"
                "window_close" = "Successfully processed.*profession change"
            }

            $patternsFound = 0
            foreach ($patternName in $patterns.Keys) {
                if ($content -match $patterns[$patternName]) {
                    $patternsFound++
                    if ($Verbose) {
                        Write-Host "  ✓ Found pattern: $patternName" -ForegroundColor Green
                    }
                }
            }

            $successRate = [math]::Round(($patternsFound / $patterns.Count) * 100, 1)

            if ($patternsFound -ge ($patterns.Count * 0.7)) {  # 70% threshold
                $results += [ValidationResult]::new("integration_patterns", $true, "✅ Integration patterns found ($patternsFound/$($patterns.Count)) - $successRate%")
            } else {
                $results += [ValidationResult]::new("integration_patterns", $false, "❌ Integration patterns insufficient ($patternsFound/$($patterns.Count)) - $successRate%")
            }
        } else {
            $results += [ValidationResult]::new("test_logs_exist", $false, "⚠️ No test logs found for integration validation")
        }
    } else {
        $results += [ValidationResult]::new("test_logs_dir", $false, "⚠️ Test logs directory not found")
    }

    return $results
}

function Generate-Report {
    param([ValidationResult[]]$Results)

    $totalTests = $Results.Count
    $passedTests = ($Results | Where-Object { $_.Passed }).Count
    $failedTests = $totalTests - $passedTests
    $successRate = if ($totalTests -gt 0) { [math]::Round(($passedTests / $totalTests) * 100, 1) } else { 0 }

    $report = @"
# Implementation Validation Report
Generated: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')

## Summary
- **Total Tests**: $totalTests
- **Passed**: $passedTests ✅
- **Failed**: $failedTests ❌
- **Success Rate**: $successRate%

## Test Results

"@

    # Group results by category
    $categories = @{
        "Code Structure" = $Results | Where-Object { $_.TestName -like "*code*" -or $_.TestName -like "*logic*" }
        "Translation System" = $Results | Where-Object { $_.TestName -like "*translation*" -or $_.TestName -like "*lang*" }
        "Logic Flows" = $Results | Where-Object { $_.TestName -like "*system*" -or $_.TestName -like "*cleanup*" -or $_.TestName -like "*confirmation*" }
        "Integration" = $Results | Where-Object { $_.TestName -like "*integration*" -or $_.TestName -like "*logs*" }
    }

    foreach ($category in $categories.Keys) {
        $catResults = $categories[$category]
        if ($catResults.Count -gt 0) {
            $report += "`n### $category`n"
            foreach ($result in $catResults) {
                $report += "- **$($result.TestName)**: $($result.Message)`n"
            }
            $report += "`n"
        }
    }

    # Add recommendations
    if ($failedTests -gt 0) {
        $report += "## ⚠️ Action Required`n"
        $report += "Some validation tests failed. Please review the failed items above.`n`n"

        $failedItems = $Results | Where-Object { -not $_.Passed }
        $report += "### Failed Tests:`n"
        foreach ($failed in $failedItems) {
            $report += "- $($failed.TestName): $($failed.Message)`n"
        }
        $report += "`n"
    } else {
        $report += "## ✅ All Validations Passed`n"
        $report += "Implementation appears stable and feature-complete.`n`n"
    }

    return $report
}

# Main execution
Write-Host "🚀 Starting Implementation Validation..." -ForegroundColor Yellow
Write-Host "Project Root: $ProjectRoot" -ForegroundColor Gray
Write-Host "=" * 60

$allResults = @()

# Run all validation tests
$allResults += Test-CodeStructure -ProjectRoot $ProjectRoot
$allResults += Test-TranslationSystem -ProjectRoot $ProjectRoot
$allResults += Test-LogicFlows -ProjectRoot $ProjectRoot
$allResults += Test-IntegrationLogs -ProjectRoot $ProjectRoot

# Generate report
$report = Generate-Report -Results $allResults

# Save report
$reportPath = Join-Path $ProjectRoot ".claude\validation_report.md"
$reportDir = Split-Path $reportPath -Parent
if (-not (Test-Path $reportDir)) {
    New-Item -ItemType Directory -Path $reportDir -Force | Out-Null
}
$report | Out-File -FilePath $reportPath -Encoding UTF8

# Display summary
Write-Host "`n" + ("=" * 60)
Write-Host "VALIDATION COMPLETE" -ForegroundColor Yellow
Write-Host "=" * 60

# Display results summary
$totalTests = $allResults.Count
$passedTests = ($allResults | Where-Object { $_.Passed }).Count
$failedTests = $totalTests - $passedTests

Write-Host "📊 Summary:" -ForegroundColor Cyan
Write-Host "  Total Tests: $totalTests"
Write-Host "  Passed: $passedTests" -ForegroundColor Green
Write-Host "  Failed: $failedTests" -ForegroundColor $(if ($failedTests -eq 0) { "Green" } else { "Red" })
Write-Host "  Success Rate: $([math]::Round(($passedTests / $totalTests) * 100, 1))%"

Write-Host "`n📄 Full report saved to: $reportPath" -ForegroundColor Gray

if ($Verbose) {
    Write-Host "`n📋 Detailed Results:" -ForegroundColor Cyan
    foreach ($result in $allResults) {
        $color = if ($result.Passed) { "Green" } else { "Red" }
        Write-Host "  $($result.Message)" -ForegroundColor $color
    }
}

# Return appropriate exit code
if ($failedTests -eq 0) {
    Write-Host "`n✅ All tests passed!" -ForegroundColor Green
    exit 0
} else {
    Write-Host "`n❌ $failedTests test(s) failed. Please review the report." -ForegroundColor Red
    exit 1
}