package com.app.nyerocos.ui.screen.voice

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.nyerocos.data.model.ConversationMode
import com.app.nyerocos.data.repository.ChatRepository
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.AudioTranscriptionConfig
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.PublicPreviewAPI
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.liveGenerationConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Voice session states — kept minimal for clean Live API integration.
 */
data class VoiceSessionUiState(
    val mode: String = "",
    val isCallActive: Boolean = true,
    val isConnecting: Boolean = false,
    val isConversing: Boolean = false,
    val isMuted: Boolean = false,
    val lastUserText: String = "",
    val lastAiText: String = "",
    val elapsedSeconds: Int = 0,
    val error: String? = null
)

/**
 * ViewModel for Gemini Live API voice sessions.
 *
 * Uses Firebase AI LiveModel for real-time bidirectional audio streaming.
 * No SpeechRecognizer, no TTS — Gemini handles everything natively.
 */
@OptIn(PublicPreviewAPI::class)
class VoiceSessionViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(VoiceSessionUiState())
    val uiState: StateFlow<VoiceSessionUiState> = _uiState.asStateFlow()

    private var liveSession: Any? = null
    private var timerJob: Job? = null
    private val repository = ChatRepository(application)
    private var conversationId: String? = null

    // Transcript buffering — accumulate fragments, save on turn change
    private val userBuffer = StringBuilder()
    private val aiBuffer = StringBuilder()

    private enum class Speaker { NONE, USER, AI }
    private var lastSpeaker = Speaker.NONE

    companion object {
        private const val TAG = "VoiceSession"
        private const val LIVE_MODEL_NAME = "gemini-2.5-flash-native-audio-preview-12-2025"
    }

    private var connectJob: Job? = null
    private var isSessionReady = false
    private var pendingStartAudio = false

    /**
     * Phase 1: Start connecting immediately (no permission needed).
     * Called as soon as the screen opens, even before permission dialog.
     */
    fun prepareSession(mode: String) {
        if (_uiState.value.isConversing || _uiState.value.isConnecting) return

        _uiState.value = _uiState.value.copy(
            mode = mode,
            isConnecting = true,
            error = null
        )

        val conversationMode = try {
            ConversationMode.valueOf(mode)
        } catch (e: Exception) {
            ConversationMode.STUDY
        }

        connectJob = viewModelScope.launch {
            try {
                conversationId = repository.createConversation(mode)

                val liveModel = Firebase.ai(backend = GenerativeBackend.googleAI())
                    .liveModel(
                        modelName = LIVE_MODEL_NAME,
                        generationConfig = liveGenerationConfig {
                            responseModality = ResponseModality.AUDIO
                            inputAudioTranscription = AudioTranscriptionConfig()
                            outputAudioTranscription = AudioTranscriptionConfig()
                        },
                        systemInstruction = content { text(conversationMode.systemPrompt) }
                    )

                val session = liveModel.connect()
                liveSession = session
                isSessionReady = true
                Log.d(TAG, "Session connected — ready for audio")

                // If permission was already granted while we were connecting
                if (pendingStartAudio) {
                    beginAudioConversation()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to connect", e)
                _uiState.value = _uiState.value.copy(
                    isConnecting = false,
                    error = e.message
                )
            }
        }
    }

    /**
     * Phase 2: Start audio after permission is granted.
     * If session is already connected, starts immediately.
     * If still connecting, queues to start when ready.
     */
    fun startAudio() {
        if (isSessionReady) {
            viewModelScope.launch { beginAudioConversation() }
        } else {
            pendingStartAudio = true
        }
    }

    private suspend fun beginAudioConversation() {
        try {
            val session = liveSession ?: return

            (session as com.google.firebase.ai.type.LiveSession).startAudioConversation(
                transcriptHandler = { inputTranscription, outputTranscription ->
                    handleTranscription(inputTranscription?.text, outputTranscription?.text)
                }
            )

            _uiState.value = _uiState.value.copy(
                isConnecting = false,
                isConversing = true
            )
            startTimer()
            pendingStartAudio = false
            Log.d(TAG, "Audio conversation started")
        } catch (e: SecurityException) {
            Log.e(TAG, "Microphone permission not granted", e)
            _uiState.value = _uiState.value.copy(
                isConnecting = false,
                error = "Microphone permission required"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio", e)
            _uiState.value = _uiState.value.copy(
                isConnecting = false,
                error = e.message
            )
        }
    }

    /**
     * Accumulate transcript fragments. Save to DB only when speaker changes.
     * e.g. User: "Can" "you" "say" → saved as one bubble "Can you say"
     */
    private fun handleTranscription(userText: String?, aiText: String?) {
        userText?.takeIf { it.isNotBlank() }?.let { text ->
            // Speaker changed from AI → User: flush AI buffer first
            if (lastSpeaker == Speaker.AI) {
                flushAiBuffer()
            }
            lastSpeaker = Speaker.USER
            userBuffer.append(text).append(" ")
            _uiState.value = _uiState.value.copy(lastUserText = userBuffer.toString().trim())
        }

        aiText?.takeIf { it.isNotBlank() }?.let { text ->
            // Speaker changed from User → AI: flush User buffer first
            if (lastSpeaker == Speaker.USER) {
                flushUserBuffer()
            }
            lastSpeaker = Speaker.AI
            aiBuffer.append(text).append(" ")
            _uiState.value = _uiState.value.copy(lastAiText = aiBuffer.toString().trim())
        }
    }

    private fun flushUserBuffer() {
        val text = userBuffer.toString().trim()
        if (text.isNotEmpty()) {
            viewModelScope.launch {
                conversationId?.let { id -> repository.saveMessage(id, text, isFromUser = true) }
            }
            userBuffer.clear()
        }
    }

    private fun flushAiBuffer() {
        val text = aiBuffer.toString().trim()
        if (text.isNotEmpty()) {
            viewModelScope.launch {
                conversationId?.let { id -> repository.saveMessage(id, text, isFromUser = false) }
            }
            aiBuffer.clear()
        }
    }

    fun toggleMute() {
        val newMuted = !_uiState.value.isMuted
        _uiState.value = _uiState.value.copy(isMuted = newMuted)
        Log.d(TAG, if (newMuted) "Muted" else "Unmuted")
    }

    fun endCall() {
        // Flush any remaining buffered text before closing
        flushUserBuffer()
        flushAiBuffer()

        viewModelScope.launch {
            try {
                (liveSession as? AutoCloseable)?.close()
            } catch (e: Exception) {
                Log.e(TAG, "Error closing session", e)
            }
            liveSession = null
            timerJob?.cancel()
            _uiState.value = _uiState.value.copy(
                isCallActive = false,
                isConversing = false
            )
            Log.d(TAG, "Session ended")
        }
    }

    private fun startTimer() {
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_uiState.value.isCallActive) {
                    _uiState.value = _uiState.value.copy(
                        elapsedSeconds = _uiState.value.elapsedSeconds + 1
                    )
                }
            }
        }
    }

    override fun onCleared() {
        flushUserBuffer()
        flushAiBuffer()
        viewModelScope.launch {
            try {
                (liveSession as? AutoCloseable)?.close()
            } catch (_: Exception) {}
        }
        timerJob?.cancel()
        super.onCleared()
    }
}
