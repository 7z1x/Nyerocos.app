package com.app.nyerocos.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.app.nyerocos.R
import com.app.nyerocos.ui.theme.NyerocosBlack
import com.app.nyerocos.ui.theme.NyerocosYellow

enum class BottomTab(
    @StringRes val labelRes: Int,
    val icon: ImageVector
) {
    CALL(R.string.nav_call, Icons.Filled.Call),
    HISTORY(R.string.nav_history, Icons.Filled.History)
}

@Composable
fun NyerocosBottomBar(
    selectTab: BottomTab,
    onTabSelected: (BottomTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .border(
                width = 2.dp,
                color = NyerocosBlack,
            )
            .padding(horizontal = 32.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomTab.entries.forEach { tab ->
            BottomTabItem(
                tab = tab,
                isSelected = tab == selectTab,
                onClick = { onTabSelected(tab) }
            )
        }
    }
}

@Composable
private fun BottomTabItem(
    tab: BottomTab,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .then(
                if (isSelected) {
                    Modifier
                        .background(NyerocosYellow, shape)
                        .border(3.dp, NyerocosBlack, shape)
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                } else {
                    Modifier
                        .clickable { onClick() }
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                }
            )
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = stringResource(tab.labelRes),
            tint = NyerocosBlack
        )
        Text(
            text = stringResource(tab.labelRes),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = NyerocosBlack
        )
    }
}