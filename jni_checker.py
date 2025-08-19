import os
import re

# Adjust to your project root
PROJECT_ROOT = "."

# Regex to find native methods in Java/Kotlin
JAVA_NATIVE_REGEX = re.compile(r"native\s+[\w<>]+\s+(\w+)\s*\(")

# Regex to find JNIEXPORT / Java_ functions in C++
CPP_JNI_REGEX = re.compile(r"(Java_[\w_]+)")

def scan_files(root, exts):
    """Recursively yield files with given extensions"""
    for dirpath, _, files in os.walk(root):
        for f in files:
            if any(f.endswith(ext) for ext in exts):
                yield os.path.join(dirpath, f)

def extract_java_natives(file):
    methods = []
    with open(file, "r", encoding="utf-8", errors="ignore") as f:
        for line in f:
            match = JAVA_NATIVE_REGEX.search(line)
            if match:
                methods.append(match.group(1))
    return methods

def extract_cpp_jni(file):
    methods = []
    with open(file, "r", encoding="utf-8", errors="ignore") as f:
        for line in f:
            match = CPP_JNI_REGEX.search(line)
            if match:
                methods.append(match.group(1))
    return methods

def main():
    java_natives = {}
    cpp_functions = set()

    # Step 1: Find all native methods in Java
    for file in scan_files(PROJECT_ROOT, [".java", ".kt"]):
        natives = extract_java_natives(file)
        if natives:
            java_natives[file] = natives

    # Step 2: Find all JNI functions in C++
    for file in scan_files(PROJECT_ROOT, [".cpp", ".c", ".cc"]):
        funcs = extract_cpp_jni(file)
        cpp_functions.update(funcs)

    # Step 3: Report
    print("=== Java Native Methods (Declarations) ===")
    for file, methods in java_natives.items():
        for m in methods:
            print(f"{m} (from {file})")

    print("\n=== C++ JNI Implementations ===")
    for func in sorted(cpp_functions):
        print(func)

    print("\n=== Cross-check (missing implementations) ===")
    for file, methods in java_natives.items():
        for m in methods:
            expected_name = f"Java_{file.replace('/', '_').replace('.', '_')}_{m}"
            # Not exact (because package/class naming must match), but helps detect missing links
            found = any(m in func for func in cpp_functions)
            status = "✅ FOUND" if found else "❌ MISSING"
            print(f"{m} -> {status}")

if __name__ == "__main__":
    main()
