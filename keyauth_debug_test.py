#!/usr/bin/env python3
"""
KeyAuth API Debug Test Script for BearMod
=========================================

This script validates and debugs the KeyAuth API authentication flow
to identify issues with the Android implementation before fixing them.

Based on the Android SimpleLicenseVerifier.java configuration:
- APP_NAME: "com.bearmod"
- OWNER_ID: "yLoA9zcOEF"
- VERSION: "1.3"
- APP_HASH: "4f9b15598f6e8bdf07ca39e9914cd3e9"
- API_URL: "https://keyauth.win/api/1.3/"

Test License Key: BEARX1-rlUGoC-1ljGtR-WHr5gp-cTj8vQ-oJppuY
"""

import requests
import hashlib
import platform
import json
import time
import os
import sys
from datetime import datetime

class KeyAuthDebugger:
    def __init__(self):
        # KeyAuth Configuration (from Android SimpleLicenseVerifier.java)
        self.APP_NAME = "com.bearmod"
        self.OWNER_ID = "yLoA9zcOEF"
        self.VERSION = "1.3"
        self.APP_HASH = "4f9b15598f6e8bdf07ca39e9914cd3e9"
        self.API_URL = "https://keyauth.win/api/1.3/"
        
        # Test License Key
        self.TEST_LICENSE_KEY = "BEARX1-rlUGoC-1ljGtR-WHr5gp-cTj8vQ-oJppuY"
        
        # Session Management
        self.session_id = None
        self.auth_token = None
        self.user_expiration = None
        self.is_authenticated = False
        
        # HTTP Session for connection reuse
        self.session = requests.Session()
        self.session.headers.update({
            'User-Agent': 'BearMod/1.0',
            'Content-Type': 'application/x-www-form-urlencoded'
        })
        
        # Debug logging
        self.debug_log = []
        
    def log(self, level, message):
        """Enhanced logging with timestamps"""
        timestamp = datetime.now().strftime("%H:%M:%S.%f")[:-3]
        log_entry = f"[{timestamp}] {level}: {message}"
        print(log_entry)
        self.debug_log.append(log_entry)
        
    def generate_hwid(self):
        """
        Generate Hardware ID similar to Android HWID.getHWID()
        Uses stable system identifiers that don't change
        """
        try:
            # Combine system properties similar to Android implementation
            system_info = (
                platform.system() +
                platform.machine() +
                platform.processor() +
                str(os.cpu_count()) +
                platform.node()
            )
            
            # Generate MD5 hash (same as Android implementation)
            hwid_hash = hashlib.md5(system_info.encode()).hexdigest().upper()
            self.log("DEBUG", f"Generated HWID: {hwid_hash}")
            return hwid_hash
            
        except Exception as e:
            self.log("ERROR", f"HWID generation failed: {e}")
            # Fallback HWID
            fallback = hashlib.md5("FALLBACK_HWID".encode()).hexdigest().upper()
            self.log("WARN", f"Using fallback HWID: {fallback}")
            return fallback
    
    def make_api_request(self, request_type, additional_params=None):
        """
        Make KeyAuth API request with comprehensive logging
        """
        # Base parameters (always required)
        params = {
            'type': request_type,
            'name': self.APP_NAME,
            'ownerid': self.OWNER_ID,
            'ver': self.VERSION
        }
        
        # Add hash for init requests
        if request_type == 'init':
            params['hash'] = self.APP_HASH
            
        # Add session ID for authenticated requests
        if request_type != 'init' and self.session_id:
            params['sessionid'] = self.session_id
            
        # Add additional parameters
        if additional_params:
            params.update(additional_params)
            
        self.log("DEBUG", f"API Request [{request_type}]: {json.dumps(params, indent=2)}")
        
        try:
            response = self.session.post(self.API_URL, data=params, timeout=10)
            
            self.log("INFO", f"HTTP Response: {response.status_code}")
            
            if response.status_code == 200:
                response_text = response.text
                self.log("DEBUG", f"Response Body: {response_text}")
                
                try:
                    response_json = json.loads(response_text)
                    return True, response_json
                except json.JSONDecodeError:
                    self.log("ERROR", "Invalid JSON response")
                    return False, {"error": "Invalid JSON response", "raw": response_text}
            else:
                self.log("ERROR", f"HTTP Error: {response.status_code}")
                return False, {"error": f"HTTP {response.status_code}", "raw": response.text}
                
        except requests.exceptions.Timeout:
            self.log("ERROR", "Request timeout")
            return False, {"error": "Request timeout"}
        except requests.exceptions.RequestException as e:
            self.log("ERROR", f"Request failed: {e}")
            return False, {"error": str(e)}
    
    def initialize_keyauth(self):
        """
        Step 1: Initialize KeyAuth application
        """
        self.log("INFO", "=== STEP 1: KeyAuth Initialization ===")
        
        success, response = self.make_api_request('init')
        
        if success and response.get('success'):
            # Extract session ID
            self.session_id = response.get('sessionid')
            if self.session_id:
                self.log("SUCCESS", f"KeyAuth initialized successfully")
                self.log("INFO", f"Session ID: {self.session_id[:8]}...")
                return True
            else:
                self.log("ERROR", "No session ID in response")
                return False
        else:
            error_msg = response.get('message', 'Unknown error')
            self.log("ERROR", f"KeyAuth initialization failed: {error_msg}")
            return False
    
    def verify_license(self, license_key=None):
        """
        Step 2: Verify license key with KeyAuth
        """
        if not license_key:
            license_key = self.TEST_LICENSE_KEY
            
        self.log("INFO", "=== STEP 2: License Verification ===")
        self.log("INFO", f"Testing license key: {license_key}")
        
        if not self.session_id:
            self.log("ERROR", "No session ID - initialization required first")
            return False
            
        hwid = self.generate_hwid()
        
        additional_params = {
            'key': license_key,
            'hwid': hwid
        }
        
        success, response = self.make_api_request('license', additional_params)
        
        if success and response.get('success'):
            self.log("SUCCESS", "License verification successful!")
            
            # Extract user data
            if 'info' in response:
                user_info = response['info']
                self.log("INFO", f"User Info: {json.dumps(user_info, indent=2)}")
                
                # Extract expiration if available
                if 'subscriptions' in user_info and user_info['subscriptions']:
                    sub = user_info['subscriptions'][0]
                    if 'expiry' in sub:
                        expiry_timestamp = sub['expiry']
                        expiry_date = datetime.fromtimestamp(int(expiry_timestamp))
                        self.user_expiration = expiry_date.strftime("%Y-%m-%d %H:%M:%S")
                        self.log("INFO", f"License expires: {self.user_expiration}")
            
            self.is_authenticated = True
            return True
        else:
            error_msg = response.get('message', 'Unknown error')
            self.log("ERROR", f"License verification failed: {error_msg}")
            return False

    def validate_session(self):
        """
        Step 3: Validate current session (simulates app restart)
        """
        self.log("INFO", "=== STEP 3: Session Validation ===")

        if not self.session_id:
            self.log("ERROR", "No session ID to validate")
            return False

        success, response = self.make_api_request('check')

        if success and response.get('success'):
            self.log("SUCCESS", "Session validation successful!")
            return True
        else:
            error_msg = response.get('message', 'Unknown error')
            self.log("ERROR", f"Session validation failed: {error_msg}")
            return False

    def simulate_app_restart(self):
        """
        Simulate app restart by clearing runtime state but keeping session
        """
        self.log("INFO", "=== SIMULATING APP RESTART ===")

        # Save session data (what would be stored persistently)
        saved_session_id = self.session_id
        saved_auth_token = self.auth_token
        saved_expiration = self.user_expiration

        # Clear runtime state (what happens on app restart)
        self.is_authenticated = False

        self.log("INFO", "Runtime state cleared, attempting to restore from saved data...")

        # Restore saved data
        self.session_id = saved_session_id
        self.auth_token = saved_auth_token
        self.user_expiration = saved_expiration

        if self.session_id:
            self.log("INFO", f"Restored session ID: {self.session_id[:8]}...")
            return True
        else:
            self.log("ERROR", "No session data to restore")
            return False

    def test_persistence_flow(self):
        """
        Test the complete persistence flow that should work after app reinstallation
        """
        self.log("INFO", "=== TESTING PERSISTENCE FLOW ===")

        # Step 1: Fresh authentication
        if not self.initialize_keyauth():
            return False

        if not self.verify_license():
            return False

        # Step 2: Simulate app restart
        if not self.simulate_app_restart():
            return False

        # Step 3: Validate session after restart
        if not self.validate_session():
            self.log("ERROR", "Session validation failed after app restart!")
            return False

        self.log("SUCCESS", "Persistence flow test completed successfully!")
        return True

    def run_comprehensive_test(self):
        """
        Run comprehensive KeyAuth API test suite
        """
        self.log("INFO", "=" * 60)
        self.log("INFO", "KEYAUTH API COMPREHENSIVE TEST SUITE")
        self.log("INFO", "=" * 60)
        self.log("INFO", f"API URL: {self.API_URL}")
        self.log("INFO", f"App Name: {self.APP_NAME}")
        self.log("INFO", f"Owner ID: {self.OWNER_ID}")
        self.log("INFO", f"Version: {self.VERSION}")
        self.log("INFO", f"App Hash: {self.APP_HASH}")
        self.log("INFO", f"Test License: {self.TEST_LICENSE_KEY}")
        self.log("INFO", "=" * 60)

        # Test 1: Basic authentication flow
        self.log("INFO", "\n🔍 TEST 1: Basic Authentication Flow")
        if not self.initialize_keyauth():
            self.log("ERROR", "❌ Basic authentication flow failed at initialization")
            return False

        if not self.verify_license():
            self.log("ERROR", "❌ Basic authentication flow failed at license verification")
            return False

        self.log("SUCCESS", "✅ Basic authentication flow successful")

        # Test 2: Session validation
        self.log("INFO", "\n🔍 TEST 2: Session Validation")
        if not self.validate_session():
            self.log("ERROR", "❌ Session validation failed")
            return False

        self.log("SUCCESS", "✅ Session validation successful")

        # Test 3: App restart simulation
        self.log("INFO", "\n🔍 TEST 3: App Restart Simulation")
        if not self.simulate_app_restart():
            self.log("ERROR", "❌ App restart simulation failed")
            return False

        # Re-validate session after restart
        if not self.validate_session():
            self.log("ERROR", "❌ Session validation failed after app restart")
            return False

        self.log("SUCCESS", "✅ App restart simulation successful")

        # Test 4: HWID consistency
        self.log("INFO", "\n🔍 TEST 4: HWID Consistency")
        hwid1 = self.generate_hwid()
        hwid2 = self.generate_hwid()

        if hwid1 == hwid2:
            self.log("SUCCESS", "✅ HWID generation is consistent")
        else:
            self.log("ERROR", f"❌ HWID inconsistency: {hwid1} != {hwid2}")
            return False

        self.log("INFO", "\n" + "=" * 60)
        self.log("SUCCESS", "🎉 ALL TESTS PASSED! KeyAuth API flow is working correctly.")
        self.log("INFO", "=" * 60)

        return True

    def save_debug_log(self, filename="keyauth_debug.log"):
        """
        Save debug log to file for analysis
        """
        try:
            with open(filename, 'w') as f:
                f.write("KeyAuth API Debug Log\n")
                f.write("=" * 50 + "\n")
                f.write(f"Generated: {datetime.now()}\n")
                f.write("=" * 50 + "\n\n")

                for entry in self.debug_log:
                    f.write(entry + "\n")

            self.log("INFO", f"Debug log saved to: {filename}")
        except Exception as e:
            self.log("ERROR", f"Failed to save debug log: {e}")

def main():
    """
    Main test execution
    """
    debugger = KeyAuthDebugger()

    try:
        # Run comprehensive test suite
        success = debugger.run_comprehensive_test()

        # Save debug log
        debugger.save_debug_log()

        if success:
            print("\n🎉 KeyAuth API test completed successfully!")
            print("The authentication flow is working correctly.")
            print("Check keyauth_debug.log for detailed logs.")
            sys.exit(0)
        else:
            print("\n❌ KeyAuth API test failed!")
            print("Check keyauth_debug.log for detailed error analysis.")
            sys.exit(1)

    except KeyboardInterrupt:
        print("\n⚠️  Test interrupted by user")
        debugger.save_debug_log()
        sys.exit(1)
    except Exception as e:
        print(f"\n💥 Unexpected error: {e}")
        debugger.save_debug_log()
        sys.exit(1)

if __name__ == "__main__":
    main()
