package com.app.nyerocos.ui.screen.history

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.outlined.Bedtime
import androidx.compose.material.icons.outlined.RecordVoiceOver
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.nyerocos.R
import com.app.nyerocos.data.local.entity.ConversationEntity
import com.app.nyerocos.ui.components.BottomTab
import com.app.nyerocos.ui.components.NyerocosBottomBar
import com.app.nyerocos.ui.theme.NyerocosBlack
import com.app.nyerocos.ui.theme.NyerocosBlue
import com.app.nyerocos.ui.theme.NyerocosRed
import com.app.nyerocos.ui.theme.NyerocosSurface
import com.app.nyerocos.ui.theme.NyerocosYellow
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onConversationClick: (String) -> Unit,
    onNavigateToHome: () -> Unit = {},
    viewModel: HistoryViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val msgChatDeleted = stringResource(R.string.chat_deleted)
    val msgUndo = stringResource(R.string.undo)

    val filteredConversations = if (searchQuery.isEmpty()) {
        uiState.conversations
    } else {
        uiState.conversations.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.mode.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = NyerocosSurface,
        bottomBar = {
            NyerocosBottomBar(
                selectTab = BottomTab.HISTORY,
                onTabSelected = { tab ->
                    if (tab == BottomTab.CALL) onNavigateToHome()
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = NyerocosBlack,
                    contentColor = NyerocosSurface,
                    actionColor = NyerocosYellow,
                    shape = RoundedCornerShape(4.dp)
                )
            }
        }
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(NyerocosSurface)
                .statusBarsPadding()
        ) {
            Text(
                text = "CHAT HISTORY",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = NyerocosBlack,
                modifier = Modifier
                    .padding(top = 20.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(3.dp)
                    .background(NyerocosBlack)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(NyerocosBlack.copy(alpha = 0.05f))
                    .padding(horizontal = 20.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

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
                        items(
                            count = filteredConversations.size,
                            key = { filteredConversations[it].id }
                        ) { index ->
                            val conversation = filteredConversations[index]
                            SwipeToDeleteContainer(
                                conversation = conversation,
                                onDelete = { deletedConversation ->
                                    viewModel.deleteConversation(deletedConversation.id)
                                    coroutineScope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = msgChatDeleted,
                                            actionLabel = msgUndo,
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.restoreConversation(deletedConversation)
                                        }
                                    }
                                },
                                onClick = { onConversationClick(conversation.id) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteContainer(
    conversation: ConversationEntity,
    onDelete: (ConversationEntity) -> Unit,
    onClick: () -> Unit
) {
    var isRemoved by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                isRemoved = true
                true
            } else {
                false
            }
        },
    )

    LaunchedEffect(isRemoved) {
        if (isRemoved) {
            onDelete(conversation)
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val backgroundColor by animateColorAsState(
                targetValue = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.EndToStart -> NyerocosRed
                    else -> Color.Transparent
                },
                animationSpec = tween(200),
                label = "delete_bg_color"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor, RoundedCornerShape(4.dp))
                    .border(
                        width = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) 3.dp else 0.dp,
                        color = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) NyerocosBlack else Color.Transparent,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteForever,
                        contentDescription = stringResource(R.string.delete_desc),
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = stringResource(R.string.delete_label),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 1.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    ) {
        ConversationCard(
            conversation = conversation,
            onClick = onClick
        )
    }
}

@Composable
private fun ConversationCard(
    conversation: ConversationEntity,
    onClick: () -> Unit
) {
    val timeAgo = getTimeAgo(conversation.lastMessageAt)

    val cardColor = NyerocosYellow

    val modeIcon: ImageVector = when (conversation.mode.uppercase()) {
        "INTERVIEW" -> Icons.Outlined.RecordVoiceOver
        "STUDY" -> Icons.AutoMirrored.Outlined.MenuBook
        "CHILL" -> Icons.Outlined.Bedtime
        else -> Icons.AutoMirrored.Outlined.MenuBook
    }

    val iconBgColor = when (conversation.mode.uppercase()) {
        "INTERVIEW" -> Color(0xFFE63B2E)
        "STUDY" -> Color.White
        "CHILL" -> NyerocosBlue
        else -> NyerocosSurface
    }

    // Icon tint per mode
    val iconTint = when (conversation.mode.uppercase()) {
        "INTERVIEW" -> Color.White
        "STUDY" -> NyerocosBlack
        "CHILL" -> Color.White
        else -> NyerocosBlack
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 5.dp, y = 5.dp)
                .background(NyerocosBlack, RoundedCornerShape(4.dp))
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardColor, RoundedCornerShape(4.dp))
                .border(3.dp, NyerocosBlack, RoundedCornerShape(4.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

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
