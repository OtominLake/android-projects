package com.otominlake.test.anote

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.otominlake.test.anote.databinding.ActivityMainBinding
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader

@Serializable
data class NoteData(val noteContent: String)

class MainActivity : AppCompatActivity() {
    private val _tag = "ANoteApp"
    private val _filename = "anotedata.json"
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    // Serialize and store application data to a file
    private fun storeData() {
        Log.v(_tag, "storeData")

        try {
            // Collect data
            val appData = NoteData(binding.edNote.text.toString())

            // Serialize
            val json = Json.encodeToString(appData)

            // Write to a file
            val fileOutputStream : FileOutputStream = openFileOutput(_filename, Context.MODE_PRIVATE)
            fileOutputStream.write(json.toByteArray())

        } catch (e: java.lang.Exception) {
            Log.e(_tag, "storeData exeption: ${e.message}")
        }
    }

    // Load application data from a file and deserialize
    private fun restoreData() {
        Log.v(_tag, "restoreData")
        var restoredData = NoteData("EMPTY")

        try {
            // Read from a file
            var fileInputStream : FileInputStream? = null
            fileInputStream = openFileInput(_filename)
            var inputStreamReader = InputStreamReader(fileInputStream)
            val bufferedReader = BufferedReader(inputStreamReader)
            val stringBuilder = StringBuilder()
            var text : String? = null

            while ( { text = bufferedReader.readLine(); text}() != null) {
                stringBuilder.append(text)
            }

            val fileContent = stringBuilder.toString()

            // Deserialize
            restoredData = Json.decodeFromString<NoteData>(fileContent)

        } catch (e: java.lang.Exception) {
            Log.w(_tag, "restoreData Exception: ${e.message}")
        }

        // Set note content
        binding.edNote.setText(restoredData.noteContent)
    }

    override fun onStart() {
        Log.v(_tag, "onStart called")
        super.onStart()
        restoreData()
    }

    override fun onStop() {
        Log.v(_tag, "onStop called")
        super.onStop()
        storeData()
    }
}