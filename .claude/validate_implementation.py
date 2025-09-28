#!/usr/bin/env python3
"""
Implementation Validation Script for Xeenaa Villager Manager

This script validates that all profession management features work correctly
and haven't been broken by recent changes.
"""

import re
import subprocess
import time
import json
from pathlib import Path
from typing import Dict, List, Tuple, Optional

class ValidationResult:
    def __init__(self, test_name: str, passed: bool, message: str, logs: List[str] = None):
        self.test_name = test_name
        self.passed = passed
        self.message = message
        self.logs = logs or []

class ImplementationValidator:
    def __init__(self, project_root: str):
        self.project_root = Path(project_root)
        self.log_patterns = {
            # Profession Management Patterns
            "profession_filtering": r"Loaded \d+ available professions",
            "current_profession_excluded": r"Skip currently selected profession",

            # Guard Assignment Patterns
            "guard_assignment": r"Guard profession selected - refreshing tabs",
            "tab_refresh": r"Initialized VillagerManagementScreen with 2 tabs",
            "rank_tab_switch": r"Successfully switched to RankTab",
            "guard_data_creation": r"Created and synced guard data",

            # Guard Removal Patterns
            "guard_emerald_warning": r"Guard profession change requested.*warning about emerald loss",
            "emerald_loss_processing": r"Player .* lost \d+ emeralds as penalty",
            "guard_data_cleanup": r"guard data cleaned up",
            "window_close_after_guard_change": r"Successfully processed guard profession change",

            # Tab Navigation Patterns
            "tab_button_creation": r"createTabButtons",
            "tab_visibility": r"Initialized VillagerManagementScreen with \d+ tabs",

            # Translation Patterns
            "guard_translation": r"Display: 'Guard'",
            "profession_registry": r"=== PROFESSION REGISTRY DETAILS ===",

            # Error Prevention Patterns
            "no_duplicate_professions": r"Skip blacklisted professions",
            "server_sync": r"Successfully changed villager \d+ profession"
        }

        self.expected_behaviors = {
            "guard_to_farmer_flow": [
                "Guard profession change requested",
                "emeralds will be lost",
                "guard data cleaned up",
                "Successfully processed guard profession change"
            ],
            "farmer_to_guard_flow": [
                "Guard profession selected",
                "refreshing tabs",
                "Initialized VillagerManagementScreen with 2 tabs",
                "Successfully switched to RankTab"
            ],
            "profession_list_filtering": [
                "Loaded.*available professions",
                "Skip currently selected profession"
            ]
        }

    def run_validation_tests(self) -> List[ValidationResult]:
        """Run comprehensive validation tests"""
        results = []

        print("🔍 Starting Implementation Validation...")
        print("=" * 60)

        # Test 1: Code Structure Validation
        results.extend(self._validate_code_structure())

        # Test 2: Translation System Validation
        results.extend(self._validate_translation_system())

        # Test 3: Logic Flow Validation
        results.extend(self._validate_logic_flows())

        # Test 4: Integration Test (if possible)
        results.extend(self._validate_integration())

        return results

    def _validate_code_structure(self) -> List[ValidationResult]:
        """Validate that code structure supports our features"""
        results = []

        # Check profession filtering implementation
        profession_tab = self.project_root / "src/client/java/com/xeenaa/villagermanager/client/gui/ProfessionTab.java"
        if profession_tab.exists():
            content = profession_tab.read_text()

            # Check for current profession filtering
            if "Skip currently selected profession" in content:
                results.append(ValidationResult(
                    "profession_filtering_code",
                    True,
                    "✅ Current profession filtering logic found in ProfessionTab"
                ))
            else:
                results.append(ValidationResult(
                    "profession_filtering_code",
                    False,
                    "❌ Current profession filtering logic missing"
                ))

            # Check for window closing logic
            if "parentScreen.close()" in content:
                results.append(ValidationResult(
                    "window_closing_code",
                    True,
                    "✅ Window closing logic found in profession selection"
                ))
            else:
                results.append(ValidationResult(
                    "window_closing_code",
                    False,
                    "❌ Window closing logic missing"
                ))
        else:
            results.append(ValidationResult(
                "profession_tab_exists",
                False,
                "❌ ProfessionTab.java not found"
            ))

        # Check tab management implementation
        tabbed_screen = self.project_root / "src/client/java/com/xeenaa/villagermanager/client/gui/TabbedManagementScreen.java"
        if tabbed_screen.exists():
            content = tabbed_screen.read_text()

            # Check for tab button recreation
            if "createTabButtons()" in content and "refreshTabs()" in content:
                results.append(ValidationResult(
                    "tab_refresh_code",
                    True,
                    "✅ Tab refresh and recreation logic found"
                ))
            else:
                results.append(ValidationResult(
                    "tab_refresh_code",
                    False,
                    "❌ Tab refresh logic incomplete"
                ))

        return results

    def _validate_translation_system(self) -> List[ValidationResult]:
        """Validate translation system"""
        results = []

        # Check language file
        lang_file = self.project_root / "src/main/resources/assets/xeenaa_villager_manager/lang/en_us.json"
        if lang_file.exists():
            try:
                with open(lang_file, 'r') as f:
                    translations = json.load(f)

                # Check for guard translations
                guard_translations = [
                    "entity.minecraft.villager.xeenaa_villager_manager.guard",
                    "entity.minecraft.villager.guard"
                ]

                found_translations = sum(1 for key in guard_translations if key in translations)

                if found_translations >= 1:
                    results.append(ValidationResult(
                        "guard_translations",
                        True,
                        f"✅ Guard translations found ({found_translations}/{len(guard_translations)})"
                    ))
                else:
                    results.append(ValidationResult(
                        "guard_translations",
                        False,
                        "❌ Guard translations missing"
                    ))

            except json.JSONDecodeError:
                results.append(ValidationResult(
                    "lang_file_format",
                    False,
                    "❌ Language file has invalid JSON format"
                ))
        else:
            results.append(ValidationResult(
                "lang_file_exists",
                False,
                "❌ Language file not found"
            ))

        return results

    def _validate_logic_flows(self) -> List[ValidationResult]:
        """Validate logic flow implementations"""
        results = []

        # Check for emerald loss system
        server_handler = self.project_root / "src/main/java/com/xeenaa/villagermanager/network/ServerPacketHandler.java"
        if server_handler.exists():
            content = server_handler.read_text()

            # Check for emerald loss logic (not refund)
            if "emeralds will be lost" in content and "refund" not in content.lower():
                results.append(ValidationResult(
                    "emerald_loss_system",
                    True,
                    "✅ Emerald loss system implemented (no refunds)"
                ))
            else:
                results.append(ValidationResult(
                    "emerald_loss_system",
                    False,
                    "❌ Emerald loss system not properly implemented"
                ))

        # Check guard data cleanup
        if server_handler.exists():
            content = server_handler.read_text()
            if "guard data cleaned up" in content or "removeGuardData" in content:
                results.append(ValidationResult(
                    "guard_data_cleanup",
                    True,
                    "✅ Guard data cleanup implemented"
                ))
            else:
                results.append(ValidationResult(
                    "guard_data_cleanup",
                    False,
                    "❌ Guard data cleanup missing"
                ))

        return results

    def _validate_integration(self) -> List[ValidationResult]:
        """Validate integration by checking recent test logs"""
        results = []

        # Look for recent test logs
        test_logs_dir = self.project_root / "test_logs"
        if test_logs_dir.exists():
            log_files = list(test_logs_dir.glob("*.log"))
            if log_files:
                # Read most recent log
                latest_log = max(log_files, key=lambda p: p.stat().st_mtime)
                content = latest_log.read_text()

                # Validate expected patterns
                patterns_found = 0
                for pattern_name, pattern in self.log_patterns.items():
                    if re.search(pattern, content):
                        patterns_found += 1

                if patterns_found >= len(self.log_patterns) * 0.7:  # 70% threshold
                    results.append(ValidationResult(
                        "integration_patterns",
                        True,
                        f"✅ Integration test patterns found ({patterns_found}/{len(self.log_patterns)})"
                    ))
                else:
                    results.append(ValidationResult(
                        "integration_patterns",
                        False,
                        f"❌ Integration test patterns insufficient ({patterns_found}/{len(self.log_patterns)})"
                    ))
            else:
                results.append(ValidationResult(
                    "test_logs_exist",
                    False,
                    "⚠️ No test logs found for integration validation"
                ))

        return results

    def generate_report(self, results: List[ValidationResult]) -> str:
        """Generate a comprehensive validation report"""
        total_tests = len(results)
        passed_tests = sum(1 for r in results if r.passed)
        failed_tests = total_tests - passed_tests

        report = f"""
# Implementation Validation Report
Generated: {time.strftime('%Y-%m-%d %H:%M:%S')}

## Summary
- **Total Tests**: {total_tests}
- **Passed**: {passed_tests} ✅
- **Failed**: {failed_tests} ❌
- **Success Rate**: {(passed_tests/total_tests*100):.1f}%

## Test Results

"""

        # Group results by category
        categories = {
            "Code Structure": [r for r in results if "code" in r.test_name],
            "Translation System": [r for r in results if "translation" in r.test_name or "lang" in r.test_name],
            "Logic Flows": [r for r in results if "system" in r.test_name or "cleanup" in r.test_name],
            "Integration": [r for r in results if "integration" in r.test_name or "logs" in r.test_name]
        }

        for category, cat_results in categories.items():
            if cat_results:
                report += f"### {category}\n"
                for result in cat_results:
                    report += f"- **{result.test_name}**: {result.message}\n"
                report += "\n"

        # Add recommendations
        if failed_tests > 0:
            report += "## ⚠️ Action Required\n"
            report += "Some validation tests failed. Please review the failed items above.\n\n"
        else:
            report += "## ✅ All Validations Passed\n"
            report += "Implementation appears stable and feature-complete.\n\n"

        return report

def main():
    # Determine project root
    project_root = Path.cwd()

    # Run validation
    validator = ImplementationValidator(str(project_root))
    results = validator.run_validation_tests()

    # Generate and save report
    report = validator.generate_report(results)

    # Save report
    report_file = project_root / ".claude" / "validation_report.md"
    report_file.parent.mkdir(exist_ok=True)
    report_file.write_text(report)

    # Print summary
    print("\n" + "=" * 60)
    print("VALIDATION COMPLETE")
    print("=" * 60)
    print(report)
    print(f"📄 Full report saved to: {report_file}")

    # Return exit code based on results
    failed_count = sum(1 for r in results if not r.passed)
    return min(failed_count, 1)  # Return 0 for success, 1 for failure

if __name__ == "__main__":
    exit(main())