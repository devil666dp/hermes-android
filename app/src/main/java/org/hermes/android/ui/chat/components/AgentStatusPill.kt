package org.hermes.android.ui.chat.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hermes.android.ui.theme.AssistantUiTokens

enum class AgentState {
    WORKING,
    WAITING,
    DONE,
    FAILED
}

/**
 * Port of assistant-ui/elements/agent-status
 * Displays current agent status pill with state dot, label, and elapsed timer.
 */
@Composable
fun AgentStatusPill(
    state: AgentState,
    label: String,
    elapsed: String? = null,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotPulse"
    )

    Surface(
        shape = AssistantUiTokens.PillShape,
        color = AssistantUiTokens.paperColor(),
        border = AssistantUiTokens.fieldBorder(),
        tonalElevation = 1.dp,
        modifier = modifier
            .clip(AssistantUiTokens.PillShape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            when (state) {
                AgentState.WORKING -> {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .alpha(pulseAlpha)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    )
                }
                AgentState.WAITING -> {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = "Waiting",
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(11.dp)
                    )
                }
                AgentState.DONE -> {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Done",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(11.dp)
                    )
                }
                AgentState.FAILED -> {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Failed",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurface
            )

            if (!elapsed.isNullOrBlank() && (state == AgentState.WORKING || state == AgentState.WAITING)) {
                Text(
                    text = elapsed,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
