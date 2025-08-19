# Development Rules (wf-05)

## Documentation requirements
- Public Activities, Services, JNI classes, and API facades must include:
  - Class-level Javadoc/KDoc describing purpose, responsibilities, lifecycle
  - Javadoc for JNI methods: params, return, errors, lifecycle notes
- If user-visible: add short snippet to `docs/` describing feature discovery and usage

Quick-start snippets (copy/paste):
- Function Javadoc template: `docs/snippet/JavadocFunctionSample.md`
- JNI method pattern: `docs/snippet/JNIMethodSample.md`
- API + Mock skeleton: `docs/snippet/APIMockSkeleton.md`
 - MockWebServer sample: `docs/snippet/MockWebServerSample.md`
 - Zygisk Enc Root scaffold: `docs/snippet/ZygiskEncRootModule.md`

Full guide: `docs/TESTING_GUIDE.md`

## Testing requirements
- Every API/service provides a mock implementation for unit tests.
  - Example: `com.bearmod.auth.api.LicenseAPI` with `MockLicenseAPI`
- JVM unit tests use mocks by default. Instrumentation tests may hit real backends.

### Instrumentation testing (androidTest)
- Use `MockWebServer` for networked code. For `SimpleLicenseVerifier`, set:
  - `SimpleLicenseVerifier.setApiBaseForTesting(server.url("/").toString())`
  - `SimpleLicenseVerifier.clearApiBaseForTesting()`
- Keep timeouts low; use CountDownLatch/IdlingResource.

## CI gates
- Checkstyle enforces Javadoc on public classes/methods
  - Config: `app/checkstyle.xml`
  - Gradle: `:app:checkstyleMain` and `:app:checkstyleTest`
- JNI validation and both Java and native tests run on every PR
 - Kotlin style: ktlint runs (warning-only initially)
 - Native static analysis: cppcheck runs (warning-only initially)

## Local commands
- Java unit tests: `./gradlew :app:testDebugUnitTest`
- Native tests: `cmake -S app/src/test/cpp -B app/src/test/cpp/build && cmake --build app/src/test/cpp/build && ctest --test-dir app/src/test/cpp/build`
- Checkstyle: `./gradlew :app:checkstyleMain :app:checkstyleTest`
 - ktlint check: `./gradlew :app:ktlintCheck` (warning-only)
 - ktlint format: `./gradlew :app:ktlintFormat`
 - cppcheck (example): `cppcheck --std=c++17 --enable=warning,style,performance --suppress=missingIncludeSystem app/src/main/cpp app/src/test/cpp`
