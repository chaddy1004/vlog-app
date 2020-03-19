package com.chaddysroom.vloggingapp.activity

import android.content.Intent
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.content.FileProvider
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import com.chaddysroom.vloggingapp.R
import java.io.File
import android.os.StrictMode
import android.widget.*


class PictureViewActivity : AppCompatActivity() {
    private lateinit var message: String

    private fun initButtons() {
        val mailShareButton = findViewById<ImageButton>(R.id.shareButton)

        //Technically should not do this since it is a security hazard... apprently
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        mailShareButton.setOnClickListener {
            val sendIntent = Intent()
            Toast.makeText(this, this.message, Toast.LENGTH_LONG).show()
            val uri = Uri.fromFile(File(this@PictureViewActivity.message))
//            val uri = FileProvider.getUriForFile(applicationContext, packageName+".fileprovider", File(this@PictureViewActivity.message));

            sendIntent.apply {
                action = Intent.ACTION_SEND
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                putExtra(Intent.EXTRA_STREAM, uri)
                type = "image/*"
//                putExtra(Intent.EXTRA_TEXT, "By Lumetix 2020")
//                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_picture_view)
        this.message = intent.getStringExtra("uriToFile")
        initButtons()
        val pictureView = findViewById<ImageView>(R.id.imagePreview)
        pictureView.setImageURI(Uri.parse(this.message))
        Log.i("ACTI", this.message)
    }
}
