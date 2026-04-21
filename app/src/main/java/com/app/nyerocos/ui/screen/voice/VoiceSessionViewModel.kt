package com.app.nyerocos.ui.screen.voice

import android.app.Application
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.nyerocos.data.model.ChatMessage
import com.app.nyerocos.data.model.ConversationMode
import com.app.nyerocos.data.repository.ChatRepository
import com.app.nyerocos.data.service.GeminiAiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

data class VoiceSessionUiState(
    val mode: String = "",
    val isCallActive: Boolean = true,
    val isListening: Boolean = false,
    val isAiSpeaking: Boolean = false,
    val isProcessing: Boolean = false,
    val lastSpokenText: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val elapsedSeconds: Int = 0
)

class VoiceSessionViewModel(application: Application) :AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val _uiState = MutableStateFlow(VoiceSessionUiState())
    val uiState: StateFlow<VoiceSessionUiState> = _uiState.asStateFlow()

    private var aiService: GeminiAiService? = null
    private val tts = TextToSpeech(application, this)
    private var isTtsReady = false

    private val repository = ChatRepository(application)
    private var conversationId: String? = null

    var onAiFinishedSpeaking: (() -> Unit)? = null

    init {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                if (_uiState.value.isCallActive) {
                    _uiState.value = _uiState.value.copy(
                        elapsedSeconds = _uiState.value.elapsedSeconds + 1
                    )
                }
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.getDefault()
            isTtsReady = true

            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _uiState.value = _uiState.value.copy(isAiSpeaking = true)
                }

                override fun onDone(utteranceId: String?) {
                    _uiState.value = _uiState.value.copy(isAiSpeaking = false)
                    onAiFinishedSpeaking?.invoke()
                }
                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _uiState.value = _uiState.value.copy(isAiSpeaking = false)
                }
            })
        }
    }

    fun setMode(mode: String) {
        _uiState.value = _uiState.value.copy(mode = mode)

        val conversationMode = try {
            ConversationMode.valueOf(mode)
        } catch (e: Exception) {
            ConversationMode.STUDY
        }
        aiService = GeminiAiService(systemPrompt = conversationMode.systemPrompt)

        viewModelScope.launch {
            conversationId = repository.createConversation(mode)
        }
    }

    fun onSpeechResult(spokenText: String) {
        if (spokenText.isEmpty()) return

        _uiState.value = _uiState.value.copy(
            lastSpokenText = spokenText,
            isAiSpeaking = false,
            isProcessing = true
        )

        val userMessage = ChatMessage(content = spokenText, isFromUser = true)
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage
        )

        viewModelScope.launch {
            conversationId?.let {
                repository.saveMessage(it, spokenText, isFromUser = true)
            }

            val history = _uiState.value.messages
                .dropLast(1)
                .map { Pair(it.content, it.isFromUser) }

            val responseText =
                aiService?.sendMessageWithHistory(
                    history = history,
                    newMessage = spokenText
                ) ?: "..."
            val aiMessage = ChatMessage(content = responseText, isFromUser = false)
            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + aiMessage,
                isProcessing = false
            )
            conversationId?.let {
                repository.saveMessage(it, responseText, isFromUser = false)
            }
            speak(responseText)

        }
    }

    fun setListening(listening: Boolean) {
        _uiState.value = _uiState.value.copy(isListening = listening)
    }

    private fun speak(text: String) {
        if (isTtsReady) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "voice_response")
        }
    }

    fun endCall() {
        tts.stop()
        _uiState.value = _uiState.value.copy(isCallActive = false)
    }

    override fun onCleared() {
        tts.stop()
        tts.shutdown()
        super.onCleared()
    }

}




