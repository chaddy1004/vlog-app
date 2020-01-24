package com.chaddysroom.vloggingapp.activity

import android.Manifest
import android.content.Context
import android.graphics.*
import android.hardware.camera2.*
import android.media.ImageReader
import android.media.MediaRecorder
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import com.chaddysroom.vloggingapp.R
import com.chaddysroom.vloggingapp.utils.draw_util.SurfaceViewDraw
import com.chaddysroom.vloggingapp.utils.draw_util.drawRect
import kotlinx.android.synthetic.main.activity_main.*
import pub.devrel.easypermissions.EasyPermissions
import java.io.*
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import java.text.SimpleDateFormat
import java.util.*
import com.chaddysroom.vloggingapp.utils.file_util.galleryAddPic
import com.chaddysroom.vloggingapp.utils.img_util.ImageProcesser

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
    private val surfaceDrawer by lazy {
        SurfaceViewDraw(overlayView, this@MainActivity)
    }

    private var rotationMatrix = Matrix()
    private var imageProcessor = ImageProcesser()


    // Companion object initialization
    companion object {
        const val REQUEST_CAMERA_PERMISSION = 100
        const val REQUEST_AUDIO_AND_STORAGE_PERMISSION = 101
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
    private var isRecording: Boolean = false
    private val mediaRecorder by lazy {
        MediaRecorder()
    }

    private val imageReader by lazy {
        ImageReader.newInstance(1920, 1080, ImageFormat.YUV_420_888, 5)
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


    val overlayReadyCallback = object : SurfaceHolder.Callback {
        override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {}
        override fun surfaceDestroyed(p0: SurfaceHolder?) {}
        override fun surfaceCreated(p0: SurfaceHolder?) {
        }
    }



    private val faceDetectorCallback = object : CameraCaptureSession.CaptureCallback() {
        override fun onCaptureCompleted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            super.onCaptureCompleted(session, request, result)
            surfaceDrawer.clearScreen()
            val faces = result.get(CaptureResult.STATISTICS_FACES)
            Log.i("FACESSSSSSSS", faces!!.size.toString())
            val displayRotation = this@MainActivity.windowManager?.defaultDisplay?.rotation
            val height = overlayView.height
            val width = overlayView.width
            val activeArraySizeRect =
                getSpecificCharacteristics(CAMERA_CURRENT, CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)!!
            val sensorOrientation =
                getSpecificCharacteristics(CAMERA_CURRENT, CameraCharacteristics.SENSOR_ORIENTATION)!!
            for (face in faces) { //!! is a null check operator. If it is null, it will throw null pointer exception
                val bounds = face.bounds
                val boundsF = RectF(bounds)
                if (displayRotation == 0) {
                    rotationMatrix.setRotate(sensorOrientation.toFloat())
                    rotationMatrix.postScale(-1f, 1f)
                    rotationMatrix.postTranslate(height.toFloat() - 200, 2 * width.toFloat() + 400)
                }
                Log.e("RECTANGLE BEFORE", boundsF.toString())
                rotationMatrix.mapRect(boundsF)
                Log.e("RECTANGLE AFTER", boundsF.toString())
                // Transformation
                surfaceDrawer.drawBoundingBox(boundingBox = boundsF)
            }
        }
    }

    ///////////////////////
    // override functions//
    ///////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startBackgroundThread()
        cameraView.holder.addCallback(surfaceReadyCallback)
        cameraView.holder.setFixedSize(MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT)

        overlayView.setZOrderMediaOverlay(true)
        overlayView.holder.setFormat(PixelFormat.TRANSPARENT)

        val cameraSwap_button = findViewById<ImageButton>(R.id.cameraswap_button)
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
                }
            } else if (isRecording) {
                stopMediaRecorder()
            }
        }


        imageReader.setOnImageAvailableListener(imageProcessor, backgroundHandler)
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
        val previewSurface = cameraView.holder.surface
        val imgReaderSurface = imageReader.surface

        captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequestBuilder.addTarget(previewSurface)
        captureRequestBuilder.addTarget(imgReaderSurface)
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
                    captureRequestBuilder.set(
                        CaptureRequest.STATISTICS_FACE_DETECT_MODE,
                        CaptureRequest.STATISTICS_FACE_DETECT_MODE_FULL
                    )
                    captureSession.setRepeatingRequest(
                        captureRequestBuilder.build(),
                        faceDetectorCallback,
                        Handler { true })
                }
            }
        }
        cameraDevice.createCaptureSession(mutableListOf(previewSurface, imgReaderSurface), captureCallback, backgroundHandler)
    }

    private fun recordingSession() {
        setupMediaRecorder()
        val previewSurface = cameraView.holder.surface
        val recordSurface = mediaRecorder.surface
        val imgReaderSurface = imageReader.surface


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
            connectWithCamera(CAMERA_BACK)
        } else {
            EasyPermissions.requestPermissions(
                this,
                getString(R.string.camera_request_rationale),
                REQUEST_CAMERA_PERMISSION,
                Manifest.permission.CAMERA
            )
            connectWithCamera(CAMERA_BACK)
        }
    }

    private fun connectWithCamera(cameraDirection: String) {
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
        if (this::captureSession.isInitialized){
            captureSession.close()
        }

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
            connectWithCamera(CAMERA_CURRENT)
        } else if (CAMERA_CURRENT == CAMERA_BACK) {
            CAMERA_CURRENT = CAMERA_FRONT
            closeCamera()
            connectWithCamera(CAMERA_CURRENT)
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
            SENSOR_DEFAULT_ORIENTATION_DEGREES -> mediaRecorder.setOrientationHint(
                DEFAULT_ORIENTATION.get(rotation!!)
            )
            SENSOR_INVERSE_ORIENTATION_DEGREES -> mediaRecorder.setOrientationHint(
                INVERSE_ORIENTATION.get(rotation!!)
            )
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
            start()
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
//        connectWithCamera(CAMERA_CURRENT)
        previewSession()
        galleryAddPic(currentVideoFile, this@MainActivity)
    }

}




