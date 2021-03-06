package com.chaddysroom.vloggingapp.utils.img_util

import android.content.Context
import android.graphics.ImageFormat
import android.media.Image
import android.media.ImageReader
import android.media.ImageWriter
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.SurfaceView
import android.view.View
import org.opencv.core.CvType.*
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgcodecs.Imgcodecs
import java.io.File
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import com.chaddysroom.vloggingapp.utils.file_util.galleryAddPic
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.IllegalStateException

open class ImageProcessor(surface: SurfaceView, context: Context, currentCamera: Boolean) :
    ImageReader.OnImageAvailableListener, View.OnClickListener {
    init {
        System.loadLibrary("native-img_processing")
    }

    open fun onCaptureCallback() {

    }

    private val mSurface = surface
    external fun YUV2RGB(
        srcWidth: Int,
        srcHeight: Int,
        YUVaddr: ByteBuffer,
        matptr: Long,
        frontCamera: Boolean
    ): Int

    external fun Grayscale2Surface(
        srcWidth: Int,
        srcHeight: Int,
        YUVaddr: ByteBuffer,
        dirName: String,
        matptr: Long
    ): Int

    //    private lateinit var img : Image
    private lateinit var planes: Array<Image.Plane>
    private var imgheight = 0
    private var imgwidth = 0
    private var capture = false
    private val context = context
    var currentCamera = currentCamera
    lateinit var imageWriter: ImageWriter
    private lateinit var latestFile : String
    private var lastFile = "LASTFILE"

    fun getLatestFile(): String {
        return latestFile
    }

    override fun onClick(v: View?) {
        if (!capture) {
            capture = true
        }
    }

    fun isinitialized(): Boolean{
        return this::latestFile.isInitialized
    }

    fun isChanged():Boolean{
        Log.i("isChanged_last", lastFile.toString())
        Log.i("isChanged_latest", latestFile.toString())
        return lastFile != latestFile
    }

    override fun onImageAvailable(reader: ImageReader?) {
        val img = reader?.acquireLatestImage() ?: return

//        val message = ""
//        Log.i("IMAGEPROCESSOR", "Latest Image Received")
//        Log.i("FORMAT", reader.imageFormat.toString())
////        Log.i("HEIGHT", img.height.toString())
////        Log.i("WIDTH", img.width.toString())
//        if (img.format != ImageFormat.YUV_420_888) {
//            Log.e("ImageProcessor", "NOT_YUV_420_888")
//        }
        if (capture) {

            if (this::latestFile.isInitialized){

                lastFile = latestFile
                Log.e("INSIDE CAPTURE_last", lastFile)
                Log.e("INSIDE CAPTURE_lastest", latestFile)
//                Log.i("isChanged_last", lastFile.toString())
            }
            latestFile = save2file(planes = img.planes, height = img.height, width = img.width)
            Log.e("INSIDE CAPTURE_lastest_out", latestFile)
            capture = false
        }
        Log.i("ONIMG_h", img.height.toString())
        Log.i("ONIMG_w", img.width.toString())
        if (imageWriter != null) {
            Log.e("IMAGE", img.format.toString())
            Log.e("IMAGE_WRITER", imageWriter.format.toString())
            imageWriter.queueInputImage(img)
        }
//        Log.e("RECEIVED", currentCamera.toString())
        img.close()
    }


    private fun save2file(planes: Array<Image.Plane>, height: Int, width: Int): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val filepath = Environment.getExternalStorageDirectory()
        val filename = "picture_${timestamp}.png"
        val dir = File(filepath.absolutePath + "/DCIM/" + "/VlogApp/")
        val file = File(dir, filename)
        // Your image processing code goes here
        val plane0 = planes[0]
        var result = -1
        val mat = Mat(height, width, CV_8UC4, Scalar(0.0));
        YUV2RGB(width, height, plane0.buffer, mat.nativeObjAddr, currentCamera)
        val hello = Imgcodecs.imwrite(file.toString(), mat)
        galleryAddPic(file, context = context)
        return file.toString()
    }

    private fun render2surface(planes: Array<Image.Plane>, height: Int, width: Int): String {

        return "asdf"
    }
}

