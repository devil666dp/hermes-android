package org.hermes.android.ui.chat.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hermes.android.ui.theme.AssistantUiTokens

/**
 * Port of assistant-ui/elements/scroll-anchor
 * Floating pill indicating that the conversation has new incoming content while user is scrolled up.
 */
@Composable
fun ScrollAnchor(
    visible: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Scroll to latest"
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut() + slideOutVertically { it / 2 },
        modifier = modifier
    ) {
        Surface(
            shape = AssistantUiTokens.PillShape,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            border = AssistantUiTokens.fieldBorder(),
            tonalElevation = 6.dp,
            shadowElevation = 4.dp,
            modifier = Modifier
                .clip(AssistantUiTokens.PillShape)
                .clickable { onClick() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Scroll to bottom",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
