package org.hermes.android.ui.chat.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hermes.android.data.model.ApprovalChoice
import org.hermes.android.data.model.ApprovalMessage
import org.hermes.android.ui.theme.AssistantUiTokens

/**
 * Port of assistant-ui/elements/approval-card
 * Human in the loop action approval before the agent takes consequential actions.
 */
@Composable
fun ApprovalCard(
    message: ApprovalMessage,
    onChoiceSelected: (ApprovalChoice) -> Unit
) {
    var showConfirmAlways by remember { mutableStateOf(false) }

    Surface(
        shape = AssistantUiTokens.PaperShape,
        color = AssistantUiTokens.paperColor(),
        border = AssistantUiTokens.paperBorder(),
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Action Approval Required",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = message.description,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                border = AssistantUiTokens.fieldBorder(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = message.command,
                    fontFamily = FontFamily.Monospace,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (message.resolved) {
                Surface(
                    shape = AssistantUiTokens.PillShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = "Decision: ${message.choice?.value?.replaceFirstChar { it.uppercase() } ?: "Resolved"}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            } else if (showConfirmAlways) {
                Text(
                    text = "Always allow this command in this session without prompt?",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = { showConfirmAlways = false }) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            showConfirmAlways = false
                            onChoiceSelected(ApprovalChoice.ALWAYS)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Confirm Always")
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onChoiceSelected(ApprovalChoice.ONCE) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AssistantUiTokens.inkContainerColor(),
                            contentColor = AssistantUiTokens.inkContentColor()
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Approve")
                    }
                    OutlinedButton(
                        onClick = { showConfirmAlways = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Always")
                    }
                    FilledTonalButton(
                        onClick = { onChoiceSelected(ApprovalChoice.REJECT) },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reject")
                    }
                }
            }
        }
    }
}
