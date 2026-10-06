package org.hermes.android.ui.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import org.hermes.android.data.network.ConnectionStatus
import org.hermes.android.ui.chat.components.*
import org.hermes.android.ui.theme.AssistantUiTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    val state by viewModel.uiState.collectAsState()

    var showModelDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    // Live elapsed timer while generating
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    LaunchedEffect(state.isGenerating) {
        if (state.isGenerating) {
            elapsedSeconds = 0
            while (true) {
                delay(1000)
                elapsedSeconds++
            }
        } else {
            elapsedSeconds = 0
        }
    }

    val formattedElapsed = remember(elapsedSeconds) {
        val mins = elapsedSeconds / 60
        val secs = elapsedSeconds % 60
        String.format("%d:%02d", mins, secs)
    }

    if (showModelDialog) {
        ModelSelectorDialog(
            selectedModel = state.activeModel,
            onModelSelected = { viewModel.selectModel(it) },
            onDismiss = { showModelDialog = false }
        )
    }

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
                        elapsed = if (state.isGenerating) formattedElapsed else null,
                        onClick = { showModelDialog = true },
                        modifier = Modifier.padding(end = 4.dp)
                    )

                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Switch Model (${state.activeModel})") },
                                onClick = {
                                    showMenu = false
                                    showModelDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Reconnect Gateway") },
                                onClick = {
                                    showMenu = false
                                    viewModel.connectWithCredentials("sunil", "rashmoni$034")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Clear Chat") },
                                onClick = {
                                    showMenu = false
                                    viewModel.clearMessages()
                                }
                            )
                        }
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
                usageState = state.usageState,
                onOpenModelPicker = { showModelDialog = true }
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
