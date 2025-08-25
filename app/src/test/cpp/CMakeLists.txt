cmake_minimum_required(VERSION 3.16)
project(bearmod_native_tests CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

include(FetchContent)
FetchContent_Declare(
  googletest
  URL https://github.com/google/googletest/archive/refs/tags/release-1.12.1.zip
)
# For Windows: Prevent overriding the parent project's compiler/linker settings
set(gtest_force_shared_crt ON CACHE BOOL "" FORCE)
FetchContent_MakeAvailable(googletest)

enable_testing()

add_executable(jni_utils_test
  jni_utils_test.cpp
)

target_include_directories(jni_utils_test PRIVATE
  ${CMAKE_CURRENT_SOURCE_DIR}
)

target_link_libraries(jni_utils_test
  GTest::gtest_main
)

include(GoogleTest)
gtest_discover_tests(jni_utils_test)
