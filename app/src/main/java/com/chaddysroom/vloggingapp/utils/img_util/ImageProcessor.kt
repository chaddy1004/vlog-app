package com.chaddysroom.vloggingapp.utils.img_util

import android.media.Image
import android.media.ImageReader
import android.util.Log
import org.opencv.core.CvType.*
import org.opencv.core.Mat
import java.nio.ByteBuffer

class ImageProcessor : ImageReader.OnImageAvailableListener {
    init{
        System.loadLibrary("native-img_processing")
    }

    external fun helloworld() : String
    external fun receive(bytebuffer: ByteBuffer, size: Int): String
    external fun toMat(bytebuffer: ByteBuffer, height: Int, width: Int): String
    external fun YUVMerge(y_mat: Long, u_mat : Long, v_mat : Long, outYUV_mat : Long) : Int;

    override fun onImageAvailable(reader: ImageReader?) {
        val img = reader?.acquireLatestImage() ?: return
        val planes = img.planes
        var message = ""
//        Log.i("IMAGEPROCESSOR", "Latest Image Received")
//        Log.i("FORMAT", reader.imageFormat.toString())
        Log.i("NDK", helloworld())

        message = processImg(planes = planes, height=img.height, width=img.width)
        Log.e("RECEIVED", message)
        img.close()
    }

    private fun processImg(planes: Array<Image.Plane>, height:Int, width:Int): String {
        // Your image processing code goes here
        val plane0 = planes[0]
        val plane1 = planes[1]
        val plane2 = planes[2]

        val y_mat = Mat(height, width, CV_8UC1, plane0.buffer)
        val u_mat = Mat(height, width, CV_8UC1, plane1.buffer)
        val v_mat = Mat(height, width, CV_8UC1, plane2.buffer)

        var yuv_mat = Mat(height, width, CV_8UC3);

//        plane0.buffer.get(plane_bytes)
        var result = 0

        result = YUVMerge(y_mat.nativeObjAddr, u_mat.nativeObjAddr, v_mat.nativeObjAddr, yuv_mat.nativeObjAddr)
        Log.e("processImg", result.toString())
        Log.i("processImg", "Img processed")

        y_mat.release()
        u_mat.release()
        v_mat.release()
        yuv_mat.release()

        return "DoneProcessing"
    }
}

