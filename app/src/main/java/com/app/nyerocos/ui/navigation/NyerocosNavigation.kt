package com.app.nyerocos.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

@Serializable
data class VoiceSessionRoute(
    val mode: String
)

@Serializable
object HistoryRoute

@Serializable
data class TranscriptRoute(
    val conversationId: String
)