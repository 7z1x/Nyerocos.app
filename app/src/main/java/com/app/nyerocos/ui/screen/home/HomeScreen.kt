package com.app.nyerocos.ui.screen.home

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.nyerocos.R
import com.app.nyerocos.data.model.ConversationMode
import com.app.nyerocos.ui.components.BottomTab
import com.app.nyerocos.ui.components.ModeButton
import com.app.nyerocos.ui.components.NyerocosBottomBar
import com.app.nyerocos.ui.theme.NyerocosBlack

@Composable
fun HomeScreen(
    onStartSession: (ConversationMode) -> Unit,
    onNavigationHistory: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold (
        bottomBar = {
            NyerocosBottomBar(
                selectTab = BottomTab.CALL,
                onTabSelected = { tab ->
                    if (tab == BottomTab.HISTORY) onNavigationHistory()
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.app_name).uppercase(),
                style = MaterialTheme.typography.titleLarge,
                color = NyerocosBlack
            )

            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(160.dp)
                    .border(3.dp, NyerocosBlack),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Mic,
                    contentDescription = stringResource(R.string.microphone_icon),
                    modifier = Modifier.size(80.dp),
                    tint = NyerocosBlack
                )
            }
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(R.string.home_headline).uppercase(),
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                color = NyerocosBlack
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.home_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
             Spacer(modifier = Modifier.height(40.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ConversationMode.entries.forEach { mode ->
                    ModeButton(
                        text = stringResource(mode.displayNameRes),
                        icon = mode.icon,
                        onClick = { onStartSession(mode)}
                    )
                }
            }
        }
    }
}