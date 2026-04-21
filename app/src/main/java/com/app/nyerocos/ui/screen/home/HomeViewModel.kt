package com.app.nyerocos.ui.screen.home

import androidx.lifecycle.ViewModel
import com.app.nyerocos.data.model.ConversationMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HomeUiState(
    val selectedMode: ConversationMode = ConversationMode.INTERVIEW
)

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> =
        _uiState.asStateFlow()

    fun selectMode(mode: ConversationMode) {
        _uiState.value = _uiState.value.copy(selectedMode = mode)
    }
}