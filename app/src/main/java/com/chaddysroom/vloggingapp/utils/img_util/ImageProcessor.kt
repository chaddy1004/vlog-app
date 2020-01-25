package com.chaddysroom.vloggingapp.utils.img_util

import android.media.Image
import android.media.ImageReader
import android.util.Log

class ImageProcessor : ImageReader.OnImageAvailableListener {
    init{
        System.loadLibrary("native-img_processing")
    }

    external fun helloworld() : String
    override fun onImageAvailable(reader: ImageReader?) {
        var img = reader!!.acquireLatestImage()
//        Log.i("IMAGEPROCESSOR", "Latest Image Received")
//        Log.i("FORMAT", reader.imageFormat.toString())
        Log.i("NDK", helloworld())

        processImg(img = img)
        img.close()
    }

    private fun processImg(img: Image): Image {
        // Your image processing code goes here
        var img_processed = img
        Log.i("processImg", "Img processed")
        return img_processed
    }
}

