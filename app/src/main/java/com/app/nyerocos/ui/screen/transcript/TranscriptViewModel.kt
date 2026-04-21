package com.app.nyerocos.ui.screen.transcript

import android.app.Application
import android.speech.tts.TextToSpeech
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.nyerocos.data.model.ChatMessage
import com.app.nyerocos.data.repository.ChatRepository
import com.app.nyerocos.data.service.GeminiAiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

data class TranscriptUiState(
    val conversationId: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false
)

class TranscriptViewModel(application: Application) : AndroidViewModel(application),
    TextToSpeech.OnInitListener {

    private val _uiState = MutableStateFlow(TranscriptUiState())
    val uiState: StateFlow<TranscriptUiState> = _uiState.asStateFlow()

    private val aiService = GeminiAiService()
    private val repository = ChatRepository(application)

    private val tts = TextToSpeech(application, this)
    private var isTtsReady = false

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.getDefault()
            isTtsReady = true
        }
    }

    fun setConversationId(id: String) {
        _uiState.value = _uiState.value.copy(conversationId = id)

        viewModelScope.launch {
            repository.getMessagesForConversation(id).collect { messages ->
                _uiState.value = _uiState.value.copy(messages = messages)
            }
        }
    }

    fun onInputChanged(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty()) return

        val conversationId = _uiState.value.conversationId
        _uiState.value = _uiState.value.copy(
            inputText = "",
            isLoading = true
        )

        viewModelScope.launch {
            repository.saveMessage(conversationId, text, isFromUser = true)

            val history = _uiState.value.messages
                .map { Pair(it.content, it.isFromUser) }

            val responseText = aiService.sendMessageWithHistory(
                history = history,
                newMessage = text
            )

            repository.saveMessage(conversationId, responseText, isFromUser = false)

            _uiState.value = _uiState.value.copy(isLoading = false)

            speak(responseText)
        }
    }

    private fun speak(text: String) {
        if (isTtsReady) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "nyerocos_response")
        }
    }

    override fun onCleared() {
        tts.stop()
        tts.shutdown()
        super.onCleared()
    }
}
