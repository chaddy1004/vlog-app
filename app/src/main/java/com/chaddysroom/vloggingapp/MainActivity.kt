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
import android.os.HandlerThread
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.util.*

class MainActivity : AppCompatActivity() {
    private val MAX_PREVIEW_WIDTH = 1280
    private val MAX_PREVIEW_HEIGHT = 720
    private lateinit var captureSession: CameraCaptureSession
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private val cameraManager by lazy {
        // Must wait for onCreate to finish. Therefore used lazy
        getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }


    private lateinit var backgroundThread: HandlerThread
    private lateinit var backgroundHandler: Handler
    private lateinit var cameraDevice: CameraDevice
    private val deviceStateCallback =
        object : CameraDevice.StateCallback() { // Object expression, declares anonymous object
            override fun onOpened(camera: CameraDevice) {
                Log.d(TAG, "camera device opened")
                if (camera != null) {
                    cameraDevice = camera
                    previewSession()

                }
            }

            override fun onDisconnected(camera: CameraDevice) {
                Log.d(TAG, "camera deviced disconnected")
                camera?.close()
            }

            override fun onError(camera: CameraDevice, error: Int) {
                Log.d(TAG, "camera device error")
                this@MainActivity.finish()
            }

        }


    companion object {
        const val REQUEST_CAMERA_PERMISSION = 100
        const val TAG = "MainActivity"
    }


    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("Camera2").also { it.start() }
        backgroundHandler = Handler(backgroundThread.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread.quitSafely()
        try {
            backgroundThread.join()
        } catch (e: InterruptedException) {
            Log.e(TAG, e.toString())
        }
    }

    val surfaceReadyCallback = object : SurfaceHolder.Callback {
        override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {}
        override fun surfaceDestroyed(p0: SurfaceHolder?) {}

        override fun surfaceCreated(p0: SurfaceHolder?) {
            launchCamera()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startBackgroundThread()
        surfaceView.holder.addCallback(surfaceReadyCallback)

    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()

    }

    override fun onPause() {
        closeCamera()
        stopBackgroundThread()
        super.onPause()
    }

    override fun onDestroy() {
        closeCamera()
        stopBackgroundThread()
        super.onDestroy()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun hasCameraPermission(): Boolean {
        return EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)
    }


    private fun previewSession() {
        val previewSurface = surfaceView.holder.surface
        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequestBuilder.addTarget(previewSurface)

        val captureCallback = object : CameraCaptureSession.StateCallback() {
            override fun onConfigureFailed(session: CameraCaptureSession) {
                Log.e(TAG, "Creating capture session failed")
            }

            override fun onConfigured(session: CameraCaptureSession) {
                if (session != null) {
                    captureSession = session
                    captureRequestBuilder.set(
                        CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
                    )
                    captureSession.setRepeatingRequest(
                        captureRequestBuilder.build(),
                        object : CameraCaptureSession.CaptureCallback() {},
                        Handler { true })
                }
            }
        }

        cameraDevice.createCaptureSession(mutableListOf(previewSurface), captureCallback, null)

    }

    private fun launchCamera() {
        if (hasCameraPermission()) {
            Log.d(TAG, "App has camera permission]")
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


    private fun closeCamera() {
        if (this::captureSession.isInitialized)
            captureSession.close()
        if (this::cameraDevice.isInitialized)
            cameraDevice.close()
    }

    private fun startCameraSession() {
        try {
            if (cameraManager.cameraIdList.isEmpty()) {
                // no cameras
                Toast.makeText(this, "No Cameras available", Toast.LENGTH_SHORT).show()
            }
            val cameraIdList = cameraManager.cameraIdList
            val firstCamera = cameraIdList[0]
            cameraManager.openCamera(firstCamera, deviceStateCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        } catch (e: SecurityException) {
            Log.e(TAG, e.toString())
        } catch (eƒ: InterruptedException) {
            Log.e(TAG, "openCamera() interrupted while opened")
        }

    }

    private fun areDimensionsSwapped(displayRotation: Int, cameraCharacteristics: CameraCharacteristics): Boolean {
        when (displayRotation) {
            Surface.ROTATION_0, Surface.ROTATION_180 -> { // -> used to indicate condition
                if (cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 90 || cameraCharacteristics.get(
                        CameraCharacteristics.SENSOR_ORIENTATION
                    ) == 270
                ) {
                    return true
                }
            }
            Surface.ROTATION_90, Surface.ROTATION_270 -> {
                if (cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) == 0 || cameraCharacteristics.get(
                        CameraCharacteristics.SENSOR_ORIENTATION
                    ) == 180
                ) {
                    return true
                }
            }
            else -> {
                // invalid display rotation
            }
        }
        return false
    }

}




