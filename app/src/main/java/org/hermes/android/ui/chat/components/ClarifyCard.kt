package org.hermes.android.ui.chat.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.hermes.android.data.model.ClarifyMessage

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ClarifyCard(
    message: ClarifyMessage,
    onAnswerSelected: (String) -> Unit
) {
    var customText by remember { mutableStateOf("") }

    ElevatedCard(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Clarification Needed",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message.question,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (message.resolved) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Answered: ${message.answer.orEmpty().ifEmpty { "Let Hermes decide" }}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(10.dp)
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
                            SuggestionChip(
                                onClick = { onAnswerSelected(choice) },
                                label = { Text(choice) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = customText,
                        onValueChange = { customText = it },
                        placeholder = { Text("Or enter custom answer...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (customText.isNotBlank()) {
                                onAnswerSelected(customText.trim())
                            }
                        },
                        enabled = customText.isNotBlank()
                    ) {
                        Text("Reply")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { onAnswerSelected("") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Skip — Let Hermes Decide")
                }
            }
        }
    }
}
