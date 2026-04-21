package com.app.nyerocos.ui.screen.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.nyerocos.data.local.entity.ConversationEntity
import com.app.nyerocos.ui.theme.NyerocosBlack
import com.app.nyerocos.ui.theme.NyerocosBlue
import com.app.nyerocos.ui.theme.NyerocosRed
import com.app.nyerocos.ui.theme.NyerocosSurface
import com.app.nyerocos.ui.theme.NyerocosYellow

@Composable
fun HistoryScreen(
    onConversationClick: (String) -> Unit,
    onNavigateToHome: () -> Unit = {},
    viewModel: HistoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredConversations = if (searchQuery.isEmpty()) {
        uiState.conversations
    } else {
        uiState.conversations.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.mode.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NyerocosSurface)
            .statusBarsPadding()
    ) {
        // === HEADER ===
        Text(
            text = "CHAT HISTORY",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = NyerocosBlack,
            modifier = Modifier
                .padding(top = 16.dp)
                .align(Alignment.CenterHorizontally)
        )

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(3.dp)
                .background(NyerocosBlack)
        )

        // === CONTENT ===
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NyerocosBlack.copy(alpha = 0.05f))
                .padding(horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // === SEARCH BAR ===
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, NyerocosBlack, RoundedCornerShape(4.dp)),
                placeholder = {
                    Text(
                        text = "SEARCH CONVERSATIONS...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NyerocosBlack.copy(alpha = 0.4f),
                        letterSpacing = 1.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filter",
                        tint = NyerocosBlack
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = NyerocosSurface,
                    unfocusedContainerColor = NyerocosSurface,
                    focusedTextColor = NyerocosBlack,
                    unfocusedTextColor = NyerocosBlack,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = NyerocosBlack
                ),
                shape = RoundedCornerShape(4.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // === CONVERSATION LIST ===
            if (filteredConversations.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 48.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No conversations yet",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = NyerocosBlack.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Start a session to see history here!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = NyerocosBlack.copy(alpha = 0.3f)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredConversations.size) { index ->
                        val conversation = filteredConversations[index]
                        ConversationCard(
                            conversation = conversation,
                            onClick = { onConversationClick(conversation.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}

@Composable
private fun ConversationCard(
    conversation: ConversationEntity,
    onClick: () -> Unit
) {
    val timeAgo = getTimeAgo(conversation.lastMessageAt)

    // Warna card — semua kuning
    val cardColor = NyerocosYellow

    // Icon berdasarkan mode
    val modeIcon: ImageVector = when (conversation.mode.uppercase()) {
        "INTERVIEW" -> Icons.Outlined.RecordVoiceOver
        "STUDY" -> Icons.AutoMirrored.Outlined.MenuBook
        "CHILL" -> Icons.Outlined.Bedtime
        else -> Icons.AutoMirrored.Outlined.MenuBook
    }

    // Icon background per mode
    val iconBgColor = when (conversation.mode.uppercase()) {
        "INTERVIEW" -> Color(0xFFE63B2E)   // merah
        "STUDY" -> Color.White              // putih
        "CHILL" -> NyerocosBlue             // biru
        else -> NyerocosSurface
    }

    // Icon tint per mode
    val iconTint = when (conversation.mode.uppercase()) {
        "INTERVIEW" -> Color.White
        "STUDY" -> NyerocosBlack
        "CHILL" -> Color.White
        else -> NyerocosBlack
    }

    // Neo-brutalist card
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        // Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 5.dp, y = 5.dp)
                .background(NyerocosBlack, RoundedCornerShape(4.dp))
        )
        // Main card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardColor, RoundedCornerShape(4.dp))
                .border(3.dp, NyerocosBlack, RoundedCornerShape(4.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mode icon
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(iconBgColor, RoundedCornerShape(4.dp))
                    .border(2.dp, NyerocosBlack, RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = modeIcon,
                    contentDescription = conversation.mode,
                    tint = iconTint,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title & preview
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = conversation.title.uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = NyerocosBlack,
                    maxLines = 1,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${conversation.mode} session",
                    style = MaterialTheme.typography.bodySmall,
                    color = NyerocosBlack.copy(alpha = 0.6f),
                    maxLines = 1
                )
            }

            // Time ago badge
            Box(
                modifier = Modifier
                    .background(NyerocosBlack, RoundedCornerShape(4.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = timeAgo,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = NyerocosSurface,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

private fun getTimeAgo(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    val minutes = diff / (1000 * 60)
    val hours = diff / (1000 * 60 * 60)
    val days = diff / (1000 * 60 * 60 * 24)

    return when {
        minutes < 1 -> "NOW"
        minutes < 60 -> "${minutes}M\nAGO"
        hours < 24 -> "${hours}H\nAGO"
        days < 7 -> "${days}D\nAGO"
        else -> "${days / 7}W\nAGO"
    }
}
