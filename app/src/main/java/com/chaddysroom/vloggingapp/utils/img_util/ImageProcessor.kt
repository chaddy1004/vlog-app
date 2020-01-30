package com.chaddysroom.vloggingapp.utils.img_util

import android.media.Image
import android.media.ImageReader
import android.util.Log
import java.nio.ByteBuffer

class ImageProcessor : ImageReader.OnImageAvailableListener {
    init{
        System.loadLibrary("native-img_processing")
    }

    external fun helloworld() : String
    external fun receive(bytebuffer: ByteBuffer, size: Int): String
    external fun toMat(bytebuffer: ByteBuffer, height: Int, width: Int): String

    override fun onImageAvailable(reader: ImageReader?) {
        val img = reader!!.acquireLatestImage()
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
        lateinit var plane_bytes : ByteArray

//        if (plane0.buffer.hasArray()){
//            plane0.buffer.get(plane_bytes)
//        }
//        else
//            return "NOTING HERE"
//
        if(plane0.buffer.isDirect) {
            Log.w("processImg", "BUFFER IS DIRECT~~!!!!!!!!!")
        }
        else
        {
            Log.w("processImg", "BUFFER NOT DIRECT~~!!!!!!!!!")
        }
//        plane0.buffer.get(plane_bytes)
        var message = ""
        Log.i("processImg", "Img processed")
//        message = receive(plane0.buffer, 40)
        message = toMat(plane0.buffer, height, width)
        return message
    }
}

