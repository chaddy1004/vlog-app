package com.chaddysroom.vloggingapp.utils.img_util

import android.media.Image
import android.media.ImageReader
import android.util.Log

class ImageProcesser : ImageReader.OnImageAvailableListener {
    override fun onImageAvailable(reader: ImageReader?) {
        var img = reader!!.acquireLatestImage()
        Log.i("IMAGEPROCESSOR", "Latest Image Received")
        Log.i("FORMAT", reader.imageFormat.toString())

        processImg(img = img)
        img.close()
    }

    private fun processImg(img: Image): Image {
        // Your image processing code goes here
        var img_processed = img
        return img_processed
    }
}

