package org.hermes.android.ui.chat.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.hermes.android.ui.theme.AssistantUiTokens

data class ModelOption(
    val id: String,
    val name: String,
    val provider: String,
    val description: String
)

val HERMES_SUPPORTED_MODELS = listOf(
    ModelOption(
        id = "nous-hermes-3-8b",
        name = "Nous Hermes 3 (8B)",
        provider = "Nous Research / Zerops",
        description = "Optimized for fast tool execution and function calling"
    ),
    ModelOption(
        id = "nous-hermes-3-70b",
        name = "Nous Hermes 3 (70B)",
        provider = "Nous Research / Zerops",
        description = "Advanced reasoning, multi-step orchestration and coding"
    ),
    ModelOption(
        id = "claude-3-5-sonnet",
        name = "Claude 3.5 Sonnet",
        provider = "Anthropic",
        description = "State-of-the-art coding and agent workflows"
    ),
    ModelOption(
        id = "gpt-4o",
        name = "GPT-4o",
        provider = "OpenAI",
        description = "Omni multimodal model with high speed and broad world knowledge"
    ),
    ModelOption(
        id = "deepseek-r1",
        name = "DeepSeek R1",
        provider = "DeepSeek",
        description = "Open-weights reasoning model with explicit chain of thought"
    )
)

/**
 * Port of assistant-ui/elements/model-selector
 * Dialog allowing the user to view and switch the active agent model.
 */
@Composable
fun ModelSelectorDialog(
    selectedModel: String,
    onModelSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = AssistantUiTokens.PaperShape,
            color = AssistantUiTokens.paperColor(),
            border = AssistantUiTokens.paperBorder(),
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Select Agent Model",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Pick the underlying model that drives Hermes in this session.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    HERMES_SUPPORTED_MODELS.forEach { model ->
                        val isSelected = selectedModel.contains(model.name) || model.name.contains(selectedModel)

                        Surface(
                            shape = AssistantUiTokens.FieldShape,
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                            border = if (isSelected) AssistantUiTokens.paperBorder() else AssistantUiTokens.fieldBorder(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(AssistantUiTokens.FieldShape)
                                .clickable {
                                    onModelSelected(model.name)
                                    onDismiss()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = model.name,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                fontSize = 13.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                            modifier = Modifier.padding(start = 2.dp)
                                        ) {
                                            Text(
                                                text = model.provider,
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = model.description,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier
                                            .size(18.dp)
                                            .padding(start = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Close")
                    }
                }
            }
        }
    }
}
