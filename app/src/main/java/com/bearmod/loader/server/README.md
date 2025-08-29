# BearMod Loader Server Package

## Overview
The `com.bearmod.loader.server` package provides server communication and authentication services for the BearMod application. This package handles all external API interactions, license validation, and secure server communication.

## Package Structure
```
com.bearmod.loader.server/
├── SimpleLicenseVerifier.java   # KeyAuth API integration and license validation
├── HWID.java                    # Hardware ID generation and validation
└── Server.java                  # Base server communication wrapper
```

## Core Components

### SimpleLicenseVerifier.java
**Purpose**: Handles KeyAuth API integration for license validation and authentication.

**Key Features**:
- License key validation
- Hardware ID verification
- Session management
- Token handling
- Error recovery

**Usage**:
```java
import com.bearmod.loader.server.SimpleLicenseVerifier;

// Validate license
SimpleLicenseVerifier.verifyLicense(context, licenseKey, new LicenseCallback() {
    @Override
    public void onSuccess(String message) {
        // License valid
    }

    @Override
    public void onError(String error) {
        // Handle error
    }
});
```

### HWID.java
**Purpose**: Generates and validates hardware IDs for device authentication.

**Key Features**:
- Secure hardware ID generation
- Device fingerprinting
- HWID validation
- Anti-tampering protection

**Usage**:
```java
import com.bearmod.loader.server.HWID;

// Generate HWID
String hwid = HWID.getHWID();

// Validate HWID format
boolean isValid = HWID.isValidHwid(hwid);
```

### Server.java
**Purpose**: Base wrapper for server communication operations.

**Key Features**:
- HTTP client management
- Request/response handling
- Error handling
- Connection management

## Security Features

### Authentication Security
- Secure token storage and transmission
- Hardware ID validation
- Session timeout management
- Anti-replay attack protection

### Communication Security
- HTTPS-only communication
- Certificate validation
- Request signing
- Response verification

### Data Protection
- Encrypted data transmission
- Secure key storage
- Memory-only operations where possible
- Anti-debugging measures

## API Integration

### KeyAuth API
The package integrates with KeyAuth API for:
- License validation
- User authentication
- Session management
- Feature gating

### Error Handling
- Network error recovery
- API rate limiting
- Timeout management
- Graceful degradation

## Dependencies
- **Internal**:
  - `com.bearmod.loader.utilities.*` - Logging and utilities
- **External**:
  - OkHttp (HTTP client)
  - KeyAuth API
  - Android Security libraries

## Performance Considerations
- Connection pooling
- Request caching
- Background processing
- Memory-efficient operations

## Testing Strategy
- Mock server responses
- Network condition simulation
- Error scenario testing
- Integration testing

## Migration Notes
This package was created during the architectural migration to separate server communication concerns from UI and business logic. The original `SimpleLicenseVerifier` was refactored to use this new server architecture.

## Future Enhancements
- Add request/response caching
- Implement retry mechanisms
- Add analytics integration
- Support multiple authentication providers