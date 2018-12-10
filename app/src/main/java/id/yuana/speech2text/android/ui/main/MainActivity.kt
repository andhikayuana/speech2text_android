package id.yuana.speech2text.android.ui.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.support.v7.app.AppCompatActivity
import android.util.Log.d
import android.widget.Toast
import id.yuana.speech2text.android.R
import id.yuana.speech2text.android.data.remote.CarikApi
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val RC_SPEECH_INPUT = 12
        const val TAG: String = "MainActivity"
    }

    private var textToSpeech: TextToSpeech? = null

    @SuppressLint("LogNotTimber")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        btnSpeak.setOnClickListener { actionSpeak() }
        btnChangeInputLanguage.setOnClickListener { actionChangeInputLanguage() }

        textToSpeech = TextToSpeech(applicationContext, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {
                val lang = textToSpeech?.setLanguage(Locale("in", "ID"))

                if (lang == TextToSpeech.LANG_MISSING_DATA || lang == TextToSpeech.LANG_NOT_SUPPORTED) {
                    d(TAG, "Language not supported!")
                } else {
                    d(TAG, "Language Supported!")
                }

                d(TAG, "init success!")

            } else {
                d(TAG, "init failed!")
            }
        })
    }

    private fun actionChangeInputLanguage() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.apply {
            component = ComponentName(
                getString(R.string.pkg_google_quick_search),
                getString(R.string.cls_voice_search_pref)
            )
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.txt_not_support), Toast.LENGTH_SHORT).show()
        }
    }

    private fun actionSpeak() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.txt_speak_prompt))
        }

        try {
            startActivityForResult(intent, RC_SPEECH_INPUT)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, getString(R.string.txt_not_support), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SPEECH_INPUT && resultCode == Activity.RESULT_OK) {
            data?.extras?.let {
                val results = it[RecognizerIntent.EXTRA_RESULTS] as ArrayList<String>
                tvSpeakUp.text = results[0]
                requestToCarik(results[0])
            }
        }
    }

    private fun requestToCarik(msg: String) {

        GlobalScope.launch(Dispatchers.Main) {
            try {

                val response = async(Dispatchers.IO) {
                    CarikApi.request(msg)
                }.await()

                val data = response.body.getJSONObject("response").getJSONArray("text")[0] as String

                tvSpeakUp.text = data


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    textToSpeech?.speak(data, TextToSpeech.QUEUE_FLUSH, null, null)
                } else {
                    textToSpeech?.speak(data, TextToSpeech.QUEUE_FLUSH, null)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeech?.let {
            it.stop()
            it.shutdown()
        }
    }
}
