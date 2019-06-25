//
// Created by Administrator on 2019-05-13.
//
#include "com_xuwd_jnitest_JNative.h"

JNIEXPORT jstring JNICALL Java_com_xuwd_jnitest_JNative_getStrFromJNI
  (JNIEnv* env, jobject thiz){

    return (*env)->NewStringUTF(env,"Hi!  xuwd");
}