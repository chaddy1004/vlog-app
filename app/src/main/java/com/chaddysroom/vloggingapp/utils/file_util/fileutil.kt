package com.chaddysroom.vloggingapp.utils.file_util

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.io.File

fun galleryAddPic(currentFile: File, context: Context) {
    Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
        mediaScanIntent.data = Uri.fromFile(currentFile)
        context.sendBroadcast(mediaScanIntent)
    }
}