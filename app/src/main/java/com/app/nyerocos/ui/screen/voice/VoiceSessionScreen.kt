package com.app.nyerocos.ui.screen.voice

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.nyerocos.R
import com.app.nyerocos.ui.components.WaveformVisualizer
import com.app.nyerocos.ui.theme.NyerocosBlack
import com.app.nyerocos.ui.theme.NyerocosRed
import com.app.nyerocos.ui.theme.NyerocosSurface
import com.app.nyerocos.ui.theme.NyerocosYellow
import java.util.Locale

@Composable
fun VoiceSessionScreen(
    mode: String,
    onEndCall: () -> Unit,
    viewModel: VoiceSessionViewModel = viewModel()
) {
    LaunchedEffect(mode) {
        viewModel.setMode(mode)
    }

    LaunchedEffect(Unit) {
        viewModel.initSpeechRecognizer()
    }

    val uiState by viewModel.uiState.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startListening()
        }
    }

    val minutes = uiState.elapsedSeconds / 60
    val seconds = uiState.elapsedSeconds % 60
    val timeFormatted = String.format(Locale.US, "%02d:%02d", minutes, seconds)

    val statusText = when {
        uiState.isAiSpeaking -> "AI SPEAKING..."
        uiState.isProcessing -> "THINKING..."
        uiState.isListening -> "LISTENING..."
        else -> "TAP TO START"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NyerocosSurface)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.nyerocos_text),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = NyerocosBlack,
            modifier = Modifier.padding(top = 16.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .height(3.dp)
                .background(NyerocosBlack)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .background(NyerocosBlack, RoundedCornerShape(4.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Text(
                text = "LIVE SESSION",
                style = MaterialTheme.typography.labelMedium,
                color = NyerocosSurface,
                letterSpacing = 2.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = mode.uppercase(),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = NyerocosBlack,
            textAlign = TextAlign.Center
        )

        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall,
            color = NyerocosBlack.copy(alpha = 0.5f),
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "ACTIVE DURATION: $timeFormatted",
            style = MaterialTheme.typography.labelSmall,
            color = NyerocosBlack.copy(alpha = 0.5f),
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 6.dp, y = 6.dp)
                    .background(NyerocosBlack, RoundedCornerShape(4.dp))
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .offset(x = 3.dp, y = 3.dp)
                    .background(NyerocosYellow, RoundedCornerShape(4.dp))
                    .border(3.dp, NyerocosBlack, RoundedCornerShape(4.dp))
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(NyerocosSurface, RoundedCornerShape(4.dp))
                    .border(3.dp, NyerocosBlack, RoundedCornerShape(4.dp))
                    .clip(RoundedCornerShape(4.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    modifier = Modifier.matchParentSize()
                ) {}

                WaveformVisualizer(
                    isActive = uiState.isAiSpeaking || uiState.isListening,
                    isAiSpeaking = uiState.isAiSpeaking,
                    rmsLevel = uiState.rmsLevel
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (uiState.lastSpokenText.isNotEmpty()) {
            Text(
                text = "\"${uiState.lastSpokenText}\"",
                style = MaterialTheme.typography.bodySmall,
                color = NyerocosBlack.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ControlButton(
                icon = Icons.Default.MicOff,
                contentDescription = "Mute",
                onClick = { /* TODO: toggle mute */ }
            )

            IconButton(
                onClick = {
                    viewModel.endCall()
                    onEndCall()
                },
                modifier = Modifier
                    .width(180.dp)
                    .height(72.dp)
                    .background(NyerocosRed, RoundedCornerShape(8.dp))
                    .border(3.dp, NyerocosBlack, RoundedCornerShape(8.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.CallEnd,
                        contentDescription = stringResource(R.string.voice_end_call),
                        tint = NyerocosSurface,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "END\nCALL",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Black,
                        color = NyerocosSurface,
                        lineHeight = 16.sp
                    )
                }
            }

            ControlButton(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = "Speaker",
                onClick = { /* TODO: toggle speaker */ }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(56.dp)
            .background(NyerocosSurface, RoundedCornerShape(8.dp))
            .border(3.dp, NyerocosBlack, RoundedCornerShape(8.dp))
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = NyerocosBlack,
            modifier = Modifier.size(24.dp)
        )
    }
}


