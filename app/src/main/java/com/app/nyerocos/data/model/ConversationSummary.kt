package com.app.nyerocos.data.model

import androidx.compose.ui.graphics.vector.ImageVector

data class ConversationSummary(
    val id: String,
    val title: String,
    val preview: String,
    val timeAgo: String,
    val icon: ImageVector

)