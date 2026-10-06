package org.hermes.android.ui.chat.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.hermes.android.data.model.UsageState

@Composable
fun UsageContextBar(usage: UsageState) {
    if (usage.totalTokens == 0L && usage.cost == null) return

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "⚡ ${usage.totalTokens} tokens",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (usage.cost != null) {
                Text(
                    text = String.format("$%.4f", usage.cost),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (usage.contextTokens != null && usage.contextWindowTokens != null && usage.contextWindowTokens > 0) {
                val pct = (usage.contextTokens.toDouble() / usage.contextWindowTokens * 100).toInt()
                Text(
                    text = "Context: $pct%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
