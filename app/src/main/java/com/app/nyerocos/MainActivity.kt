package com.app.nyerocos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.app.nyerocos.ui.navigation.HistoryRoute
import com.app.nyerocos.ui.navigation.HomeRoute
import com.app.nyerocos.ui.navigation.TranscriptRoute
import com.app.nyerocos.ui.navigation.VoiceSessionRoute
import com.app.nyerocos.ui.screen.history.HistoryScreen
import com.app.nyerocos.ui.screen.home.HomeScreen
import com.app.nyerocos.ui.screen.transcript.TranscriptScreen
import com.app.nyerocos.ui.screen.voice.VoiceSessionScreen
import com.app.nyerocos.ui.theme.NyerocosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NyerocosTheme {
                NyerocosApp()
            }
        }
    }
}

@Composable
fun NyerocosApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = HomeRoute
    ) {
        composable<HomeRoute> {
            HomeScreen(
                onStartSession = { mode ->
                    navController.navigate(VoiceSessionRoute(mode = mode.name))
                },
                onNavigationHistory = {
                    navController.navigate(HistoryRoute)
                }
            )
        }
        composable<VoiceSessionRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<VoiceSessionRoute>()
            VoiceSessionScreen(
                mode = route.mode,
                onEndCall = {
                    navController.popBackStack()
                }
            )
        }
        composable<HistoryRoute> {
            HistoryScreen(
                onConversationClick = { conversationId ->
                    navController.navigate(
                        TranscriptRoute(conversationId = conversationId))
                },
                onNavigateToHome = {
                    navController.popBackStack()
                }
            )
        }
        composable<TranscriptRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<TranscriptRoute>()
            TranscriptScreen(
                conversationId = route.conversationId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
