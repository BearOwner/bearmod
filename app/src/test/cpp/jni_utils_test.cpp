#include <gtest/gtest.h>

// Host build: we may not have Android headers available.
// Provide a minimal stub of LogJNIError when not on Android.
#ifndef __ANDROID__
// Match the declaration style used in JNI_Bridge.h
extern "C" void LogJNIError(void* /*env*/, const char* /*function*/, const char* /*error*/) {
    // No-op for host tests
}
#endif

TEST(JNIUtilsTest, LogErrorDoesNotCrash) {
    // Ensure calling the logger stub does not crash in host environment
    LogJNIError(nullptr, "testFunc", "test error");
    SUCCEED();
}
