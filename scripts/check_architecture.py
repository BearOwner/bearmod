import os
import ast
import argparse
import json
import sys
from typing import List, Dict


def eprint(*args, **kwargs):
    print(*args, file=sys.stderr, **kwargs)


def find_god_classes(directory: str, threshold: int = 15) -> List[Dict]:
    god_classes: List[Dict] = []
    for root, _, files in os.walk(directory):
        for file in files:
            if file.endswith('.java') or file.endswith('.py'):
                path = os.path.join(root, file)
                try:
                    with open(path, "r", encoding="utf-8", errors="ignore") as f:
                        source = f.read()
                        if file.endswith(".py"):
                            try:
                                tree = ast.parse(source)
                            except SyntaxError:
                                # Skip unparsable python files quietly
                                continue
                            for node in ast.walk(tree):
                                if isinstance(node, ast.ClassDef):
                                    methods = [n for n in node.body if isinstance(n, ast.FunctionDef)]
                                    if len(methods) > threshold:
                                        god_classes.append({
                                            "file": path,
                                            "class": node.name,
                                            "methods": len(methods),
                                            "severity": "CRITICAL"
                                        })
                        else:  # crude Java method counting
                            method_count = sum(
                                1 for line in source.splitlines()
                                if (" void " in line or "public " in line or "private " in line or "protected " in line)
                                and "(" in line and ")" in line
                            )
                            if method_count > threshold:
                                god_classes.append({
                                    "file": path,
                                    "class": "JavaClass",
                                    "methods": method_count,
                                    "severity": "CRITICAL"
                                })
                except Exception as e:
                    # Clean error logging to stderr without traceback
                    eprint(f"Error reading {path}: {e}")
    return god_classes


def find_duplicate_functions(directory: str) -> List[Dict]:
    func_map: Dict[str, tuple] = {}
    duplicates: List[Dict] = []
    for root, _, files in os.walk(directory):
        for file in files:
            if file.endswith(".java") or file.endswith(".py"):
                path = os.path.join(root, file)
                try:
                    with open(path, "r", encoding="utf-8", errors="ignore") as f:
                        source = f.read()
                        if file.endswith(".py"):
                            try:
                                tree = ast.parse(source)
                            except SyntaxError:
                                continue
                            for node in ast.walk(tree):
                                if isinstance(node, ast.FunctionDef):
                                    sig = node.name + str(len(node.args.args))
                                    if sig in func_map:
                                        duplicates.append({
                                            "file": path,
                                            "function": node.name,
                                            "severity": "WARNING"
                                        })
                                    else:
                                        func_map[sig] = (path, node.name)
                        else:  # Java
                            for line in source.splitlines():
                                if ((" void " in line or "public " in line or "private " in line or "protected " in line)
                                    and "(" in line and ")" in line):
                                    name = line.split("(")[0].split()[-1]
                                    sig = name
                                    if sig in func_map:
                                        duplicates.append({
                                            "file": path,
                                            "function": name,
                                            "severity": "WARNING"
                                        })
                                    else:
                                        func_map[sig] = (path, name)
                except Exception as e:
                    eprint(f"Error reading {path}: {e}")
    return duplicates


def main() -> int:
    parser = argparse.ArgumentParser(description="AI Architecture Checker")
    parser.add_argument("--dir", dest="dir", default="app/src/main/java", help="Target directory to scan")
    parser.add_argument("--format", choices=["text", "json"], default="text", help="Output format")
    parser.add_argument("--threshold", type=int, default=15, help="Method count threshold for god classes")
    parser.add_argument("--strict", action="store_true", help="Exit with non-zero if critical issues found")
    args = parser.parse_args()

    target_dir = args.dir
    if not os.path.isdir(target_dir):
        eprint(f"Target directory does not exist: {target_dir}")
        # Non-zero if dir missing to surface misconfiguration
        return 2

    god_classes = find_god_classes(target_dir, threshold=args.threshold)
    duplicates = find_duplicate_functions(target_dir)

    findings = {
        "directory": target_dir,
        "threshold": args.threshold,
        "god_classes": god_classes,
        "duplicates": duplicates,
    }

    if args.format == "json":
        print(json.dumps(findings, indent=2))
    else:
        print("=== God Classes ===")
        for g in god_classes:
            print(f"[CRITICAL] {g['file']}: {g['class']} ({g['methods']} methods)")
        if not god_classes:
            print("(none)")
        print("\n=== Duplicate Functions ===")
        for d in duplicates:
            print(f"[WARNING] {d['file']}: {d['function']}")
        if not duplicates:
            print("(none)")

    if args.strict and any(f.get("severity") == "CRITICAL" for f in god_classes):
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
