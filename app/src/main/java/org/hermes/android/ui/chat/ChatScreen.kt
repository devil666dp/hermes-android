package org.hermes.android.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.hermes.android.data.network.ConnectionStatus
import org.hermes.android.ui.chat.components.*
import org.hermes.android.ui.theme.AssistantUiTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val dotColor = when (state.connectionStatus) {
                            ConnectionStatus.CONNECTED -> Color(0xFF4CAF50)
                            ConnectionStatus.CONNECTING -> Color(0xFFFFC107)
                            else -> Color(0xFFF44336)
                        }
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .background(dotColor, CircleShape)
                        )
                        Column {
                            Text(
                                text = "Hermes Agent",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = when (state.connectionStatus) {
                                    ConnectionStatus.CONNECTED -> "Zerops prg1 · ${state.sessionId?.take(8) ?: "Active"}"
                                    ConnectionStatus.CONNECTING -> "Connecting to Zerops..."
                                    else -> "Disconnected"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // assistant-ui Agent Status Pill in top bar
                    val agentState = when {
                        state.isGenerating -> AgentState.WORKING
                        state.connectionStatus == ConnectionStatus.CONNECTING -> AgentState.WAITING
                        state.errorMessage != null -> AgentState.FAILED
                        else -> AgentState.DONE
                    }
                    val statusLabel = when {
                        state.isGenerating -> "Thinking..."
                        state.connectionStatus == ConnectionStatus.CONNECTING -> "Connecting..."
                        state.errorMessage != null -> "Failed"
                        else -> "Ready"
                    }

                    AgentStatusPill(
                        state = agentState,
                        label = statusLabel,
                        elapsed = if (state.isGenerating) "live" else null,
                        modifier = Modifier.padding(end = 4.dp)
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
                onInterrupt = viewModel::interrupt,
                activeModel = state.activeModel,
                usageState = state.usageState
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            UsageContextBar(usage = state.usageState)

            AnimatedVisibility(
                visible = state.errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    border = AssistantUiTokens.fieldBorder(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.errorMessage ?: "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            MessageList(
                messages = state.messages,
                onClarifyAnswer = viewModel::onClarifyAnswer,
                onApprovalChoice = viewModel::onApprovalChoice,
                onSelectStarterPrompt = { prompt ->
                    viewModel.onPromptChange(prompt)
                    viewModel.submitPrompt()
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
