package utils

import android.content.Intent
import android.net.Uri
import java.io.File

private fun galleryAddPic(currentFile: File) {
    Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
        mediaScanIntent.data = Uri.fromFile(currentFile)
    }
}