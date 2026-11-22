package com.example.viewmodel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SathiAiRepository
import com.example.data.SathiPreferences
import com.example.data.SathiPreferencesRepository
import com.example.device.DeviceActionExecutor
import com.example.model.ChatMessage
import com.example.model.DeviceAction
import com.example.model.DeviceActionType
import com.example.model.InstalledAppInfo
import com.example.model.ScriptTone
import com.example.model.VideoFormat
import com.example.model.YouTubeScript
import com.example.voice.SathiVoiceManager
import com.example.voice.VoiceStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

private const val TAG = "SathiViewModel"

sealed interface StudioUiState {
    data object Idle : StudioUiState
    data object Loading : StudioUiState
    data class Success(val script: YouTubeScript) : StudioUiState
    data class Error(val message: String) : StudioUiState
}

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isGenerating: Boolean = false,
    val errorBanner: String? = null,
    val pendingContactMessageId: String? = null
)

class SathiViewModel(application: Application) : AndroidViewModel(application) {

    private val aiRepository = SathiAiRepository()
    private val prefsRepository = SathiPreferencesRepository(application)
    private val actionExecutor = DeviceActionExecutor()
    private val voiceManager = SathiVoiceManager(application)

    val preferences: StateFlow<SathiPreferences> = prefsRepository.preferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SathiPreferences()
    )

    val voiceStatus: StateFlow<VoiceStatus> = voiceManager.status

    private val _chatState = MutableStateFlow(ChatUiState())
    val chatState: StateFlow<ChatUiState> = _chatState.asStateFlow()

    private val _studioState = MutableStateFlow<StudioUiState>(StudioUiState.Idle)
    val studioState: StateFlow<StudioUiState> = _studioState.asStateFlow()

    private val _installedApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    val installedApps: StateFlow<List<InstalledAppInfo>> = _installedApps.asStateFlow()

    init {
        refreshInstalledApps()
    }

    fun refreshInstalledApps() {
        viewModelScope.launch {
            val apps = withContext(Dispatchers.IO) {
                actionExecutor.getInstalledApps(getApplication())
            }
            _installedApps.value = apps
        }
    }

    fun sendMessage(rawText: String) {
        val text = rawText.trim()
        if (text.isEmpty() || _chatState.value.isGenerating) return

        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            isFromUser = true,
            text = text
        )
        _chatState.update {
            it.copy(
                messages = it.messages + userMsg,
                isGenerating = true,
                errorBanner = null
            )
        }

        viewModelScope.launch {
            try {
                val result = aiRepository.sendMessage(text)
                val assistantId = UUID.randomUUID().toString()
                val action = result.deviceAction

                val baseMessage = ChatMessage(
                    id = assistantId,
                    isFromUser = false,
                    text = result.replyText,
                    deviceAction = action,
                    youtubeScript = result.youtubeScript
                )

                _chatState.update {
                    it.copy(
                        messages = it.messages + baseMessage,
                        isGenerating = false
                    )
                }

                // Speak reply if auto-speak is enabled
                if (preferences.value.autoSpeakEnabled) {
                    val spoken = action?.spokenReply?.takeIf { s -> s.isNotBlank() } ?: result.replyText
                    voiceManager.speak(spoken)
                }

                // Execute device action automatically if applicable and enabled
                if (action != null &&
                    action.action != DeviceActionType.NONE &&
                    preferences.value.autoExecuteActions
                ) {
                    // Brief pause so user sees the reply / hears TTS start cleanly
                    delay(650L)
                    executeDeviceAction(assistantId, action)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "sendMessage failed", e)
                val errorText = e.localizedMessage ?: "দুঃখিত, এই মুহূর্তে উত্তর দেওয়া সম্ভব হয়নি।"
                val errorMsg = ChatMessage(
                    id = UUID.randomUUID().toString(),
                    isFromUser = false,
                    text = errorText,
                    isError = true
                )
                _chatState.update {
                    it.copy(
                        messages = it.messages + errorMsg,
                        isGenerating = false,
                        errorBanner = errorText
                    )
                }
            }
        }
    }

    fun executeDeviceAction(messageId: String, action: DeviceAction) {
        val result = actionExecutor.execute(getApplication(), action)
        _chatState.update { state ->
            state.copy(
                messages = state.messages.map { msg ->
                    if (msg.id == messageId) {
                        msg.copy(
                            actionAttempted = true,
                            actionSucceeded = result.success,
                            actionStatusMessage = result.messageBn,
                            needsContactPicker = result.needsContactPicker
                        )
                    } else {
                        msg
                    }
                },
                pendingContactMessageId = if (result.needsContactPicker) messageId else state.pendingContactMessageId
            )
        }
    }

    fun runQuickAction(action: DeviceAction): String {
        val result = actionExecutor.execute(getApplication(), action)
        return result.messageBn
    }

    fun markContactPickerTarget(messageId: String) {
        _chatState.update { it.copy(pendingContactMessageId = messageId) }
    }

    fun handlePickedContact(contactUri: Uri) {
        val result = actionExecutor.resolvePickedContactAndDial(getApplication(), contactUri)
        val targetId = _chatState.value.pendingContactMessageId
        if (targetId != null) {
            _chatState.update { state ->
                state.copy(
                    messages = state.messages.map { msg ->
                        if (msg.id == targetId) {
                            msg.copy(
                                actionAttempted = true,
                                actionSucceeded = result.success,
                                actionStatusMessage = result.messageBn,
                                needsContactPicker = !result.success
                            )
                        } else {
                            msg
                        }
                    },
                    pendingContactMessageId = null
                )
            }
        }
    }

    fun generateStudioScript(topic: String, tone: ScriptTone, format: VideoFormat) {
        val cleanTopic = topic.trim()
        if (cleanTopic.isEmpty()) {
            _studioState.value = StudioUiState.Error("অনুগ্রহ করে ভিডিওর টপিক বা নিশ লিখুন।")
            return
        }

        _studioState.value = StudioUiState.Loading
        viewModelScope.launch {
            try {
                val script = aiRepository.generateYouTubeScript(cleanTopic, tone, format)
                _studioState.value = StudioUiState.Success(script)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "generateStudioScript failed", e)
                _studioState.value = StudioUiState.Error(
                    e.localizedMessage ?: "স্ক্রিপ্ট তৈরি করা যায়নি। আবার চেষ্টা করুন।"
                )
            }
        }
    }

    fun startVoiceInput() {
        voiceManager.startListening { transcript ->
            sendMessage(transcript)
        }
    }

    fun stopVoiceInput() {
        voiceManager.stopListening()
    }

    fun clearVoiceError() {
        voiceManager.clearVoiceError()
    }

    fun speakText(text: String) {
        voiceManager.speak(text)
    }

    fun stopSpeaking() {
        voiceManager.stopSpeaking()
    }

    fun setAutoSpeak(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepository.setAutoSpeak(enabled)
            if (!enabled) voiceManager.stopSpeaking()
        }
    }

    fun setAutoExecuteActions(enabled: Boolean) {
        viewModelScope.launch {
            prefsRepository.setAutoExecute(enabled)
        }
    }

    fun setPreferredTone(tone: ScriptTone) {
        viewModelScope.launch {
            prefsRepository.setPreferredTone(tone)
        }
    }

    fun clearChat() {
        voiceManager.stopSpeaking()
        aiRepository.resetChat()
        _chatState.value = ChatUiState()
    }

    fun dismissError() {
        _chatState.update { it.copy(errorBanner = null) }
    }

    override fun onCleared() {
        voiceManager.release()
        super.onCleared()
    }
}
