//
// Created by Chad Paik on 2020-01-23.
//

#include <opencv2/opencv.hpp>
#include <opencv2/imgcodecs.hpp>
#include <opencv/highgui.h>
#include <jni.h>
#include <string>
#include <android/bitmap.h>
#include <android/native_window_jni.h>

using namespace std;



extern "C" JNIEXPORT jint JNICALL
Java_com_chaddysroom_vloggingapp_utils_img_1util_ImageProcessor_YUV2RGB(JNIEnv *env, jobject, jint srcWidth,
                                                                        jint srcHeight, jobject srcBuffer,
                                                                        jlong matptr,
                                                                        jboolean frontCamera) {
    uint8_t *srcLumaPtr = reinterpret_cast<uint8_t *>(env->GetDirectBufferAddress(srcBuffer));
    cv::Mat mYUV(srcHeight + srcHeight / 2, srcWidth, CV_8UC1, srcLumaPtr);
//    cv::Mat mYUV(srcHeight+srcHeight/2, srcWidth, CV_8UC1, srcLumaPtr);

    cv::Mat &flipRGBA = *(cv::Mat *) matptr;
    cv::Mat srcRGBA = cv::Mat(srcHeight + srcHeight / 2, srcWidth, CV_8UC4);

//    cv::Mat srcRGBA(srcHeight, srcWidth, CV_8UC4);
    cv::cvtColor(mYUV, srcRGBA, CV_YUV2BGRA_NV21);
    cv::transpose(srcRGBA, flipRGBA);
    if (frontCamera) {
        cv::flip(flipRGBA, flipRGBA, 0);
    } else {
        cv::flip(flipRGBA, flipRGBA, 1);
    }
//    const char *converted = env->GetStringUTFChars(dirName, 0);
//
//    string name = std::string(converted, strlen(converted));
    int result = 0;
    if (srcRGBA.empty()) {
        return 0;
    } else {
        return result;
    }
}


extern "C" JNIEXPORT jint JNICALL
Java_com_chaddysroom_vloggingapp_utils_img_1util_ImageProcessor_Grayscale2Surface(JNIEnv *env, jobject, jint srcWidth,
                                                                                  jint srcHeight, jobject srcBuffer,
                                                                                  jobject dstSurface) {
    uint8_t *srcLumaPtr = reinterpret_cast<uint8_t *>(env->GetDirectBufferAddress(srcBuffer));
    cv::Mat mYUV(srcHeight, srcWidth, CV_8UC1, srcLumaPtr);
    cv::Mat mGray(srcHeight, srcWidth, CV_8UC1);
    cv::Mat surfaceGray(srcHeight, srcWidth, CV_8UC1);
    cv::Mat flipGray(srcHeight, srcWidth, CV_8UC1);


    cv::cvtColor(mYUV, mGray, CV_YUV2GRAY_NV21);
    cv::transpose(mGray, flipGray);
    cv::flip(flipGray, flipGray, 1);
    if (mGray.empty()) {
        return 0;
    }

    ANativeWindow *win = ANativeWindow_fromSurface(env, dstSurface);
    ANativeWindow_acquire(win);
    ANativeWindow_Buffer buf;


    uint8_t *dstGrayptr = reinterpret_cast<uint8_t *>(buf.bits);
    cv::Mat dstGray(srcWidth, buf.stride, CV_8UC1, dstGrayptr);


    ANativeWindow_unlockAndPost(win);
    ANativeWindow_release(win);

    return 1;
}



//extern "C"
//JNIEXPORT jstring JNICALL
//Java_com_chaddysroom_vloggingapp_utils_img_1util_ImageProcessor_surfaceTest(JNIEnv *env, jclass type, jint srcWidth, jint srcHeight, jobject srcBuffer,
//                                                                jobject dstSurface, jstring path_, jint savefile) {
//    const char *str = env->GetStringUTFChars(path_, 0);
//
//    // Code
//
//    LOGE("bob path:%s saveFile=%d", str, savefile);
//
//    uint8_t *srcLumaPtr = reinterpret_cast<uint8_t *>(env->GetDirectBufferAddress(srcBuffer));
//
//    if (srcLumaPtr == NULL) {
//        LOGE("blit NULL pointer ERROR");
//        return NULL;
//    }
//
//    int dstWidth;
//    int dstHeight;
//
//    cv::Mat mYuv(srcHeight + srcHeight / 2, srcWidth, CV_8UC1, srcLumaPtr);
//
//
//    ANativeWindow *win = ANativeWindow_fromSurface(env, dstSurface);
//    ANativeWindow_acquire(win);
//
//    ANativeWindow_Buffer buf;
//
//    dstWidth = srcHeight;
//    dstHeight = srcWidth;
//
//    ANativeWindow_setBuffersGeometry(win, dstWidth, dstHeight, 0 /*format unchanged*/);
//
//    if (int32_t err = ANativeWindow_lock(win, &buf, NULL)) {
//        LOGE("ANativeWindow_lock failed with error code %d\n", err);
//        ANativeWindow_release(win);
//        return NULL;
//    }
//
//    uint8_t *dstLumaPtr = reinterpret_cast<uint8_t *>(buf.bits);
//    Mat dstRgba(dstHeight, buf.stride, CV_8UC4,
//                dstLumaPtr);        // TextureView buffer, use stride as width
//    Mat srcRgba(srcHeight, srcWidth, CV_8UC4);
//    Mat flipRgba(dstHeight, dstWidth, CV_8UC4);
//
//    // convert YUV -> RGBA
//    cv::cvtColor(mYuv, flipRgba, CV_YUV2RGBA_NV21);
//
//    // Rotate 90 degree
//    // rotateMat(flipRgba, 2);
//
//    LOGE(" ------- DATA -----------  \n dstWidth: %d   stride: %d ", dstRgba.cols, buf.stride);
//
//    // copy to TextureView surface
//    uchar *dbuf;
//    uchar *sbuf;
//    dbuf = dstRgba.data;
//    sbuf = flipRgba.data;
//    int i;
//    for (i = 0; i < flipRgba.rows; i++) {
//        dbuf = dstRgba.data + i * buf.stride * 4;
//        memcpy(dbuf, sbuf, flipRgba.cols * 4);
//        sbuf += flipRgba.cols * 4;
//    }
//
//
//    // Draw some rectangles
//    line(dstRgba, Point(dstWidth/2, 0), Point(dstWidth/2, dstHeight-1),Scalar(255, 255, 255));
//    line(dstRgba, Point(0,dstHeight-1), Point(dstWidth-1, dstHeight-1),Scalar(255,255,255 ));
//
//    LOGE("bob dstWidth=%d height=%d", dstWidth, dstHeight);
//    ANativeWindow_unlockAndPost(win);
//    ANativeWindow_release(win);
//
//    // Release
//    env->ReleaseStringUTFChars(path_, str);
//
//    //Ret
//    return env->NewStringUTF("abc");
//}
//
//

