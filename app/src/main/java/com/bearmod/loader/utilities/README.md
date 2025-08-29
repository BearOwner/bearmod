# BearMod Loader Utilities Package

## Overview
The `com.bearmod.loader.utilities` package provides shared utility classes and helper functions used across the BearMod application. This package contains common functionality that supports the modular architecture by providing reusable components.

## Package Structure
```
com.bearmod.loader.utilities/
├── Logx.java          # Centralized logging utility with obfuscated codes
├── NativeUtils.java    # Native library loading and state management
├── StrObf.java         # String obfuscation utilities
├── FileUtil.java       # File operations and management
└── PermissionManager.kt # Android permission handling
```

## Core Components

### Logx.java
**Purpose**: Centralized logging system with obfuscated error codes for security.

**Key Features**:
- Obfuscated error codes to prevent reverse engineering
- Consistent logging format across the application
- Debug/release mode support
- Performance-optimized logging

**Usage**:
```java
import com.bearmod.loader.utilities.Logx;

// Log with obfuscated code
Logx.d("AUTH_SUCCESS");  // Maps to: "Authentication successful"

// Log with custom message
Logx.e("FILE_ERROR", exception);  // Maps to: "File operation failed"
```

### NativeUtils.java
**Purpose**: Manages native library loading state and provides utility functions for JNI operations.

**Key Features**:
- Native library state tracking
- Safe native method invocation
- Error handling for native operations
- Thread-safe operations

**Usage**:
```java
import com.bearmod.loader.utilities.NativeUtils;

// Check if native library is loaded
if (NativeUtils.isNativeLoaded()) {
    // Safe to call native methods
    NativeUtils.runIfReady(() -> {
        // Native operations here
    });
}
```

### FileUtil.java
**Purpose**: Comprehensive file operations utility with security considerations.

**Key Features**:
- Secure file operations
- Path validation and sanitization
- Memory-efficient file handling
- Cross-platform compatibility

**Usage**:
```java
import com.bearmod.loader.utilities.FileUtil;

// Read file securely
String content = FileUtil.readFile("/path/to/file");

// Write file with validation
FileUtil.writeFile("/path/to/file", content);
```

### PermissionManager.kt (Kotlin)
**Purpose**: Android permission management with modern Android permission handling.

**Key Features**:
- Modern Android permission API support
- Permission request/result handling
- Backward compatibility
- Permission status checking

**Usage**:
```kotlin
import com.bearmod.loader.utilities.PermissionManager

// Check permission status
val status = permissionManager.checkStoragePermission()

// Request permission
permissionManager.requestStoragePermission(activity, REQUEST_CODE)
```

## Dependencies
- **Internal**: None (pure utility package)
- **External**:
  - Android SDK (for Android-specific utilities)
  - Kotlin Standard Library (for PermissionManager.kt)

## Security Considerations
- All file operations include path validation
- Logging uses obfuscated codes to prevent information leakage
- Permission handling follows Android security best practices
- String obfuscation protects sensitive data

## Performance Notes
- File operations are optimized for memory usage
- Logging is designed to minimize performance impact
- Native operations include proper error handling
- Permission checks are cached where appropriate

## Migration Notes
This package was created during the architectural migration from the monolithic `com.bearmod.util` package. All references have been updated across the codebase to use the new package structure.

## Future Enhancements
- Add more specialized utilities as needed
- Implement caching for frequently accessed data
- Add unit tests for all utility functions
- Consider adding configuration options for different environments