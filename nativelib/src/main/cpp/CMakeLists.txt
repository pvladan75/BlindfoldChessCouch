cmake_minimum_required(VERSION 3.22.1)

project("nativelib")

# Pronalazi sve .cpp fajlove u ovom i podfolderima i uključuje ih u build
file(GLOB_RECURSE SOURCES "*.cpp")

add_library(
        nativelib
        SHARED
        ${SOURCES}
)

target_link_libraries(
        nativelib
        android
        log
)