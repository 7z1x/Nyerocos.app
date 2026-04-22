package com.app.nyerocos.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.app.nyerocos.ui.theme.NyerocosBlack
import com.app.nyerocos.ui.theme.NyerocosBlue
import com.app.nyerocos.ui.theme.NyerocosRed
import com.app.nyerocos.ui.theme.NyerocosYellow
import kotlin.random.Random

@Composable
fun WaveformVisualizer(
    isActive: Boolean,
    isAiSpeaking: Boolean,
    rmsLevel: Float = 0f
) {
    val barColors = listOf(
        NyerocosRed, NyerocosBlack, NyerocosBlue, NyerocosRed,
        NyerocosYellow, NyerocosBlack, NyerocosYellow, NyerocosBlack,
        NyerocosBlue, NyerocosRed, NyerocosYellow, NyerocosBlack,
        NyerocosRed, NyerocosBlack
    )
    val barCount = barColors.size

    // Setiap bar punya target height sendiri yang berubah independen
    val barTargets = remember { List(barCount) { mutableFloatStateOf(0.05f) } }

    // Coroutine update tiap bar secara random setiap 60ms
    LaunchedEffect(isActive, isAiSpeaking) {
        while (true) {
            if (!isActive) {
                barTargets.forEach { it.floatValue = 0.05f }
            } else {
                val baseLevel = if (isAiSpeaking) {
                    // AI bicara: random mid-high
                    0.3f + Random.nextFloat() * 0.55f
                } else {
                    // User bicara: scale dari RMS
                    (rmsLevel / 10f).coerceIn(0.05f, 1f)
                }
                barTargets.forEach { target ->
                    val randomFactor = Random.nextFloat() * 0.7f + 0.3f
                    val noise = (Random.nextFloat() - 0.5f) * 0.25f
                    target.floatValue = (baseLevel * randomFactor + noise).coerceIn(0.04f, 0.97f)
                }
            }
            kotlinx.coroutines.delay(60L)
        }
    }

    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.Bottom
    ) {
        barColors.forEachIndexed { index, color ->
            val animatedHeight by animateFloatAsState(
                targetValue = barTargets[index].floatValue,
                animationSpec = tween(durationMillis = 100),
                label = "bar_$index"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp)
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .fillMaxSize(fraction = animatedHeight.coerceIn(0.03f, 1f))
                        .background(color)
                        .border(1.dp, NyerocosBlack)
                )
            }
        }
    }
}
