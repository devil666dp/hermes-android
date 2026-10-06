package org.hermes.android.ui.chat

import org.hermes.android.data.model.ChatMessage
import org.hermes.android.data.model.UsageState
import org.hermes.android.data.network.ConnectionStatus

data class ChatUiState(
    val sessionId: String? = null,
    val messages: List<ChatMessage> = emptyList(),
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val isGenerating: Boolean = false,
    val promptInput: String = "",
    val usageState: UsageState = UsageState(),
    val activeModel: String = "Nous Hermes 3",
    val activeProfile: String = "default",
    val errorMessage: String? = null
)
