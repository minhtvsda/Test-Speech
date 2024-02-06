package com.example.myapplication

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.databinding.FragmentFirstBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale


/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment(), OnInitListener {
    lateinit var speechIntent : Intent
    var lastMatchedPartials = ""
    val vietnameseLanguage = "vi"
    var englishLanguage = "en-US"

    val listener : RecognitionListener by lazy {
         object : RecognitionListener {
             var text = binding.textviewFirst
             override fun onReadyForSpeech(params: Bundle?) {
                 println("onReadyForSpeech")
             }

             override fun onBeginningOfSpeech() {
                 println("onBeginningOfSpeech")

             }

             override fun onRmsChanged(rmsdB: Float) {
//                println("onRmsChanged $rmsdB")

             }

             override fun onBufferReceived(buffer: ByteArray?) {
                 println("onBufferReceived")


             }

             override fun onEndOfSpeech() {
                 println("onEndOfSpeech")

             }

             override fun onError(error: Int) {
                 // Handle speech recognition errors
//                text.text = "Here come onError $error"
                 println("onError  $error")

                 startListeningAgain()

             }

             override fun onResults(results: Bundle?) {
                 // Extract the recognized text
                 val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                 if (!matches.isNullOrEmpty()) {
                     // Display the recognized text in a TextView or perform further actions
                     binding.textviewFirst2.text = matches[0]
                     var textAppend = matches[0].substring(lastMatchedPartials.length)
                     binding.textviewFirst.apply {
                         text = text.toString() + textAppend + ". "
                     }
                 }
                 println("Here come onResult")

                 startListeningAgain()
             }

             override fun onPartialResults(partialResults: Bundle?) {
                 val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                 println("Here come onPartialResults $matches")
                 if (!matches.isNullOrEmpty()) {
                     if (matches[0].isEmpty()) return
                     // Display the recognized text in a TextView or perform further actions
                     if (matches[0].length > lastMatchedPartials.length) {
                         var textAppend = matches[0].substring(lastMatchedPartials.length)
                         binding.textviewFirst.apply {
                             text = text.toString() + textAppend
                         }
                     }
                     lastMatchedPartials = matches[0]

                 }
             }

             override fun onEvent(eventType: Int, params: Bundle?) {
                 println("onEvent")

             }
         }
     }


    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    val audioString = "android.permission.RECORD_AUDIO"

    private var _binding: FragmentFirstBinding? = null
    lateinit var textToSpeech : TextToSpeech
    private lateinit var speechRecognizer: SpeechRecognizer
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        binding.buttonFirst.text = "Click to set recognizer"
        checkPermission()

        createSpeech()

        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonFirst.setOnClickListener {
            startSpeechRecognition()
        }

        textToSpeech = TextToSpeech(context, this)
        textToSpeech.language = Locale.US

// Set the pitch (1.0 is the default, values < 1.0 decrease pitch, values > 1.0 increase pitch)
        textToSpeech.setPitch(1.0f)
    }
    fun createSpeech() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

        speechRecognizer.setRecognitionListener(listener)
    }
    fun startListeningAgain() {
        Thread {
            // Code to be executed in the background thread
            // ...
            Thread.sleep(400)
            activity?.runOnUiThread {
                speechRecognizer.startListening(factorySpeechIntent(englishLanguage))
            }

            // Update the UI from the background thread
        }.start();
    }

    private fun factorySpeechIntent(language: String) : Intent = speechIntent.apply {
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
    }
    private fun startSpeechRecognition() {
        speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        speechIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, englishLanguage)
        speechIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, activity?.packageName)

        // Start speech recognition
        speechRecognizer.startListening(factorySpeechIntent(englishLanguage))
    }


    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(requireActivity(), audioString
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(), arrayOf<String>(audioString),
                REQUEST_RECORD_AUDIO_PERMISSION
            )
        } else {
            // Permission has already been granted
            // Initialize your SpeechRecognizer here

        }
    }




    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // TextToSpeech is initialized successfully
            // You can now use it to speak text
            binding.textviewFirst.setText("Here come success!!")

        } else {
            // Initialization failed, handle the error
        }
    }

    private fun speakText(text: String) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }
}