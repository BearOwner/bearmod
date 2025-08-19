cmake_minimum_required(VERSION 3.22.1)
project(mundo_core VERSION 2.0.0 LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

set(MUNDO_SOURCES
        ${CMAKE_CURRENT_SOURCE_DIR}/api/mundo_api.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/core/mundo_core.cpp
        ${CMAKE_CURRENT_SOURCE_DIR}/jni/mundo_jni_bridge.cpp
)

add_library(mundo SHARED ${MUNDO_SOURCES})

target_include_directories(mundo
        PUBLIC
        ${CMAKE_CURRENT_SOURCE_DIR}/../../main/cpp/include
        PRIVATE
        ${CMAKE_CURRENT_SOURCE_DIR}
)

# Compile options/defs
target_compile_definitions(mundo PRIVATE MUNDO_LIBRARY_BUILD)

target_compile_options(mundo PRIVATE
        -fvisibility=hidden
        -ffunction-sections
        -fdata-sections
        -Wall
)

if(CMAKE_BUILD_TYPE STREQUAL "Debug")
    target_compile_options(mundo PRIVATE -O0 -g)
else()
    target_compile_options(mundo PRIVATE -O2)
endif()

set_target_properties(mundo PROPERTIES
        VERSION ${PROJECT_VERSION}
        SOVERSION ${PROJECT_VERSION_MAJOR}
)

target_link_options(mundo PRIVATE -Wl,--gc-sections)

target_link_libraries(mundo
        log
        android
        dl
)

