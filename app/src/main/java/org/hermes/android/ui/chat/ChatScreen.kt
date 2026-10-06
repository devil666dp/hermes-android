package org.hermes.android.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.hermes.android.data.network.ConnectionStatus
import org.hermes.android.ui.chat.components.ChatComposer
import org.hermes.android.ui.chat.components.MessageList
import org.hermes.android.ui.chat.components.UsageContextBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val dotColor = when (state.connectionStatus) {
                                ConnectionStatus.CONNECTED -> Color(0xFF4CAF50)
                                ConnectionStatus.CONNECTING -> Color(0xFFFFC107)
                                else -> Color(0xFFF44336)
                            }
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(dotColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Hermes Agent",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Text(
                            text = when (state.connectionStatus) {
                                ConnectionStatus.CONNECTED -> "Zerops prg1 · ${state.sessionId?.take(12) ?: "Active"}"
                                ConnectionStatus.CONNECTING -> "Connecting to Zerops..."
                                else -> "Disconnected"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    AssistChip(
                        onClick = { /* Open model switcher */ },
                        label = { Text(state.activeModel, style = MaterialTheme.typography.labelSmall) }
                    )
                    IconButton(onClick = { viewModel.connectWithCredentials("sunil", "rashmoni$034") }) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reconnect")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        bottomBar = {
            ChatComposer(
                input = state.promptInput,
                isGenerating = state.isGenerating,
                onInputChange = viewModel::onPromptChange,
                onSend = viewModel::submitPrompt,
                onInterrupt = viewModel::interrupt
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            UsageContextBar(usage = state.usageState)

            if (state.errorMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.errorMessage ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }

            MessageList(
                messages = state.messages,
                onClarifyAnswer = viewModel::onClarifyAnswer,
                onApprovalChoice = viewModel::onApprovalChoice,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
