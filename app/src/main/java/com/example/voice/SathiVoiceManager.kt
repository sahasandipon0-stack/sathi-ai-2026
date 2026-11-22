package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

private const val TAG = "SathiVoiceManager"

data class VoiceStatus(
    val isListening: Boolean = false,
    val partialTranscript: String = "",
    val isSpeaking: Boolean = false,
    val isSpeechAvailable: Boolean = false,
    val ttsReady: Boolean = false,
    val voiceError: String? = null
)

class SathiVoiceManager(context: Context) {

    private val appContext = context.applicationContext
    private val _status = MutableStateFlow(
        VoiceStatus(isSpeechAvailable = SpeechRecognizer.isRecognitionAvailable(appContext))
    )
    val status: StateFlow<VoiceStatus> = _status.asStateFlow()

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var onFinalTranscript: ((String) -> Unit)? = null

    init {
        initTextToSpeech()
    }

    private fun initTextToSpeech() {
        try {
            tts = TextToSpeech(appContext) { statusCode ->
                if (statusCode == TextToSpeech.SUCCESS) {
                    val bdLocale = Locale("bn", "BD")
                    val inLocale = Locale("bn", "IN")
                    val resBd = tts?.setLanguage(bdLocale)
                    if (resBd == TextToSpeech.LANG_MISSING_DATA || resBd == TextToSpeech.LANG_NOT_SUPPORTED) {
                        val resIn = tts?.setLanguage(inLocale)
                        if (resIn == TextToSpeech.LANG_MISSING_DATA || resIn == TextToSpeech.LANG_NOT_SUPPORTED) {
                            Log.w(TAG, "Bengali TTS voice not installed; using default locale")
                            tts?.setLanguage(Locale.getDefault())
                        }
                    }
                    tts?.setSpeechRate(1.0f)
                    tts?.setPitch(1.06f)
                    tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _status.value = _status.value.copy(isSpeaking = true)
                        }

                        override fun onDone(utteranceId: String?) {
                            _status.value = _status.value.copy(isSpeaking = false)
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            _status.value = _status.value.copy(isSpeaking = false)
                        }

                        override fun onError(utteranceId: String?, errorCode: Int) {
                            _status.value = _status.value.copy(isSpeaking = false)
                        }
                    })
                    _status.value = _status.value.copy(ttsReady = true)
                } else {
                    Log.w(TAG, "TTS initialization failed with status: $statusCode")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize TextToSpeech", e)
        }
    }

    fun speak(text: String) {
        val clean = text.trim()
        if (clean.isEmpty() || !_status.value.ttsReady) return
        try {
            tts?.speak(clean, TextToSpeech.QUEUE_FLUSH, null, UUID.randomUUID().toString())
        } catch (e: Exception) {
            Log.e(TAG, "TTS speak failed", e)
        }
    }

    fun stopSpeaking() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "TTS stop failed", e)
        } finally {
            _status.value = _status.value.copy(isSpeaking = false)
        }
    }

    fun startListening(onResult: (String) -> Unit) {
        if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
            _status.value = _status.value.copy(
                voiceError = "এই ডিভাইসে ভয়েস রিকগনিশন সার্ভিস পাওয়া যায়নি।"
            )
            return
        }

        stopSpeaking()
        onFinalTranscript = onResult

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(appContext).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _status.value = _status.value.copy(
                            isListening = true,
                            partialTranscript = "",
                            voiceError = null
                        )
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _status.value = _status.value.copy(isListening = false)
                    }

                    override fun onError(error: Int) {
                        val msg = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "কিছু শোনা যায়নি, আবার বলুন।"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "সময় শেষ, আবার চেষ্টা করুন।"
                            SpeechRecognizer.ERROR_NETWORK,
                            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "ভয়েস চিনতে ইন্টারনেট প্রয়োজন।"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "মাইক্রোফোন অনুমতি দিন।"
                            else -> "ভয়েস বুঝতে সমস্যা হয়েছে।"
                        }
                        _status.value = _status.value.copy(
                            isListening = false,
                            partialTranscript = "",
                            voiceError = msg
                        )
                    }

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull()?.trim().orEmpty()
                        _status.value = _status.value.copy(
                            isListening = false,
                            partialTranscript = ""
                        )
                        if (text.isNotEmpty()) {
                            onFinalTranscript?.invoke(text)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val partial = matches?.firstOrNull()?.trim().orEmpty()
                        if (partial.isNotEmpty()) {
                            _status.value = _status.value.copy(partialTranscript = partial)
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "bn-BD")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start SpeechRecognizer", e)
            _status.value = _status.value.copy(
                isListening = false,
                voiceError = "মাইক্রোফোন চালু করা যায়নি।"
            )
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to stop listening", e)
        } finally {
            _status.value = _status.value.copy(isListening = false)
        }
    }

    fun clearVoiceError() {
        _status.value = _status.value.copy(voiceError = null)
    }

    fun release() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing voice resources", e)
        }
    }
}
