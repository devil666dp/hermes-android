package org.hermes.android.ui.chat.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hermes.android.data.model.UsageState
import org.hermes.android.ui.theme.AssistantUiTokens

data class StagedAttachment(
    val name: String,
    val size: String
)

data class SlashCommandItem(
    val command: String,
    val description: String
)

val DEFAULT_SLASH_COMMANDS = listOf(
    SlashCommandItem("/status", "Check Zerops gateway & agent state"),
    SlashCommandItem("/help", "List available Hermes commands"),
    SlashCommandItem("/skills", "Show active skills and tools"),
    SlashCommandItem("/models", "Switch active LLM model"),
    SlashCommandItem("/reset", "Reset context and session"),
    SlashCommandItem("/clear", "Clear chat transcript")
)

val QUICK_SUGGESTIONS = listOf(
    "Check Zerops status",
    "Explain last error",
    "Run diagnostics",
    "Show active skills"
)

/**
 * Port of assistant-ui/elements/composer and assistant-ui/elements/mobile-composer.
 * Integrates:
 * - Quick suggestion chips (ThreadPrimitive.Suggestion)
 * - Slash command popover / picker
 * - Staged attachments preview
 * - Auto-resizing composer input
 * - Model & context token indicators in toolbar
 * - Canonical Send / Stop swapping action button
 */
@Composable
fun ChatComposer(
    input: String,
    isGenerating: Boolean,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onInterrupt: () -> Unit,
    activeModel: String = "Hermes 3",
    usageState: UsageState = UsageState(),
    onOpenModelPicker: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var stagedFiles by remember { mutableStateOf(listOf<StagedAttachment>()) }
    val isSlash = input.startsWith("/")
    val filteredCommands = remember(input) {
        if (isSlash) {
            val query = input.lowercase()
            DEFAULT_SLASH_COMMANDS.filter { it.command.startsWith(query) }
        } else {
            emptyList()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        // 1. Quick suggestion chips row (when input is empty and not generating)
        AnimatedVisibility(
            visible = input.isEmpty() && !isGenerating,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QUICK_SUGGESTIONS.forEach { action ->
                    Surface(
                        shape = AssistantUiTokens.PillShape,
                        color = AssistantUiTokens.fieldColor(),
                        border = AssistantUiTokens.fieldBorder(),
                        modifier = Modifier
                            .clip(AssistantUiTokens.PillShape)
                            .clickable {
                                onInputChange(action)
                            }
                    ) {
                        Text(
                            text = action,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // 2. Slash command dropdown suggestions
        AnimatedVisibility(
            visible = isSlash && filteredCommands.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                shape = AssistantUiTokens.FieldShape,
                color = AssistantUiTokens.paperColor(),
                border = AssistantUiTokens.paperBorder(),
                tonalElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    filteredCommands.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onInputChange(item.command)
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = item.command,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
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

        // 3. Staged attachments row
        AnimatedVisibility(
            visible = stagedFiles.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                stagedFiles.forEachIndexed { index, file ->
                    Surface(
                        shape = AssistantUiTokens.FieldShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        border = AssistantUiTokens.fieldBorder(),
                        modifier = Modifier.clip(AssistantUiTokens.FieldShape)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = file.name,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable {
                                        stagedFiles = stagedFiles.toMutableList().also { it.removeAt(index) }
                                    }
                            )
                        }
                    }
                }
            }
        }

        // 4. Main Composer Bar (assistant-ui paper token surface)
        Surface(
            shape = AssistantUiTokens.PaperShape,
            color = AssistantUiTokens.paperColor(),
            border = AssistantUiTokens.paperBorder(),
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                // Composer Input area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp, bottom = 8.dp)
                ) {
                    if (input.isEmpty()) {
                        Text(
                            text = "Message Hermes... (Type / for commands)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        )
                    }
                    BasicTextField(
                        value = input,
                        onValueChange = onInputChange,
                        textStyle = TextStyle(
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 24.dp, max = 120.dp)
                    )
                }

                // Composer Toolbar (Add Attachment, Model Pill, Context Usage, Mic, Send/Stop)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Add Attachment button
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.6f),
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .clickable {
                                    // Stage a simulated file attachment or trigger system picker
                                    stagedFiles = stagedFiles + StagedAttachment(
                                        name = "context-log-${stagedFiles.size + 1}.txt",
                                        size = "2.4 KB"
                                    )
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add attachment",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Model Badge Pill (assistant-ui composer-model-picker)
                        Surface(
                            shape = AssistantUiTokens.PillShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f),
                            border = AssistantUiTokens.fieldBorder(),
                            modifier = Modifier
                                .clip(AssistantUiTokens.PillShape)
                                .then(if (onOpenModelPicker != null) Modifier.clickable { onOpenModelPicker() } else Modifier)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = activeModel,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Right: Actions (Context indicator, Voice dictation, Send / Stop swap)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Context Usage indicator
                        if (usageState.totalTokens > 0) {
                            val pct = usageState.contextPercent
                            Surface(
                                shape = AssistantUiTokens.PillShape,
                                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.4f),
                                modifier = Modifier.clip(AssistantUiTokens.PillShape)
                            ) {
                                Text(
                                    text = "$pct% ctx",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                            }
                        }

                        // Mic dictation (visible when empty and not generating)
                        if (input.isEmpty() && !isGenerating) {
                            IconButton(
                                onClick = { /* Voice input trigger */ },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice dictation",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Canonical assistant-ui Send / Cancel button
                        if (isGenerating) {
                            // Cancel / Stop button mid-generation
                            Surface(
                                shape = CircleShape,
                                color = AssistantUiTokens.stopContainerColor(),
                                contentColor = AssistantUiTokens.stopContentColor(),
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .clickable { onInterrupt() }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Stop,
                                        contentDescription = "Stop generating",
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        } else {
                            val canSend = input.isNotBlank()
                            Surface(
                                shape = CircleShape,
                                color = if (canSend) AssistantUiTokens.inkContainerColor() else MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = if (canSend) AssistantUiTokens.inkContentColor() else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .then(if (canSend) Modifier.clickable { onSend() } else Modifier)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = "Send message",
                                        tint = if (canSend) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
