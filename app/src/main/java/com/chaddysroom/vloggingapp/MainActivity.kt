package com.chaddysroom.vloggingapp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.widget.Toast
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_CAMERA_PERMISSION = 100
        const val TAG = "MainActivity"
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        launchCamera()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun hasCameraPermission(): Boolean {
        return EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)
    }


    private fun launchCamera() {
        if (hasCameraPermission()) {
            Log.d(TAG, "App as camera permission]")
            startCameraSession()
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.camera_request_rationale),
                REQUEST_CAMERA_PERMISSION,
                Manifest.permission.CAMERA
            )
        }
    }

    private fun startCameraSession(){
        if (hasCameraPermission()) {
            val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            if (cameraManager.cameraIdList.isEmpty()) {
                // no cameras
                Toast.makeText(this, "No Cameras available", Toast.LENGTH_SHORT).show()
            }
            val cameraIdList = cameraManager.cameraIdList
            val firstCamera = cameraIdList[0]


        }

    }


}




