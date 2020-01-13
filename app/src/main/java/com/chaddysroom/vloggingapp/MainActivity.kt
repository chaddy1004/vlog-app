package com.chaddysroom.vloggingapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.hardware.camera2.*
import android.media.ImageReader
import android.media.MediaRecorder
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.provider.MediaStore
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import android.view.SurfaceHolder
import android.widget.Button
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.EasyPermissions
import java.io.*
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private val MAX_PREVIEW_WIDTH = 1920
    private val MAX_PREVIEW_HEIGHT = 1080

    // camera related initializations
    private lateinit var cameraDevice: CameraDevice
    private lateinit var captureSession: CameraCaptureSession
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private lateinit var CAMERA_CURRENT: String
    private val CAMERA_FRONT by lazy {
        getCameraIdforDirection(CameraCharacteristics.LENS_FACING_FRONT)
    }
    private val CAMERA_BACK by lazy {
        getCameraIdforDirection(CameraCharacteristics.LENS_FACING_BACK)
    }
    private val cameraManager by lazy {
        // Must wait for onCreate to finish. Therefore used lazy (lazy for val, lateinit for var)
        getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    // Companion object initialization
    companion object {
        const val REQUEST_CAMERA_PERMISSION = 100
        const val REQUEST_AUDIO_AND_STORAGE_PERMISSION = 101
        const val REQUEST_STORAGE_PERMISSION = 102
        const val TAG = "MainActivity"
        private val SENSOR_DEFAULT_ORIENTATION_DEGREES = 90
        private val SENSOR_INVERSE_ORIENTATION_DEGREES = 270
        private val DEFAULT_ORIENTATION = SparseIntArray().apply {
            append(Surface.ROTATION_0, 90)
            append(Surface.ROTATION_90, 0)
            append(Surface.ROTATION_180, 270)
            append(Surface.ROTATION_270, 180)
        }
        private val INVERSE_ORIENTATION = SparseIntArray().apply {
            append(Surface.ROTATION_0, 270)
            append(Surface.ROTATION_90, 180)
            append(Surface.ROTATION_180, 90)
            append(Surface.ROTATION_270, 0)
        }

    }

    // MediaRecorder Related Initialization
    private lateinit var currentVideoFile: File
    private var isCaptured: Boolean = false
    private var isRecording: Boolean = false
    private val mediaRecorder by lazy {
        MediaRecorder()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun hasCameraPermission(): Boolean {
        return EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)
    }

    private fun hasAudioPermission(): Boolean {
        return EasyPermissions.hasPermissions(this, Manifest.permission.RECORD_AUDIO)
    }

    private fun hasExStoragePermission(): Boolean {
        return EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }


    // Thread related initialization
    private lateinit var backgroundThread: HandlerThread
    private lateinit var backgroundHandler: Handler

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


    // Callbacks Initialization
    private val previewStateCallback =
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

    val surfaceReadyCallback = object : SurfaceHolder.Callback {
        override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {}
        override fun surfaceDestroyed(p0: SurfaceHolder?) {}

        override fun surfaceCreated(p0: SurfaceHolder?) {
            launchCamera()
        }
    }

    ///////////////////////
    // override functions//
    ///////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startBackgroundThread()
        surfaceView.holder.addCallback(surfaceReadyCallback)
        surfaceView.holder.setFixedSize(MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT)
        val cameraSwap_button = findViewById<Button>(R.id.cameraswap_button)
        cameraSwap_button.setOnClickListener {
            swapCameras()
        }
        val cameraRecord_button = findViewById<Button>(R.id.shutter_button)
        cameraRecord_button.setOnClickListener {
            if (!isRecording) {
                if (hasExStoragePermission() && hasAudioPermission()) { // Only start camera session once permission is granted
                    Log.d(TAG, "App has camera permission]")
                    recordingSession()
                } else {
                    val permissions =
                        arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    EasyPermissions.requestPermissions(
                        this,
                        getString(R.string.audio_request_rationale),
                        REQUEST_AUDIO_AND_STORAGE_PERMISSION,
                        *permissions
                    )
//                    EasyPermissions.requestPermissions(
//                        this,
//                        getString(R.string.exStorage_request_rationale),
//                        REQUEST_AUDIO_PERMISSION,
//                        Manifest.permission.RECORD_AUDIO
//                    )
                }
            } else if (isRecording) {
                stopMediaRecorder()
            }
        }
        CAMERA_CURRENT = CAMERA_BACK // Initializing CURRENT_CAMERA
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
    ///////////////////////
    // override functions//
    ///////////////////////


    ////////////////////
    //Various sessions//
    ////////////////////
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
        cameraDevice.createCaptureSession(mutableListOf(previewSurface), captureCallback, backgroundHandler)
    }

    private fun recordingSession() {
        setupMediaRecorder()

        val previewSurface = surfaceView.holder.surface
        val recordSurface = mediaRecorder.surface

        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
        captureRequestBuilder.addTarget(previewSurface)
        captureRequestBuilder.addTarget(recordSurface)

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
                    isRecording = true
                    mediaRecorder.start()
                }

            }
        }
        cameraDevice.createCaptureSession(
            mutableListOf(previewSurface, recordSurface),
            captureCallback,
            backgroundHandler
        )
        Toast.makeText(this, "RECORDING VIDEO AND AUDIO", Toast.LENGTH_LONG).show()
    }
    ////////////////////
    //Various sessions//
    ////////////////////


    /////////////////////////////
    // Camera related functions//
    /////////////////////////////
    private fun launchCamera() {
        if (hasCameraPermission()) { // Only start camera session once permission is granted
            Log.d(TAG, "App has camera permission]")
            startPreviewSession(CAMERA_BACK)
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.camera_request_rationale),
                REQUEST_CAMERA_PERMISSION,
                Manifest.permission.CAMERA
            )
            launchCamera()
        }
    }

    private fun startPreviewSession(cameraDirection: String) {
        try {
            if (cameraManager.cameraIdList.isEmpty()) {
                // no cameras
                Toast.makeText(this, "No Cameras available", Toast.LENGTH_SHORT).show()
            }
//            val cameraId = cameraDirection
            cameraManager.openCamera(cameraDirection, previewStateCallback, backgroundHandler)
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        } catch (e: SecurityException) {
            Log.e(TAG, e.toString())
        } catch (e: InterruptedException) {
            Log.e(TAG, "openCamera() interrupted while opened")
        }

    }

    private fun closeCamera() {
        if (this::captureSession.isInitialized)
            captureSession.close()
        if (this::cameraDevice.isInitialized)
            cameraDevice.close()
    }

    // Returns value of the speficied camera characteristic
    private fun <T> getSpecificCharacteristics(cameraId: String, key: CameraCharacteristics.Key<T>): T? {
        val characteristics =
            cameraManager.getCameraCharacteristics(cameraId) // Gets the characteristic for the specific camera ID
        var characteristic: T? = null
        try {
            characteristic = characteristics.get(key)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, e.toString())
        }
        return characteristic
    }

    private fun getCameraIdforDirection(desiredLens: Int): String {
        // Pass in desired ID for desired direction (rear or front facing)
        var cameraId = listOf<String>()
        try {
            val cameraIdList = cameraManager.cameraIdList
            cameraId = cameraIdList.filter {
                desiredLens == getSpecificCharacteristics(it, CameraCharacteristics.LENS_FACING)
            }
        } catch (e: CameraAccessException) {
            Log.e(TAG, e.toString())
        }
        return cameraId[0]
    }


    private fun swapCameras() {
        if (CAMERA_CURRENT == CAMERA_FRONT) {
            CAMERA_CURRENT = CAMERA_BACK
            closeCamera()
            startPreviewSession(CAMERA_CURRENT)
        } else if (CAMERA_CURRENT == CAMERA_BACK) {
            CAMERA_CURRENT = CAMERA_FRONT
            closeCamera()
            startPreviewSession(CAMERA_CURRENT)
        } else {
            throw IllegalStateException("CAMERA NOT OPENED")
        }
        return
    }
    /////////////////////////////
    // Camera related functions//
    /////////////////////////////

    ////////////////////////////////////
    // Video Capture Related Functions//
    ////////////////////////////////////
    private fun createVideoFile(): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val filepath = Environment.getExternalStorageDirectory()
        val filename = "VIDEO_${timestamp}.mp4"
        val dir = File(filepath.absolutePath + "/DCIM/" + "/VlogApp/")
        dir.mkdir()
        val file = File(dir, filename)
        return file
    }


    private fun setupMediaRecorder() {
        val rotation = this.windowManager?.defaultDisplay?.rotation
        val sensorOrientation = getSpecificCharacteristics(CAMERA_CURRENT, CameraCharacteristics.SENSOR_ORIENTATION)
        when (sensorOrientation) {
            SENSOR_DEFAULT_ORIENTATION_DEGREES -> mediaRecorder.setOrientationHint(DEFAULT_ORIENTATION.get(rotation!!))
            SENSOR_INVERSE_ORIENTATION_DEGREES -> mediaRecorder.setOrientationHint(INVERSE_ORIENTATION.get(rotation!!))
        }
        currentVideoFile = createVideoFile()
        mediaRecorder.apply {
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setAudioSource(MediaRecorder.AudioSource.CAMCORDER)
            setOutputFile(currentVideoFile)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setVideoEncodingBitRate(10000000)
            setVideoFrameRate(30)
            setVideoSize(1920, 1080)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            //Audio Source
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            setAudioEncodingBitRate(16 * 44100)
            setAudioSamplingRate(44100)
            try {
                prepare()
            } catch (e: IOException) {
                Log.e(TAG, e.toString())
            }
        }
    }

    private fun stopMediaRecorder() {
        mediaRecorder.apply {
            try {
                stop()
                reset()
            } catch (e: IllegalArgumentException) {
                Log.e(TAG, e.toString())
            }
        }
        isRecording = false
        Toast.makeText(this, "STOPPED RECORDING VIDEO AND AUDIO", Toast.LENGTH_LONG).show()
        startPreviewSession(CAMERA_CURRENT)
    }




}




