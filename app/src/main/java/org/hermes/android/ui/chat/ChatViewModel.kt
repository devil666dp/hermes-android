package org.hermes.android.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.hermes.android.data.model.ApprovalChoice
import org.hermes.android.data.repository.ChatRepository

class ChatViewModel(
    private val repository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        // Collect messages from repository
        viewModelScope.launch {
            repository.messages.collect { msgList ->
                _uiState.update { it.copy(messages = msgList) }
            }
        }

        // Collect connection status
        viewModelScope.launch {
            repository.connectionStatus.collect { status ->
                _uiState.update { it.copy(connectionStatus = status) }
            }
        }

        // Collect isGenerating
        viewModelScope.launch {
            repository.isGenerating.collect { generating ->
                _uiState.update { it.copy(isGenerating = generating) }
            }
        }

        // Collect current sessionId
        viewModelScope.launch {
            repository.currentSessionId.collect { id ->
                _uiState.update { it.copy(sessionId = id) }
            }
        }

        // Auto-connect with saved or default credentials
        connectWithCredentials("sunil", "rashmoni$034")
    }

    fun connectWithCredentials(username: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(errorMessage = null) }
            val loginRes = repository.authRepo.login(username, pass)
            if (loginRes.isSuccess) {
                val initRes = repository.initializeChat(_uiState.value.activeProfile)
                if (initRes.isFailure) {
                    _uiState.update {
                        it.copy(errorMessage = "WebSocket initialization failed: ${initRes.exceptionOrNull()?.message}")
                    }
                }
            } else {
                _uiState.update {
                    it.copy(errorMessage = "Authentication failed: ${loginRes.exceptionOrNull()?.message}")
                }
            }
        }
    }

    fun onPromptChange(newText: String) {
        _uiState.update { it.copy(promptInput = newText) }
    }

    fun submitPrompt() {
        val text = _uiState.value.promptInput.trim()
        if (text.isEmpty()) return

        _uiState.update { it.copy(promptInput = "") }

        viewModelScope.launch {
            // Check for background command (/btw, /bg)
            if (text.startsWith("/btw ", ignoreCase = true) || text.startsWith("/bg ", ignoreCase = true)) {
                val question = text.substringAfter(" ").trim()
                repository.submitBackground(question)
                return@launch
            }

            // Check for slash command
            if (text.startsWith("/")) {
                val parts = text.removePrefix("/").split(" ", limit = 2)
                val cmd = parts[0]
                val args = if (parts.size > 1) parts[1] else ""
                val slashRes = repository.executeSlash(cmd, args)
                if (slashRes.isFailure) {
                    _uiState.update { it.copy(errorMessage = "Command error: ${slashRes.exceptionOrNull()?.message}") }
                }
                return@launch
            }

            // Normal prompt
            val result = repository.submitPrompt(text, _uiState.value.activeProfile)
            if (result.isFailure) {
                _uiState.update { it.copy(errorMessage = "Failed to send: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    fun onClarifyAnswer(requestId: String, answer: String) {
        viewModelScope.launch {
            repository.respondClarify(requestId, answer)
        }
    }

    fun onApprovalChoice(requestId: String, choice: ApprovalChoice) {
        viewModelScope.launch {
            repository.respondApproval(requestId, choice)
        }
    }

    fun selectModel(modelName: String) {
        _uiState.update { it.copy(activeModel = modelName) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(messages = emptyList()) }
    }

    fun interrupt() {
        viewModelScope.launch {
            repository.interrupt()
        }
    }

    class Factory(private val repository: ChatRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ChatViewModel(repository) as T
        }
    }
}
