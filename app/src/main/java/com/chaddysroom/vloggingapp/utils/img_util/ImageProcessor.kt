package com.chaddysroom.vloggingapp.utils.img_util

import android.content.Context
import android.graphics.ImageFormat
import android.media.Image
import android.media.ImageReader
import android.os.Environment
import android.util.Log
import android.view.SurfaceView
import android.view.View
import org.opencv.core.CvType.*
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.imgcodecs.Imgcodecs
import java.io.File
import java.lang.IllegalArgumentException
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import com.chaddysroom.vloggingapp.utils.file_util.galleryAddPic

class ImageProcessor(surface: SurfaceView, context: Context) : ImageReader.OnImageAvailableListener, View.OnClickListener{
    init {
        System.loadLibrary("native-img_processing")
    }

    private val mSurface = surface
    external fun helloworld(): String
    external fun receive(bytebuffer: ByteBuffer, size: Int): String
    external fun toMat(bytebuffer: ByteBuffer, height: Int, width: Int): String
    external fun YUV2RGB(srcWidth: Int, srcHeight: Int, YUVaddr: ByteBuffer, dirName: String, matptr: Long): Int
    external fun Grayscale2Surface(srcWidth: Int, srcHeight: Int, YUVaddr: ByteBuffer, dirName: String, matptr: Long): Int
//    private lateinit var img : Image
    private lateinit var planes: Array<Image.Plane>
    private var imgheight = 0
    private var imgwidth = 0
    private var capture = false
    private val context = context
    override fun onClick(v: View?) {
        if (!capture){
            capture = true
        }
    }

    override fun onImageAvailable(reader: ImageReader?) {
        val img = reader?.acquireLatestImage() ?: return


        val message = ""
        Log.i("IMAGEPROCESSOR", "Latest Image Received")
        Log.i("FORMAT", reader.imageFormat.toString())
//        Log.i("HEIGHT", img.height.toString())
//        Log.i("WIDTH", img.width.toString())
        if (img.format != ImageFormat.YUV_420_888) {
            Log.e("ImageProcessor", "NOT_YUV_420_888")
        }
        if (capture){
            save2file(planes=img.planes, height=img.height, width = img.width)
            capture = false
        }
        Log.e("RECEIVED", message)
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
        var result = -1;
        val mat = Mat(height, width, CV_8UC4, Scalar(0.0));
        Log.i("HEIGHT BEFORE", mat.height().toString())
        Log.i("WIDTH BEFORE", mat.width().toString())
        YUV2RGB(width, height, plane0.buffer, "asdf" ,mat.nativeObjAddr)
        Log.i("HEIGHT AFTER", mat.height().toString())
        Log.i("WIDTH AFTER", mat.width().toString())
        val hello = Imgcodecs.imwrite(file.toString(), mat)
        galleryAddPic(file, context = context)
        return "DoneProcessing with $result $hello"
    }

    private fun render2surface(planes: Array<Image.Plane>, height:Int, width:Int):String{

        return "asdf"
    }
}

