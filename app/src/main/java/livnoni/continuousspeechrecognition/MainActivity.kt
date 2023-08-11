package livnoni.continuousspeechrecognition

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity(), RecognitionListener {
    private val maxLinesInput = 10
    private var returnedText: TextView? = null
    private var toggleButton: ToggleButton? = null
    private var progressBar: ProgressBar? = null
    private var speech: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null
    private val LOG_TAG = "VoiceRecognition"
    private var listening = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        returnedText = findViewById<View>(R.id.textView1) as TextView
        progressBar = findViewById<View>(R.id.progressBar1) as ProgressBar
        toggleButton = findViewById<View>(R.id.toggleButton1) as ToggleButton
        toggleButton!!.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                listening = true
                start()
                progressBar!!.visibility = View.VISIBLE
                progressBar!!.isIndeterminate = true
                ActivityCompat.requestPermissions(
                    this@MainActivity, arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_RECORD_PERMISSION
                )
            } else {
                listening = false
                progressBar!!.isIndeterminate = false
                progressBar!!.visibility = View.INVISIBLE
                turnOf()
            }
        }
    }

    private fun start() {
        progressBar!!.visibility = View.INVISIBLE
        speech = SpeechRecognizer.createSpeechRecognizer(this)
        Log.i(LOG_TAG, "isRecognitionAvailable: " + SpeechRecognizer.isRecognitionAvailable(this))
        speech?.setRecognitionListener(this)
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        recognizerIntent!!.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
            "en"
        )
        recognizerIntent!!.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        recognizerIntent!!.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, maxLinesInput)
    }

    private fun turnOf() {
        speech!!.stopListening()
        speech!!.destroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_RECORD_PERMISSION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this@MainActivity, "start talk...", Toast.LENGTH_SHORT).show()
                speech!!.startListening(recognizerIntent)
            } else {
                Toast.makeText(this@MainActivity, "Permission Denied!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    public override fun onResume() {
        super.onResume()
    }

    override fun onReadyForSpeech(bundle: Bundle) {
        Log.i(LOG_TAG, "onReadyForSpeech")
    }

    override fun onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech")
        progressBar!!.isIndeterminate = false
        progressBar!!.max = 10
    }

    override fun onRmsChanged(rmsdB: Float) {
        Log.i(LOG_TAG, "onRmsChanged: $rmsdB")
        progressBar!!.progress = rmsdB.toInt()
        if (!listening) {
            turnOf()
        }
    }

    override fun onBufferReceived(bytes: ByteArray) {
        Log.i(LOG_TAG, "onBufferReceived: $bytes")
    }

    override fun onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech")
    }

    override fun onError(errorCode: Int) {
        val errorMessage = getErrorText(errorCode)
        Log.d(LOG_TAG, "FAILED $errorMessage")
        returnedText!!.text = errorMessage
        speech!!.startListening(recognizerIntent)
    }

    override fun onResults(results: Bundle) {
        Log.i(LOG_TAG, "onResults")
        val matches = results
            .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        var text = ""
        for (result in matches!!) text += """
     $result
     
     """.trimIndent()
        Log.i(LOG_TAG, "onResults=$text")
        returnedText!!.text = text
        speech!!.startListening(recognizerIntent)
    }

    override fun onPartialResults(results: Bundle) {
        Log.i(LOG_TAG, "onPartialResults")
        val matches = results
            .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        var text = ""
        for (result in matches!!) text += """
     $result
     
     """.trimIndent()
        Log.i(LOG_TAG, "onPartialResults=$text")
    }

    override fun onEvent(i: Int, bundle: Bundle) {
        Log.i(LOG_TAG, "onEvent")
    }

    private fun getErrorText(errorCode: Int): String {
        val message: String
        when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> message = "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> message = "Client side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> message = "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> message = "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> message = "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> message = "No match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                message = "RecognitionService busy"
                turnOf()
            }
            SpeechRecognizer.ERROR_SERVER -> message = "error from server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> message = "No speech input"
            else -> message = "Didn't understand, please try again."
        }
        return message
    }

    companion object {
        private const val REQUEST_RECORD_PERMISSION = 100
    }
}