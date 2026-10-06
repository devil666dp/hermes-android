package org.hermes.android.ui.chat.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hermes.android.ui.theme.AssistantUiTokens

data class StarterPrompt(
    val title: String,
    val description: String,
    val prompt: String,
    val icon: ImageVector
)

val STARTER_PROMPTS = listOf(
    StarterPrompt(
        title = "Check Zerops status",
        description = "Query active services and resource metrics",
        prompt = "Check current Zerops platform status and services health.",
        icon = Icons.Default.Cloud
    ),
    StarterPrompt(
        title = "Inspect workspace files",
        description = "List and review active project directories",
        prompt = "List all active workspace files and repository structures.",
        icon = Icons.Default.Folder
    ),
    StarterPrompt(
        title = "Run system diagnostics",
        description = "Verify agent environment and tools",
        prompt = "Run diagnostic checks on agent tools, skills, and environment.",
        icon = Icons.Default.Build
    ),
    StarterPrompt(
        title = "Review agent skills",
        description = "Show installed skills and capabilities",
        prompt = "What skills and automations are currently configured for Hermes?",
        icon = Icons.Default.Code
    )
)

/**
 * Port of assistant-ui/elements/empty-state
 * Welcome state with logo, greeting, and actionable suggestion cards.
 */
@Composable
fun ChatEmptyState(
    onSelectPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing avatar badge
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            border = AssistantUiTokens.paperBorder(),
            tonalElevation = 4.dp,
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Hermes Logo",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "How can Hermes help you?",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Ask a question, trigger slash commands (/), or choose a quick starter below.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Starter prompt cards
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            STARTER_PROMPTS.forEach { item ->
                Surface(
                    shape = AssistantUiTokens.FieldShape,
                    color = AssistantUiTokens.paperColor(),
                    border = AssistantUiTokens.paperBorder(),
                    tonalElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(AssistantUiTokens.FieldShape)
                        .clickable { onSelectPrompt(item.prompt) }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
