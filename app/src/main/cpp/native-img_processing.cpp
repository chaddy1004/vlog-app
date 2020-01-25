//
// Created by Chad Paik on 2020-01-23.
//

#include <jni.h>
#include <string>

using namespace std;

extern "C" JNIEXPORT jstring JNICALL
// The 1 is there because underscore is used in the folder name.
// In function name, underscore by itself is used to replace backslashes from directory path
Java_com_chaddysroom_vloggingapp_utils_img_1util_ImageProcessor_helloworld(JNIEnv *env, jobject) {
    //return an integer
    string hello = "Hello from JNI";
    return env->NewStringUTF(hello.c_str());
}


