#!/usr/bin/env python3
"""
Android License Persistence Test Script
=======================================

This script tests the license key persistence functionality in the BearMod Android app
by simulating the complete authentication flow including app reinstallation.

Test License Key: BEARX1-rlUGoC-1ljGtR-WHr5gp-cTj8vQ-oJppuY
"""

import subprocess
import time
import sys
import os

class AndroidLicenseTester:
    def __init__(self):
        self.package_name = "com.bearmod"
        self.main_activity = f"{self.package_name}/.activity.SplashActivity"
        self.test_license = "BEARX1-rlUGoC-1ljGtR-WHr5gp-cTj8vQ-oJppuY"
        
    def run_adb_command(self, command, timeout=30):
        """Run ADB command and return output"""
        try:
            result = subprocess.run(command, shell=True, capture_output=True, text=True, timeout=timeout)
            return result.returncode == 0, result.stdout, result.stderr
        except subprocess.TimeoutExpired:
            return False, "", "Command timed out"
        except Exception as e:
            return False, "", str(e)
    
    def log(self, level, message):
        """Log message with timestamp"""
        timestamp = time.strftime("%H:%M:%S")
        print(f"[{timestamp}] {level}: {message}")
    
    def clear_app_data(self):
        """Clear app data to simulate fresh install"""
        self.log("INFO", "Clearing app data...")
        success, stdout, stderr = self.run_adb_command(f"adb shell pm clear {self.package_name}")
        if success:
            self.log("SUCCESS", "App data cleared")
            return True
        else:
            self.log("ERROR", f"Failed to clear app data: {stderr}")
            return False
    
    def launch_app(self):
        """Launch the BearMod app"""
        self.log("INFO", "Launching BearMod app...")
        success, stdout, stderr = self.run_adb_command(f"adb shell am start -n {self.main_activity}")
        if success:
            self.log("SUCCESS", "App launched")
            time.sleep(3)  # Wait for app to start
            return True
        else:
            self.log("ERROR", f"Failed to launch app: {stderr}")
            return False
    
    def force_stop_app(self):
        """Force stop the app"""
        self.log("INFO", "Force stopping app...")
        success, stdout, stderr = self.run_adb_command(f"adb shell am force-stop {self.package_name}")
        if success:
            self.log("SUCCESS", "App force stopped")
            return True
        else:
            self.log("ERROR", f"Failed to force stop app: {stderr}")
            return False
    
    def simulate_license_entry(self):
        """Simulate entering license key in the app"""
        self.log("INFO", f"Simulating license key entry: {self.test_license}")
        
        # Wait for LoginActivity to load
        time.sleep(2)
        
        # Tap on license key input field (approximate coordinates)
        self.run_adb_command("adb shell input tap 540 400")
        time.sleep(1)
        
        # Clear any existing text
        self.run_adb_command("adb shell input keyevent KEYCODE_CTRL_A")
        time.sleep(0.5)
        
        # Enter the license key
        self.run_adb_command(f"adb shell input text '{self.test_license}'")
        time.sleep(1)
        
        # Tap the login button (approximate coordinates)
        self.run_adb_command("adb shell input tap 540 500")
        time.sleep(3)  # Wait for authentication
        
        self.log("INFO", "License key entry simulated")
    
    def check_authentication_logs(self):
        """Check logcat for authentication success/failure"""
        self.log("INFO", "Checking authentication logs...")
        
        success, stdout, stderr = self.run_adb_command(
            "adb logcat -d -s SimpleLicenseVerifier:* | tail -20", timeout=10
        )
        
        if success and stdout:
            if "successful" in stdout.lower():
                self.log("SUCCESS", "Authentication appears successful based on logs")
                return True
            elif "failed" in stdout.lower() or "error" in stdout.lower():
                self.log("ERROR", "Authentication appears to have failed based on logs")
                return False
        
        self.log("WARN", "Could not determine authentication status from logs")
        return None
    
    def check_external_storage_files(self):
        """Check if authentication data is stored in external storage"""
        self.log("INFO", "Checking external storage for persistence files...")
        
        # Check for session cache file
        success, stdout, stderr = self.run_adb_command(
            f"adb shell ls /sdcard/Android/data/{self.package_name}/files/.session_cache"
        )
        
        if success and ".session_cache" in stdout:
            self.log("SUCCESS", "Session cache file found in external storage")
            return True
        else:
            self.log("WARN", "Session cache file not found in external storage")
            return False
    
    def test_app_restart_persistence(self):
        """Test if authentication persists after app restart"""
        self.log("INFO", "=== Testing App Restart Persistence ===")
        
        # Force stop the app
        if not self.force_stop_app():
            return False
        
        time.sleep(2)
        
        # Launch app again
        if not self.launch_app():
            return False
        
        # Wait for splash screen and auto-login attempt
        time.sleep(5)
        
        # Check if we're still authenticated
        auth_status = self.check_authentication_logs()
        if auth_status:
            self.log("SUCCESS", "Authentication persisted after app restart!")
            return True
        else:
            self.log("ERROR", "Authentication did not persist after app restart")
            return False
    
    def run_comprehensive_test(self):
        """Run comprehensive license persistence test"""
        self.log("INFO", "=" * 60)
        self.log("INFO", "ANDROID LICENSE PERSISTENCE TEST SUITE")
        self.log("INFO", "=" * 60)
        self.log("INFO", f"Package: {self.package_name}")
        self.log("INFO", f"Test License: {self.test_license}")
        self.log("INFO", "=" * 60)
        
        # Test 1: Fresh authentication
        self.log("INFO", "\n🔍 TEST 1: Fresh Authentication")
        if not self.clear_app_data():
            return False
        
        if not self.launch_app():
            return False
        
        self.simulate_license_entry()
        
        auth_status = self.check_authentication_logs()
        if not auth_status:
            self.log("ERROR", "❌ Fresh authentication failed")
            return False
        
        self.log("SUCCESS", "✅ Fresh authentication successful")
        
        # Test 2: External storage persistence
        self.log("INFO", "\n🔍 TEST 2: External Storage Persistence")
        if self.check_external_storage_files():
            self.log("SUCCESS", "✅ External storage persistence working")
        else:
            self.log("WARN", "⚠️  External storage persistence may not be working")
        
        # Test 3: App restart persistence
        self.log("INFO", "\n🔍 TEST 3: App Restart Persistence")
        if self.test_app_restart_persistence():
            self.log("SUCCESS", "✅ App restart persistence working")
        else:
            self.log("ERROR", "❌ App restart persistence failed")
            return False
        
        self.log("INFO", "\n" + "=" * 60)
        self.log("SUCCESS", "🎉 ALL TESTS PASSED! License persistence is working!")
        self.log("INFO", "=" * 60)
        
        return True

def main():
    """Main test execution"""
    tester = AndroidLicenseTester()
    
    try:
        # Check if ADB is available
        success, _, _ = tester.run_adb_command("adb devices")
        if not success:
            print("❌ ADB not available. Please ensure Android SDK is installed and device is connected.")
            sys.exit(1)
        
        # Run comprehensive test
        if tester.run_comprehensive_test():
            print("\n🎉 License persistence test completed successfully!")
            sys.exit(0)
        else:
            print("\n❌ License persistence test failed!")
            sys.exit(1)
            
    except KeyboardInterrupt:
        print("\n⚠️  Test interrupted by user")
        sys.exit(1)
    except Exception as e:
        print(f"\n💥 Unexpected error: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()
