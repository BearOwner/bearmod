#!/usr/bin/env python3
"""
JNI Validator for BearMod Project
==================================
This script validates JNI implementations and identifies missing or incomplete JNI functions.
"""

import os
import re
import sys
from typing import Dict, List, Tuple
from dataclasses import dataclass
from pathlib import Path

@dataclass
class JNIMethod:
    """Represents a JNI method declaration or implementation"""
    name: str
    signature: str
    return_type: str
    parameters: List[str]
    file_path: str
    line_number: int
    is_implementation: bool = False

@dataclass
class JNIAnalysis:
    """Analysis results for JNI methods"""
    declared_methods: Dict[str, JNIMethod] = None
    implemented_methods: Dict[str, JNIMethod] = None
    missing_implementations: List[str] = None
    missing_declarations: List[str] = None
    signature_mismatches: List[Tuple[str, str]] = None

class JNIValidator:
    def __init__(self, project_root: str):
        self.project_root = Path(project_root)
        self.java_files = []
        self.cpp_files = []
        self.header_files = []
        
        # Regex patterns
        self.java_native_pattern = re.compile(r'native\s+([\w<>\[\]]+)\s+(\w+)\s*\(([^)]*)\)')
        self.cpp_jni_pattern = re.compile(r'Java_([\w_]+)')
        self.cpp_jniexport_pattern = re.compile(r'JNIEXPORT\s+([\w<>\[\]]+)\s+JNICALL\s+Java_([\w_]+)')
        
    def scan_files(self):
        """Scan for Java and C++ files"""
        print("Scanning project files...")
        
        for root, dirs, files in os.walk(self.project_root):
            # Skip build and git directories
            dirs[:] = [d for d in dirs if d not in ['build', '.git', 'gradle', '.gradle']]
            
            for file in files:
                file_path = Path(root) / file
                if file.endswith('.java'):
                    self.java_files.append(file_path)
                elif file.endswith('.cpp') or file.endswith('.cc') or file.endswith('.c'):
                    self.cpp_files.append(file_path)
                elif file.endswith('.h') or file.endswith('.hpp'):
                    self.header_files.append(file_path)
    
    def extract_java_natives(self) -> Dict[str, JNIMethod]:
        """Extract native method declarations from Java files"""
        java_natives = {}
        
        for java_file in self.java_files:
            try:
                with open(java_file, 'r', encoding='utf-8') as f:
                    content = f.read()
                
                # Find class name
                class_match = re.search(r'class\s+(\w+)', content)
                if not class_match:
                    continue
                    
                class_name = class_match.group(1)
                
                # Find package
                package_match = re.search(r'package\s+([\w.]+)', content)
                package = package_match.group(1) if package_match else ""
                
                full_class_name = f"{package}.{class_name}" if package else class_name
                
                # Find native methods (ignore comments)
                lines = content.split('\n')
                for line_num, line in enumerate(lines, 1):
                    # Skip comment lines
                    stripped_line = line.strip()
                    if stripped_line.startswith('//') or stripped_line.startswith('/*') or stripped_line.startswith('*'):
                        continue
                    
                    # Look for native method declarations
                    match = self.java_native_pattern.search(line)
                    if match:
                        return_type = match.group(1)
                        method_name = match.group(2)
                        parameters = match.group(3)
                        
                        method = JNIMethod(
                            name=method_name,
                            signature=parameters,
                            return_type=return_type,
                            parameters=self._parse_parameters(parameters),
                            file_path=str(java_file),
                            line_number=line_num,
                            is_implementation=False
                        )
                        
                        key = f"{full_class_name}.{method_name}"
                        java_natives[key] = method
                    
            except Exception as e:
                print(f"Error processing {java_file}: {e}")
        
        return java_natives
    
    def extract_cpp_implementations(self) -> Dict[str, JNIMethod]:
        """Extract JNI implementations from C++ files"""
        cpp_implementations = {}
        
        for cpp_file in self.cpp_files + self.header_files:
            try:
                with open(cpp_file, 'r', encoding='utf-8') as f:
                    content = f.read()
                
                # Find JNIEXPORT functions
                for match in self.cpp_jniexport_pattern.finditer(content):
                    return_type = match.group(1)
                    function_name = match.group(2)
                    
                    # Get line number
                    lines = content[:match.start()].count('\n') + 1
                    
                    # Convert function name to method name
                    method_name = self._extract_method_name_from_jni_function(function_name)
                    
                    method = JNIMethod(
                        name=method_name,
                        signature="",  # Would need more complex parsing for full signature
                        return_type=return_type,
                        parameters=[],
                        file_path=str(cpp_file),
                        line_number=lines,
                        is_implementation=True
                    )
                    
                    cpp_implementations[function_name] = method
                
                # Also look for function declarations in header files
                if cpp_file.suffix in ['.h', '.hpp']:
                    # Look for function declarations without JNIEXPORT
                    for match in re.finditer(r'Java_([\w_]+)\s*\(', content):
                        function_name = match.group(0)[:-1]  # Remove the '('
                        
                        if function_name not in cpp_implementations:
                            # Get line number
                            lines = content[:match.start()].count('\n') + 1
                            
                            method = JNIMethod(
                                name=self._extract_method_name_from_jni_function(function_name),
                                signature="",
                                return_type="unknown",
                                parameters=[],
                                file_path=str(cpp_file),
                                line_number=lines,
                                is_implementation=True
                            )
                            
                            cpp_implementations[function_name] = method
                    
            except Exception as e:
                print(f"Error processing {cpp_file}: {e}")
        
        return cpp_implementations
    
    def _parse_parameters(self, param_str: str) -> List[str]:
        """Parse Java method parameters"""
        if not param_str.strip():
            return []
        
        params = []
        current_param = ""
        bracket_count = 0
        
        for char in param_str:
            if char == ',' and bracket_count == 0:
                params.append(current_param.strip())
                current_param = ""
            else:
                if char == '<':
                    bracket_count += 1
                elif char == '>':
                    bracket_count -= 1
                current_param += char
        
        if current_param.strip():
            params.append(current_param.strip())
        
        return params
    
    def _extract_method_name_from_jni_function(self, function_name: str) -> str:
        """Extract method name from JNI function name"""
        # Convert Java_com_bearmod_Floating_onlinename to onlinename
        parts = function_name.split('_')
        if len(parts) >= 3:
            return parts[-1]  # Last part is usually the method name
        return function_name
    
    def analyze_jni(self) -> JNIAnalysis:
        """Perform comprehensive JNI analysis"""
        print("Extracting Java native method declarations...")
        java_natives = self.extract_java_natives()
        
        print("Extracting C++ JNI implementations...")
        cpp_implementations = self.extract_cpp_implementations()
        
        # Find missing implementations
        missing_implementations = []
        missing_declarations = []
        signature_mismatches = []
        
        # Check for missing implementations
        for java_key, java_method in java_natives.items():
            # Convert Java method to expected JNI function name
            expected_jni_name = self._java_to_jni_function_name(java_key)
            # Some parsers store keys without the 'Java_' prefix. Support both.
            expected_no_prefix = expected_jni_name[5:] if expected_jni_name.startswith("Java_") else expected_jni_name

            if (expected_jni_name not in cpp_implementations) and (expected_no_prefix not in cpp_implementations):
                missing_implementations.append(java_key)
        
        # Check for missing declarations (C++ implementations without Java declarations)
        for cpp_key, cpp_method in cpp_implementations.items():
            # This is more complex and would need reverse mapping
            # For now, we'll focus on missing implementations
            pass
        
        return JNIAnalysis(
            declared_methods=java_natives,
            implemented_methods=cpp_implementations,
            missing_implementations=missing_implementations,
            missing_declarations=missing_declarations,
            signature_mismatches=signature_mismatches
        )
    
    def _java_to_jni_function_name(self, java_method_key: str) -> str:
        """Convert Java method key to expected JNI function name"""
        # Convert "com.bearmod.Floating.onlinename" to "Java_com_bearmod_Floating_onlinename"
        parts = java_method_key.split('.')
        if len(parts) >= 2:
            class_part = '_'.join(parts[:-1])  # com_bearmod_Floating
            method_part = parts[-1]  # onlinename
            return f"Java_{class_part}_{method_part}"
        return java_method_key
    
    def generate_report(self, analysis: JNIAnalysis) -> str:
        """Generate a comprehensive JNI analysis report"""
        report = []
        report.append("=" * 80)
        report.append("JNI VALIDATION REPORT FOR BEARMOD PROJECT")
        report.append("=" * 80)
        report.append("")
        
        # Summary
        report.append("SUMMARY:")
        report.append(f"  Java native method declarations: {len(analysis.declared_methods)}")
        report.append(f"  C++ JNI implementations: {len(analysis.implemented_methods)}")
        report.append(f"  Missing implementations: {len(analysis.missing_implementations)}")
        report.append(f"  Missing declarations: {len(analysis.missing_declarations)}")
        report.append(f"  Signature mismatches: {len(analysis.signature_mismatches)}")
        report.append("")
        
        # Java native method declarations
        report.append("JAVA NATIVE METHOD DECLARATIONS:")
        report.append("-" * 50)
        for key, method in analysis.declared_methods.items():
            report.append(f"  {key}")
            report.append(f"    Return: {method.return_type}")
            report.append(f"    Parameters: {', '.join(method.parameters) if method.parameters else 'none'}")
            report.append(f"    File: {method.file_path}:{method.line_number}")
            report.append("")
        
        # C++ JNI implementations
        report.append("C++ JNI IMPLEMENTATIONS:")
        report.append("-" * 50)
        for key, method in analysis.implemented_methods.items():
            report.append(f"  {key}")
            report.append(f"    Return: {method.return_type}")
            report.append(f"    File: {method.file_path}:{method.line_number}")
            report.append("")
        
        # Missing implementations
        if analysis.missing_implementations:
            report.append("MISSING IMPLEMENTATIONS:")
            report.append("-" * 50)
            for missing in analysis.missing_implementations:
                # Use ASCII fallback for environments that can't print Unicode
                try:
                    report.append(f"  ❌ {missing}")
                except Exception:
                    report.append(f"  [X] {missing}")
                if missing in analysis.declared_methods:
                    method = analysis.declared_methods[missing]
                    expected_jni = self._java_to_jni_function_name(missing)
                    report.append(f"    Expected JNI function: {expected_jni}")
                    report.append(f"    File: {method.file_path}:{method.line_number}")
                report.append("")
        
        # Recommendations
        report.append("RECOMMENDATIONS:")
        report.append("-" * 50)
        if analysis.missing_implementations:
            report.append("1. Implement missing JNI functions:")
            for missing in analysis.missing_implementations:
                expected_jni = self._java_to_jni_function_name(missing)
                report.append(f"   - {expected_jni}")
            report.append("")
        
        report.append("2. Use the JNI_Bridge.h and JNI_Bridge.cpp files for centralized JNI management")
        report.append("3. Ensure all native method signatures match between Java and C++")
        report.append("4. Add proper error handling in JNI implementations")
        report.append("5. Use the utility functions in JNI_Bridge.cpp for safe string handling")
        
        return "\n".join(report)

def main():
    if len(sys.argv) != 2:
        print("Usage: python jni_validator.py <project_root>")
        sys.exit(1)
    
    project_root = sys.argv[1]
    
    if not os.path.exists(project_root):
        print(f"Project root {project_root} does not exist")
        sys.exit(1)
    
    validator = JNIValidator(project_root)
    validator.scan_files()
    
    print(f"Found {len(validator.java_files)} Java files")
    print(f"Found {len(validator.cpp_files)} C++ files")
    print(f"Found {len(validator.header_files)} header files")
    print("")
    
    analysis = validator.analyze_jni()
    report = validator.generate_report(analysis)
    
    print(report)
    
    # Save report to file
    report_file = Path(project_root) / "jni_validation_report.txt"
    with open(report_file, 'w', encoding='utf-8') as f:
        f.write(report)
    
    print(f"\nReport saved to: {report_file}")
    
    # Exit with error code if there are issues
    if analysis.missing_implementations:
        print(f"\n❌ Found {len(analysis.missing_implementations)} missing JNI implementations")
        sys.exit(1)
    else:
        print("\n[SUCCESS] All JNI methods have implementations")
        sys.exit(0)

if __name__ == "__main__":
    main()
