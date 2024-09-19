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

class MainActivity : AppCompatActivity() {
    private var accelManage: SensorManager? = null
    private var senseAccel: Sensor? = null

    private val VIDEO_RECORD_CODE = 101
    private val CAMERA_PERMISSION_CODE = 100
    var fileUri: Uri? = null
    lateinit var accelValuesZ: MutableList<Float>
    lateinit var accelValuesX: MutableList<Float>
    lateinit var accelValuesY: MutableList<Float>
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
