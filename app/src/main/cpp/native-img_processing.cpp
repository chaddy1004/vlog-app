//
// Created by Chad Paik on 2020-01-23.
//

#include <opencv2/opencv.hpp>
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
//
//extern "C" JNIEXPORT jstring JNICALL
//Java_com_chaddysroom_vloggingapp_utils_img_1util_ImageProcessor_convert(JNIEnv *env, jobject, jlong addrRgba,
//                                                                        jlong addrGray) {
////return an integer
//    cv::Mat &mRgb = *(cv::Mat *) addrRgba;
//    cv::Mat &mGray = *(cv::Mat *) addrGray;
//    cv::cvtColor(mRgb, mGray, CV_RGB2GRAY);
//    std::string msg = "COLOUR CONVERTED";
//    return env->NewStringUTF(msg.c_str());
//}
//
extern "C" JNIEXPORT jstring JNICALL
Java_com_chaddysroom_vloggingapp_utils_img_1util_ImageProcessor_receive(JNIEnv *env, jobject, jobject bytebuffer,
                                                                        jint size) {
    string outStr;
    outStr.reserve(size / 20);
    jbyte *ptr;
    ptr = (jbyte *) (env)->GetDirectBufferAddress(bytebuffer);

    if (!ptr) {
        return env->NewStringUTF(string("NULL").c_str());
    }

    for (int i = 0; i < size / 20; i++) {
        outStr[i] = to_string(ptr[i])[0];
    }

//    return env->NewStringUTF(std::string("NOT NULL").c_str());
    return env->NewStringUTF(outStr.c_str());

};


extern "C" JNIEXPORT jstring JNICALL
Java_com_chaddysroom_vloggingapp_utils_img_1util_ImageProcessor_toMat(JNIEnv *env, jobject, jobject bytebuffer,
                                                                        jint height, jint width) {
    string outStr;
    jbyte *ptr;
    ptr = (jbyte *) (env)->GetDirectBufferAddress(bytebuffer);

    if (!ptr) {
        return env->NewStringUTF(string("NULL").c_str());
    }

    cv::Mat mat = cv::Mat(height, width, CV_8UC3, (void*)ptr);
    cv::Mat mat2 = cv::Mat(height, width, CV_8UC3);
    cv::cvtColor(mat, mat2, CV_YUV2RGB);
//    return env->NewStringUTF(std::string("NOT NULL").c_str());
    return env->NewStringUTF(string("successful").c_str());
};


extern "C" JNIEXPORT jint JNICALL
Java_com_chaddysroom_vloggingapp_utils_img_1util_ImageProcessor_YUVMerge(JNIEnv *env, jobject, jlong Yaddr, jlong Uaddr, jlong Vaddr, jlong YUVaddr) {

    cv::Mat& Y_channel = *(reinterpret_cast<cv::Mat*>(Yaddr));
    cv::Mat& U_channel = *(reinterpret_cast<cv::Mat*>(Uaddr));
    cv::Mat& V_channel = *(reinterpret_cast<cv::Mat*>(Vaddr));
    cv::Mat& outYUV = *(reinterpret_cast<cv::Mat*>(YUVaddr));

    cv::Mat YUVArr[3] = {Y_channel, U_channel, V_channel};

    cv::merge(YUVArr, 3, outYUV);

    if(outYUV.rows == Y_channel.rows && outYUV.cols == Y_channel.cols)
    {
        return jint(0);
    }
    else
    {
        return jint(1);
    }
    //cv::Mat& outGray = *(reinterpret_cast<cv::Mat*>(grayAddr));

    //cv::cvtColor(yuvMat, outGray, CV_YUV2GRAY_420);
};




