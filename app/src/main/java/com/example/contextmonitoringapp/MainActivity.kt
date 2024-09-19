package com.example.contextmonitoringapp

import android.Manifest
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.database.SQLException
import android.graphics.Bitmap
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Vibrator
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.pow

//import android.Manifest
//import android.content.ContentValues
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.database.SQLException
//import android.graphics.Bitmap
//import android.graphics.Color
//import android.hardware.Sensor
//import android.hardware.SensorEvent
//import android.hardware.SensorEventListener
//import android.hardware.SensorManager
//import android.media.MediaMetadataRetriever
//import android.net.Uri
//import android.os.Bundle
//import android.os.Environment
//import android.os.Vibrator
//import android.provider.MediaStore
//import android.view.View
//import android.widget.Button
//import android.widget.TextView
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import androidx.core.content.FileProvider
//import java.io.File


//import kotlinx.coroutines.withContext
//
//import android.content.ContentValues
//import android.content.pm.PackageManager
//import android.graphics.Bitmap
//import android.graphics.Color
//import android.os.Build
//import android.provider.MediaStore
//import android.util.Log
//import androidx.camera.core.CameraSelector
//import androidx.camera.core.ImageCapture
//import androidx.camera.core.ImageCaptureException
//import androidx.camera.core.Preview
//import androidx.camera.lifecycle.ProcessCameraProvider
//import androidx.camera.view.PreviewView
//import androidx.core.content.ContextCompat
//import androidx.lifecycle.LifecycleOwner
//import kotlinx.coroutines.Dispatchers
//import java.util.concurrent.ExecutorService
//import java.util.concurrent.Executors
//import kotlin.math.min
//
//class MainActivity : AppCompatActivity() , View.OnClickListener {
//    private lateinit var heartrate : Button
//    private lateinit var respiratoryrate : Button
//
//    private lateinit var cameraExecutor: ExecutorService
//    private lateinit var imageCapture: ImageCapture
//    private val cameraPermissionRequestCode = 1001
//    private val storagePermissionRequestCode = 1002
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_main)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//        heartrate = findViewById(R.id.heartrate)
//        respiratoryrate = findViewById(R.id.respiratoryrate)
//        if (allPermissionsGranted()) {
//            startCamera()
//        } else {
//            ActivityCompat.requestPermissions(
//                this,
//                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
//                cameraPermissionRequestCode
//            )
//        }
//        cameraExecutor = Executors.newSingleThreadExecutor()
//    }
//
//    private fun startCamera() {
//        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//
//        cameraProviderFuture.addListener({
//            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
//
//            // Get the PreviewView from the layout
//            val viewFinder = findViewById<PreviewView>(R.id.viewFinder)
//
//            // Build the Preview use case
//            val preview = Preview.Builder().build().also {
//                it.setSurfaceProvider(viewFinder.surfaceProvider)
//            }
//
//            // Build the ImageCapture use case
//            imageCapture = ImageCapture.Builder().build()
//
//            // Select back camera
//            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//
//            try {
//                // Unbind all use cases before binding again
//                cameraProvider.unbindAll()
//
//                // Bind the camera to the lifecycle
//                cameraProvider.bindToLifecycle(
//                    this as LifecycleOwner, cameraSelector, preview, imageCapture
//                )
//            } catch (exc: Exception) {
//                Log.e("CameraX", "Use case binding failed", exc)
//            }
//        }, ContextCompat.getMainExecutor(this))
//    }
//
//    private fun allPermissionsGranted() = arrayOf(
//        Manifest.permission.CAMERA,
//        Manifest.permission.WRITE_EXTERNAL_STORAGE
//    ).all {
//        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == cameraPermissionRequestCode || requestCode == storagePermissionRequestCode) {
//            if (allPermissionsGranted()) {
//                startCamera()
//            } else {
//                // Handle the case where permissions are not granted
//                Log.e("Permissions", "Permissions not granted")
//            }
//        }
//    }
//
//
//    suspend fun heartRateCalculator(uri: Uri, contentResolver: ContentResolver): Int {
//        return withContext(Dispatchers.IO) {
//            val result: Int
//            val proj = arrayOf(MediaStore.Images.Media.DATA)
//            val cursor = contentResolver.query(uri, proj, null, null, null)
//            val columnIndex =
//                cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
//            cursor?.moveToFirst()
//            val path = cursor?.getString(columnIndex?:0)
//            cursor?.close()
//
//            val retriever = MediaMetadataRetriever()
//            val frameList = ArrayList<Bitmap>()
//            try {
//                retriever.setDataSource(path)
//                val duration =
//                    retriever.extractMetadata(
//                        MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT
//                    )
//                val frameDuration = min(duration!!.toInt(), 425)
//                var i = 10
//                while (i < frameDuration) {
//                    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                        retriever.getFrameAtIndex(i)
//                    } else {
//
//                    }
//                    bitmap?.let { frameList.add(it) }
//                    i += 15
//                }
//            } catch (e: Exception) {
//                Log.d("MediaPath", "convertMediaUriToPath: ${e.stackTrace} ")
//            } finally {
//                retriever.release()
//                var redBucket: Long
//                var pixelCount: Long = 0
//                val a = mutableListOf<Long>()
//                for (i in frameList) {
//                    redBucket = 0
//                    for (y in 350 until 450) {
//                        for (x in 350 until 450) {
//                            val c: Int = i.getPixel(x, y)
//                            pixelCount++
//                            redBucket += Color.red(c) + Color.blue(c) +
//                                    Color.green(c)
//                        }
//                    }
//                    a.add(redBucket)
//                }
//                val b = mutableListOf<Long>()
//                for (i in 0 until a.lastIndex - 5) {
//                    val temp =
//                        (a.elementAt(i) + a.elementAt(i + 1) + a.elementAt(i + 2)
//                                + a.elementAt(
//                            i + 3
//                        ) + a.elementAt(
//                            i + 4
//                        )) / 4
//                    b.add(temp)
//                }
//                var x = b.elementAt(0)
//                var count = 0
//                for (i in 1 until b.lastIndex) {
//                    val p = b.elementAt(i)
//                    if ((p - x) > 3500) {
//                        count += 1
//                    }
//                    x = b.elementAt(i)
//                }
//                val rate = ((count.toFloat()) * 60).toInt()
//                result = (rate / 4)
//            }
//            result
//        }
//    }
//
//    private suspend fun capturePhoto() {
//        val imageCapture = imageCapture ?: return
//
//        // Create a file to save the image
//        val contentValues = ContentValues().apply {
//            put(MediaStore.Images.Media.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
//            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
//        }
//        val outputUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
//
//        if (outputUri != null) {
//            heartRateCalculator(outputUri,contentResolver)
////            imageCapture.takePicture(
////                ImageCapture.OutputFileOptions.Builder(contentResolver.openOutputStream(outputUri)!!).build(),
////                ContextCompat.getMainExecutor(this),
////                object : ImageCapture.OnImageSavedCallback {
////                    override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
////                        Log.d("CameraX", "Image saved: ${outputUri}")
////                        // You can use the outputUri here
////                    }
////
////                    override fun onError(exception: ImageCaptureException) {
////                        Log.e("CameraX", "Image capture failed: ${exception.message}", exception)
////                    }
////                })
//        } else {
//            Log.e("CameraX", "Failed to create output URI")
//        }
//    }
//
//    override fun onClick(v: View?) {
//        when(v?.id){
//            R.id.heartrate -> {
//
//            }
//            R.id.respiratoryrate -> {}
//        }
//    }
//    override fun onDestroy() {
//        super.onDestroy()
//        cameraExecutor.shutdown()
//    }
//}
//
////package com.example.contextmonitoringapp
////
////import android.content.ContentResolver
////import android.graphics.Bitmap
////import android.graphics.BitmapFactory
////import android.graphics.Color
////import android.os.Bundle
////import android.util.Log
////import android.view.View
////import android.widget.Button
////import androidx.appcompat.app.AppCompatActivity
////import androidx.camera.core.*
////import androidx.camera.lifecycle.ProcessCameraProvider
////import androidx.core.content.ContextCompat
////import androidx.lifecycle.LifecycleOwner
////import kotlinx.coroutines.CoroutineScope
////import kotlinx.coroutines.Dispatchers
////import kotlinx.coroutines.launch
////import kotlinx.coroutines.withContext
////import java.util.concurrent.ExecutorService
////import java.util.concurrent.Executors
////import androidx.camera.core.Preview
////import androidx.camera.view.PreviewView
////
////class MainActivity : AppCompatActivity(), View.OnClickListener {
////
////    private lateinit var heartrate: Button
////    private lateinit var respiratoryrate: Button
////    private lateinit var cameraExecutor: ExecutorService
////    private lateinit var imageCapture: ImageCapture
////
////    override fun onCreate(savedInstanceState: Bundle?) {
////        super.onCreate(savedInstanceState)
////        setContentView(R.layout.activity_main)
////
////        heartrate = findViewById(R.id.heartrate)
////        respiratoryrate = findViewById(R.id.respiratoryrate)
////
////        // Set click listeners
////        heartrate.setOnClickListener(this)
////        respiratoryrate.setOnClickListener(this)
////
////        // Initialize CameraX and start camera preview
////        startCamera()
////
////        // Initialize camera executor
////        cameraExecutor = Executors.newSingleThreadExecutor()
////    }
////
//////    private fun startCamera() {
//////        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
//////
//////        cameraProviderFuture.addListener({
//////            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
//////            val preview = Preview.Builder().build().also {
//////                it.setSurfaceProvider(findViewById(R.id.viewFinder).surfaceProvider)
//////            }
//////
//////            imageCapture = ImageCapture.Builder().build()
//////
//////            // Select back camera
//////            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
//////
//////            try {
//////                // Unbind all use cases before binding again
//////                cameraProvider.unbindAll()
//////
//////                // Bind the camera to the lifecycle
//////                cameraProvider.bindToLifecycle(
//////                    this as LifecycleOwner, cameraSelector, preview, imageCapture
//////                )
//////            } catch (exc: Exception) {
//////                Log.e("CameraX", "Use case binding failed", exc)
//////            }
//////        }, ContextCompat.getMainExecutor(this))
//////    }
////private fun startCamera() {
////    val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
////
////    cameraProviderFuture.addListener({
////        val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
////
////        // Cast the viewFinder to PreviewView
////        val viewFinder = findViewById<PreviewView>(R.id.viewFinder)
////
////        val preview = Preview.Builder().build().also {
////            it.setSurfaceProvider(viewFinder.surfaceProvider)
////        }
////
////        imageCapture = ImageCapture.Builder().build()
////
////        // Select back camera
////        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
////
////        try {
////            // Unbind all use cases before binding again
////            cameraProvider.unbindAll()
////
////            // Bind the camera to the lifecycle
////            cameraProvider.bindToLifecycle(
////                this as LifecycleOwner, cameraSelector, preview, imageCapture
////            )
////        } catch (exc: Exception) {
////            Log.e("CameraX", "Use case binding failed", exc)
////        }
////    }, ContextCompat.getMainExecutor(this))
////}
////    private suspend fun heartRateCalculator(bitmap: Bitmap): Int {
////        return withContext(Dispatchers.Default) {
////            var redBucket: Long
////            var pixelCount: Long = 0
////            val a = mutableListOf<Long>()
////
////            // Process the bitmap to calculate heart rate
////            redBucket = 0
////            for (y in 350 until 450) {
////                for (x in 350 until 450) {
////                    val c: Int = bitmap.getPixel(x, y)
////                    pixelCount++
////                    redBucket += Color.red(c) + Color.blue(c) + Color.green(c)
////                }
////            }
////            a.add(redBucket)
////
////            // Calculate heart rate using pixel changes
////            var x = a.elementAt(0)
////            var count = 0
////            for (i in 1 until a.size) {
////                val p = a.elementAt(i)
////                if ((p - x) > 3500) {
////                    count += 1
////                }
////                x = a.elementAt(i)
////            }
////            ((count.toFloat()) * 60).toInt() / 4
////        }
////    }
////
////    private fun capturePhoto() {
////        val imageCapture = imageCapture ?: return
////
////        // Set up image capture listener to capture the bitmap frame
////        imageCapture.takePicture(ContextCompat.getMainExecutor(this),
////            object : ImageCapture.OnImageCapturedCallback() {
////                override fun onCaptureSuccess(image: ImageProxy) {
////                    val bitmap = imageProxyToBitmap(image)
////                    CoroutineScope(Dispatchers.Main).launch {
////                        val heartRate = heartRateCalculator(bitmap)
////                        Log.d("HeartRate", "Calculated Heart Rate: $heartRate")
////                    }
////                    image.close() // Close the image once done
////                }
////
////                override fun onError(exception: ImageCaptureException) {
////                    Log.e("CameraX", "Image capture failed: ${exception.message}", exception)
////                }
////            })
////    }
////
////    override fun onClick(v: View?) {
////        when (v?.id) {
////            R.id.heartrate -> {
////                capturePhoto() // Capture a photo when heart rate button is clicked
////            }
////            R.id.respiratoryrate -> {
////                // Handle respiratory rate click event
////            }
////        }
////    }
////
////    // Function to convert ImageProxy to Bitmap
////    private fun imageProxyToBitmap(image: ImageProxy): Bitmap {
////        val buffer = image.planes[0].buffer
////        val bytes = ByteArray(buffer.remaining())
////        buffer.get(bytes)
////        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
////    }
////
////    override fun onDestroy() {
////        super.onDestroy()
////        cameraExecutor.shutdown()
////    }
////}

//}


////noinspection SuspiciousImport
//import android.R
//import android.content.ContentValues
//import android.content.Intent
//import android.database.SQLException
//import android.hardware.Sensor
//import android.hardware.SensorManager
//import android.media.MediaMetadataRetriever
//import android.net.Uri
//import android.os.Bundle
//import android.os.Vibrator
//import android.view.View
//import android.widget.Button
//import android.widget.TextView
//import android.widget.Toast
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//
//
//class MainActivity : AppCompatActivity() , View.OnClickListener {
//
//    private val cameraPermissionRequestCode = 100
//    private val videoRecordRequestCode = 101
//    private lateinit var fileUri: Uri
//    private lateinit var accelManage: SensorManager
//    private lateinit var senseAccel: Sensor
//
//    var start: Long = 0
//    var end: Long = 0
//
//    var metadata: MediaMetadataRetriever? = null
//    var accelValuesZ: ArrayList<Double> = ArrayList()
//    private val heartRate = 0
//    var vibrator: Vibrator? = null
//    var count: Int = 0
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
//        setContentView(R.layout.activity_main)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        val SLP = findViewById<Button>(R.id.uploadSignsSubmit)
//        val heartRateSubmit = findViewById<Button>(R.id.heartRateSubmit)
//        val respiratoryRateSubmit = findViewById<Button>(R.id.respiratoryRateSubmit)
//        val heartRate = findViewById<TextView>(R.id.heartRate)
//
//        val respiratoryRate = findViewById<TextView>(R.id.respiratoryRate)
//
//        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
//        SLP.setOnClickListener { v: View? ->
//            val intent =
//                Intent(applicationContext, SymptomLoggingPage::class.java)
//            try {
//                val values = ContentValues()
//                val b = Bundle()
//                values.put("heartRate", heartRate.text.toString())
//                values.put("respiratoryRate", respiratoryRate.text.toString())
//                b.putParcelable("Initial", values)
//                intent.putExtras(b)
//            } catch (e: SQLException) {
//                Toast.makeText(this@MainActivity, e.getMessage(), Toast.LENGTH_SHORT)
//                    .show()
//            }
//            startActivity(intent)
//        }
//    }
//
//    override fun onClick(p0: View?) {
//        TODO("Not yet implemented")
//    }
class MainActivity : AppCompatActivity() {
    private var accelManage: SensorManager? = null
    private var senseAccel: Sensor? = null

    private val VIDEO_RECORD_CODE = 101
    private val CAMERA_PERMISSION_CODE = 100
    var fileUri: Uri? = null
//    var metadata: MediaMetadataRetriever? = null
    lateinit var accelValuesZ: MutableList<Float>
    lateinit var accelValuesX: MutableList<Float>
    lateinit var accelValuesY: MutableList<Float>
//    private var heartRate = 0
    var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val SLP = findViewById<Button>(R.id.uploadSignsSubmit)
        val heartRateSubmit = findViewById<Button>(R.id.heartRateSubmit)
        val respiratoryRateSubmit = findViewById<Button>(R.id.respiratoryRateSubmit)
        val heartRate = findViewById<TextView>(R.id.heartRate)

        val respiratoryRate = findViewById<TextView>(R.id.respiratoryRate)

        vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
        accelValuesZ = mutableListOf()
        accelValuesY = mutableListOf()
        accelValuesX = mutableListOf()
        accelManage = getSystemService(SENSOR_SERVICE) as SensorManager
        senseAccel = accelManage!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)


        SLP.setOnClickListener { v: View? ->
            val intent =
                Intent(applicationContext, SymptomLoggingPage::class.java)
            try {
                val values = ContentValues()
                val b = Bundle()
                values.put("heartRate", heartRate.text.toString())
                values.put("respiratoryRate", respiratoryRate.text.toString())
                b.putParcelable("Initial", values)
                intent.putExtras(b)
            } catch (e: SQLException) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_SHORT)
                    .show()
            }
            startActivity(intent)
        }



        respiratoryRateSubmit.setOnClickListener { v: View? ->
            val thread = Thread(CalcRespRateThread())
            Toast.makeText(
                applicationContext,
                "Please Lie Down and Place the phone on chest.",
                Toast.LENGTH_SHORT
            ).show()
            thread.start()
        }

        heartRateSubmit.setOnClickListener { v: View? ->
            cameraPermission
            val mediaFile = File(
                Environment.getExternalStorageDirectory()
                    .toString() + "/Android/data/com.example.contextmonitoringapp/files/fingerTip.mp4"
            )
            fileUri = FileProvider.getUriForFile(
                applicationContext, "com.example.contextmonitoringapp.provider",
                mediaFile
            )
            val intent =
                Intent(MediaStore.ACTION_VIDEO_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            println(fileUri?.path)
            startActivityForResult(intent, VIDEO_RECORD_CODE)
        }
    }

    private val cameraPermission: Unit
        get() {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_DENIED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_CODE
                )
            }
        }

    @RequiresApi(Build.VERSION_CODES.P)
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == VIDEO_RECORD_CODE) {
            if (resultCode == RESULT_OK) {
                val videoPath = data!!.data
                println("###################$videoPath")
                val heartRate = findViewById<TextView>(R.id.heartRate)
                heartRate.text = "Calculating..."
                GlobalScope.launch {
                    val uri = Uri.parse(videoPath.toString())
                    val heartRate = heartRateCalculator(uri, contentResolver)
                    println("#################################")
                    println(heartRate)
                    runOnUiThread {
                        val heartRateTextView =
                            findViewById<View>(R.id.heartRate) as TextView
                        heartRateTextView.text = heartRate.toString() + " bpm"
                    }
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(applicationContext, "Recording Cancelled", Toast.LENGTH_SHORT).show()
                println("Error Cancelled")
            } else {
                Toast.makeText(applicationContext, "Recording Cancelled Else", Toast.LENGTH_SHORT)
                    .show()
                println("Error Cancelled 1")
            }
        }
    }

    private inner class CalcRespRateThread : Runnable {
        override fun run() {
            val start = System.currentTimeMillis()
            val end = start + 45 * 1000

            // Sleep before vibration to simulate waiting period
            Thread.sleep(3000)

            // Vibrate to notify user
            vibrator?.vibrate(500)

            // Sleep to allow for vibration duration
            Thread.sleep(2000)

            val set = object : SensorEventListener {
                override fun onSensorChanged(event: SensorEvent?) {
                    event?.let {
                        if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                            accelValuesX.add(it.values[0]) // Collect X-axis data
                            accelValuesY.add(it.values[1]) // Collect Y-axis data
                            accelValuesZ.add(it.values[2]) // Collect Z-axis data

                            println("#################################")
                            println("AccelData X: ${it.values[0]}, Y: ${it.values[1]}, Z: ${it.values[2]}")
                        }
                    }

                    // Check if the collection period is over
                    if (System.currentTimeMillis() > end) {
                        accelManage!!.unregisterListener(this)

                        // Calculate respiratory rate using helper function
                        val respiratoryRate = respiratoryRateCalculator(accelValuesX, accelValuesY, accelValuesZ)

                        // Vibrate to notify the user that the calculation is done
                        vibrator?.vibrate(500)
                        println("Respiratory rate: ${respiratoryRate}")
                        runOnUiThread {
                            findViewById<TextView>(R.id.respiratoryRate).text = "$respiratoryRate breaths per minute"
                        }
                    }
                }

                override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            }

            accelManage!!.registerListener(set, senseAccel, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }
    //Heart rate calculator helper code
    @RequiresApi(Build.VERSION_CODES.P)
    suspend fun heartRateCalculator(uri: Uri, contentResolver: ContentResolver): Int {
        return withContext(Dispatchers.IO) {
            val result: Int

            // Use a file descriptor to read the video instead of converting the Uri to a path
            val retriever = MediaMetadataRetriever()
            val frameList = ArrayList<Bitmap>()
            try {
                val parcelFileDescriptor = contentResolver.openFileDescriptor(uri, "r")
                parcelFileDescriptor?.use {
                    retriever.setDataSource(it.fileDescriptor)
                }

                val duration = retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT
                )?.toIntOrNull()

                if (duration != null) {
                    val frameDuration = min(duration, 425)
                    var i = 10
                    while (i < frameDuration) {
                        val bitmap = retriever.getFrameAtIndex(i)
                        bitmap?.let { frameList.add(it) }
                        i += 15
                    }
                }
            } catch (e: Exception) {
                Log.d("MediaPath", "Error processing video: ${e.stackTraceToString()}")
            } finally {
                retriever.release()
                var redBucket: Long
                var pixelCount: Long = 0
                val a = mutableListOf<Long>()
                for (i in frameList) {
                    redBucket = 0
                    for (y in 350 until 450) {
                        for (x in 350 until 450) {
                            val c: Int = i.getPixel(x, y)
                            pixelCount++
                            redBucket += Color.red(c) + Color.blue(c) + Color.green(c)
                        }
                    }
                    a.add(redBucket)
                }

                val b = mutableListOf<Long>()
                for (i in 0 until a.lastIndex - 5) {
                    val temp = (a[i] + a[i + 1] + a[i + 2] + a[i + 3] + a[i + 4]) / 4
                    b.add(temp)
                }

                var x = if (b.isNotEmpty()) b[0] else 0
                var count = 0
                for (i in 1 until b.lastIndex) {
                    val p = b[i]
                    if ((p - x) > 3500) {
                        count++
                    }
                    x = b[i]
                }

                val rate = (count.toFloat() * 60).toInt()
                result = rate / 4
            }

            return@withContext result
        }
    }


    // Respiratory rate calculation helper function
    fun respiratoryRateCalculator(
        accelValuesX: MutableList<Float>,
        accelValuesY: MutableList<Float>,
        accelValuesZ: MutableList<Float>,
    ): Int {
        var previousValue = 10f
        var k = 0
        for (i in 11 until accelValuesY.size) {
            val currentValue = kotlin.math.sqrt(
                accelValuesZ[i].toDouble().pow(2.0) + accelValuesX[i].toDouble().pow(2.0) + accelValuesY[i].toDouble().pow(2.0)
            ).toFloat()
            if (abs(previousValue - currentValue) > 0.15) {
                k++
            }
            previousValue = currentValue
        }
        val ret = k.toDouble() / 45.00
        return (ret * 30).toInt()
    }
}
