package org.hermes.android.ui.chat.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hermes.android.data.model.UsageState
import org.hermes.android.ui.theme.AssistantUiTokens

/**
 * Port of assistant-ui/elements/context-display and cost-meter
 * Displays token usage, run cost, and context window utilization in a sleek rail.
 */
@Composable
fun UsageContextBar(usage: UsageState) {
    if (usage.totalTokens == 0L && usage.cost == null) return

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f),
        border = AssistantUiTokens.fieldBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "${usage.totalTokens} tokens",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (usage.cost != null) {
                Text(
                    text = String.format("$%.4f", usage.cost),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (usage.contextTokens != null && usage.contextWindowTokens != null && usage.contextWindowTokens > 0) {
                val pct = (usage.contextTokens.toDouble() / usage.contextWindowTokens * 100).toInt()
                Surface(
                    shape = AssistantUiTokens.PillShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    modifier = Modifier.padding(2.dp)
                ) {
                    Text(
                        text = "Window: $pct%",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
