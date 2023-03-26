package com.otominlake.quietchime

import android.app.TimePickerDialog
import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.otominlake.quietchime.databinding.ActivityMainBinding
import kotlinx.serialization.Serializable
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit

const val TAG = "I7Y022"                                                        ///< tag for logging
const val CONFIG_FILENAME = "chimetime.json"                                    ///< configuration file name in private store

/**
 * Serializable class that contains all stored/restored app data
 *
 * @property chimeHour selected chime hour
 * @property chimeMin selected chime minute
 */
@Serializable
data class ChimeData(var chimeHour: Int, var chimeMin: Int)

class ChimeTask(private val hour: Int, private val min: Int, private val context: Context) : TimerTask() {
    private val timer = Timer()
    private var isCancelled = false
    private var mediaPlayer: MediaPlayer? = null

    init {
        val chimeTime = Calendar.getInstance()
        chimeTime.apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, min)
            set(Calendar.SECOND, 0)
        }
        if (chimeTime.time.before(Date())) {
            chimeTime.add(Calendar.DATE, 1)
        }
        Log.v(TAG, "Timer set to ${chimeTime.time}")
        timer.schedule(this, chimeTime.time, TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS))
    }

    override fun run() {
        if (!isCancelled) {

            try {
                Log.i(TAG, "Chime!")
                if (mediaPlayer == null) {
                    mediaPlayer = MediaPlayer.create(context, R.raw.bird)
                }
                mediaPlayer?.apply {
                    isLooping = false
                    start()
                }

            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        } else {
            Log.v(TAG, "I would chime but I was deleted")
            mediaPlayer?.release()
        }
    }

    fun delete() {
        isCancelled = true
        cancel()
        mediaPlayer?.release()
    }
}

/**
 * Main Activity class
 *
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val twoDigitsFormat = DecimalFormat("00")
    private var appData = ChimeData(8, 0)
    private var chimeTask: ChimeTask? = null

    /**
     * Activity has is being created
     *
     * @param savedInstanceState not used
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // load app data from file
        try {
            restoreData()
            setupChime()
        } catch (e: java.io.FileNotFoundException) {
            Log.v(TAG, "No config file, using defaults")
        } catch (e: Exception) {
            Log.e(TAG, "Exception $e")
        }

        // Select time button press
        binding.buttonSelectTime.setOnClickListener {
            // Open time picker
            TimePickerDialog(
                this,
                { _, pickedHour, pickedMin ->

                    // time has been select
                    appData.apply {
                        chimeHour = pickedHour
                        chimeMin = pickedMin
                    }
                    setupChime()
                },
                appData.chimeHour, appData.chimeMin, true
            ).show()
        }
    }

    /**
     * Set up chime based on [appData]: set textTime and timer task
     *
     */
    private fun setupChime() {
        binding.textTime.text = resources.getString(
            R.string.time_format, twoDigitsFormat.format(appData.chimeHour), twoDigitsFormat.format(appData.chimeMin)
        )

        // delete previous timer and set a new one
        chimeTask?.delete()
        chimeTask = ChimeTask(appData.chimeHour, appData.chimeMin, applicationContext)
    }

    /**
     * Activity is being stopped. Store data to file
     *
     */
    override fun onStop() {
        super.onStop()
        try {
            storeData()
            Log.v(TAG, "config stored")
        } catch (e: Exception) {
            Log.e(TAG, "Cannot store: exception is $e")
        }
    }

    /**
     * Serialize and store application data to a file
     *
     */
    private fun storeData() {
        // Serialize
        val json = Json.encodeToString(appData)

        // Write to a file
        val fileOutputStream: FileOutputStream = openFileOutput(CONFIG_FILENAME, Context.MODE_PRIVATE)
        fileOutputStream.write(json.toByteArray())
    }

    /**
     * Load application data from a file and deserialize
     *
     */
    private fun restoreData() {
        // Read from a file
        val fileInputStream: FileInputStream? = openFileInput(CONFIG_FILENAME)
        val inputStreamReader = InputStreamReader(fileInputStream)
        val bufferedReader = BufferedReader(inputStreamReader)
        val stringBuilder = StringBuilder()
        var text: String?

        while (run {
                text = bufferedReader.readLine()
                text
            } != null) {
            stringBuilder.append(text)
        }

        val fileContent = stringBuilder.toString()
        Log.d("I7Y022", fileContent)

        // Deserialize
        appData = Json.decodeFromString(fileContent)
    }
}