package com.app.nyerocos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.app.nyerocos.ui.theme.NyerocosBlack

@Composable
fun NeoBrutalistCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    borderWidth: Dp = 2.dp,
    shadowOffset: Dp = 4.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = shadowOffset, y = shadowOffset)
                .background(NyerocosBlack)
        )
        Box(
            modifier = Modifier
                .background(backgroundColor)
                .border(borderWidth, NyerocosBlack)
                .then(
                    if(onClick != null) Modifier.clickable{onClick()}
                    else Modifier
                )
                .padding(16.dp),
            content = content
        )
    }
}