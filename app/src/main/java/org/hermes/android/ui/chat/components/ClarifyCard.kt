package org.hermes.android.ui.chat.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hermes.android.data.model.ClarifyMessage
import org.hermes.android.ui.theme.AssistantUiTokens

/**
 * Port of assistant-ui/elements/elicitation-form & option-list
 * Interactive clarification card with choices and free-form response option.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClarifyCard(
    message: ClarifyMessage,
    onAnswerSelected: (String) -> Unit
) {
    var customText by remember { mutableStateOf("") }

    Surface(
        shape = AssistantUiTokens.PaperShape,
        color = AssistantUiTokens.paperColor(),
        border = AssistantUiTokens.paperBorder(),
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.HelpOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Clarification Needed",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message.question,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (message.resolved) {
                Surface(
                    shape = AssistantUiTokens.PillShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = "Answered: ${message.answer.orEmpty().ifEmpty { "Let Hermes decide" }}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            } else {
                if (message.choices.isNotEmpty()) {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        message.choices.forEach { choice ->
                            Surface(
                                shape = AssistantUiTokens.PillShape,
                                color = AssistantUiTokens.fieldColor(),
                                border = AssistantUiTokens.fieldBorder(),
                                modifier = Modifier
                                    .clip(AssistantUiTokens.PillShape)
                                    .clickable { onAnswerSelected(choice) }
                            ) {
                                Text(
                                    text = choice,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customText,
                        onValueChange = { customText = it },
                        placeholder = { Text("Or enter custom answer...", fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (customText.isNotBlank()) {
                                onAnswerSelected(customText.trim())
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AssistantUiTokens.inkContainerColor(),
                            contentColor = AssistantUiTokens.inkContentColor()
                        ),
                        enabled = customText.isNotBlank()
                    ) {
                        Text("Reply")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                TextButton(
                    onClick = { onAnswerSelected("") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Skip — Let Hermes Decide",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
