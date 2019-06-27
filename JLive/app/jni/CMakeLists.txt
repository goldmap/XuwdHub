cmake_minimum_required(VERSION 3.4.1)

#本文件在gradle中有位置定义；并以本文件所在，为其它目录定位
set(jniLibs_Dirs ${CMAKE_CURRENT_SOURCE_DIR}/../libs)
include_directories(${jniLibs_Dirs}/include)

find_library(log-lib log)
find_library(android-lib android)

add_library(JffmpegLib  SHARED  JNative.c)

add_library(avutil    SHARED    IMPORTED )
set_target_properties(avutil  PROPERTIES IMPORTED_LOCATION  ${jniLibs_Dirs}/${ANDROID_ABI}/libavutil.so )

add_library(avformat    SHARED    IMPORTED )
set_target_properties(avformat  PROPERTIES IMPORTED_LOCATION  ${jniLibs_Dirs}/${ANDROID_ABI}/libavformat.so )

add_library(avcodec    SHARED    IMPORTED )
set_target_properties(avcodec  PROPERTIES IMPORTED_LOCATION  ${jniLibs_Dirs}/${ANDROID_ABI}/libavcodec.so )

add_library(avfilter    SHARED    IMPORTED )
set_target_properties(avfilter  PROPERTIES IMPORTED_LOCATION  ${jniLibs_Dirs}/${ANDROID_ABI}/libavfilter.so )

target_link_libraries(JffmpegLib
         avformat avcodec avfilter avutil
#       avutil avresample swresample swscale avcodec avformat avfilter avdevice
        ${log-lib} ${android-lib} )