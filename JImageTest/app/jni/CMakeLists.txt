cmake_minimum_required(VERSION 3.4.1)

find_library(log-lib log)
#find_library(android-lib android)
set(JNI_LIBS_DIR  ${CMAKE_SOURCE_DIR}/../libs)
include_directories(${JNI_LIBS_DIR}/include)

add_library(Native_Lib SHARED NativeLibrary.cpp )

target_link_libraries(Native_Lib

                      ${log-lib} ${android-lib})