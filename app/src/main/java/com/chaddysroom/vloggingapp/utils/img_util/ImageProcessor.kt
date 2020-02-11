package com.chaddysroom.vloggingapp.utils.img_util

import android.graphics.ImageFormat
import android.media.Image
import android.media.ImageReader
import android.os.Environment
import android.util.Log
import org.opencv.core.CvType.*
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgcodecs.Imgcodecs
import java.io.File
import java.lang.IllegalArgumentException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*

class ImageProcessor : ImageReader.OnImageAvailableListener {
    init {
        System.loadLibrary("native-img_processing")
    }

    external fun helloworld(): String
    external fun receive(bytebuffer: ByteBuffer, size: Int): String
    external fun toMat(bytebuffer: ByteBuffer, height: Int, width: Int): String
    external fun YUVMerge(y_mat: Long, u_mat: Long, v_mat: Long, outYUV_mat: Long): Int
    external fun YUV2RGB(srcWidth: Int, srcHeight: Int, YUVaddr: ByteBuffer, dirName: String, matptr: Long): Int

    override fun onImageAvailable(reader: ImageReader?) {
        val img = reader?.acquireLatestImage() ?: return
        val planes = img.planes
        var message = ""
//        Log.i("IMAGEPROCESSOR", "Latest Image Received")
//        Log.i("FORMAT", reader.imageFormat.toString())
        Log.i("NDK", helloworld())
        if (img.format != ImageFormat.YUV_420_888) {
            Log.e("ImageProcessor", "NOT_YUV_420_888")
        }

        message = processImg(planes = planes, height = img.height, width = img.width)
        Log.e("RECEIVED", message)
        img.close()
//        System.gc()
    }


    private fun processImg(planes: Array<Image.Plane>, height: Int, width: Int): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val filepath = Environment.getExternalStorageDirectory()
        val filename = "picture_${timestamp}.png"
        val dir = File(filepath.absolutePath + "/DCIM/" + "/VlogApp/")
        val file = File(dir, filename)
        // Your image processing code goes here
        val plane0 = planes[0]
        var result = -1;
        val mat = Mat(height, width, CV_8UC4, Scalar(0.0));
        result = YUV2RGB(width, height, plane0.buffer, file.toString(), mat.nativeObjAddr)
        val hello = Imgcodecs.imwrite(file.toString(), mat)
        return "DoneProcessing with $result $hello"
    }
}

