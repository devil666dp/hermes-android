package org.hermes.android.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.hermes.android.data.model.*
import org.hermes.android.data.network.ConnectionStatus
import org.hermes.android.data.network.DashboardEventAdapter
import org.hermes.android.data.network.DashboardEventState
import org.hermes.android.data.network.HermesWebSocketClient

class ChatRepository(
    val authRepo: HermesAuthRepository,
    val wsClient: HermesWebSocketClient
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _usageState = MutableStateFlow(UsageState())
    val usageState: StateFlow<UsageState> = _usageState.asStateFlow()

    private var eventState = DashboardEventState()

    val connectionStatus: StateFlow<ConnectionStatus> = wsClient.connectionStatus

    init {
        scope.launch {
            wsClient.events.collect { event ->
                handleIncomingStreamEvent(event)
            }
        }
    }

    suspend fun initializeChat(profile: String = "default"): Result<String> {
        val connectRes = wsClient.connect()
        if (connectRes.isFailure) {
            return Result.failure(connectRes.exceptionOrNull()!!)
        }

        return try {
            val sessionId = wsClient.createSession(profile)
            _currentSessionId.value = sessionId
            Result.success(sessionId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitPrompt(text: String, profile: String = "default"): Result<Unit> {
        val sessionId = _currentSessionId.value ?: return Result.failure(IllegalStateException("No active session"))
        val now = System.currentTimeMillis()

        val userMessage = ChatBubbleMessage(
            id = "user-$now",
            role = "user",
            content = text,
            pending = false,
            timestamp = now
        )

        _messages.update { it + userMessage }
        _isGenerating.value = true

        return try {
            wsClient.submitPrompt(sessionId, text, profile)
            Result.success(Unit)
        } catch (e: Exception) {
            _isGenerating.value = false
            Result.failure(e)
        }
    }

    suspend fun submitBackground(text: String, profile: String = "default"): Result<Unit> {
        val sessionId = _currentSessionId.value ?: return Result.failure(IllegalStateException("No active session"))
        return try {
            wsClient.submitBackground(sessionId, text, profile)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun executeSlash(command: String, args: String = ""): Result<String> {
        val sessionId = _currentSessionId.value ?: ""
        return try {
            val output = wsClient.executeSlash(sessionId, command, args)
            Result.success(output)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun respondClarify(requestId: String, answer: String): Boolean {
        val success = wsClient.respondClarify(requestId, answer)
        if (success) {
            _messages.update { list ->
                list.map {
                    if (it is ClarifyMessage && it.requestId == requestId) {
                        it.copy(answer = answer, resolved = true)
                    } else it
                }
            }
        }
        return success
    }

    suspend fun respondApproval(requestId: String, choice: ApprovalChoice): Boolean {
        val sessionId = _currentSessionId.value ?: return false
        val success = wsClient.respondApproval(sessionId, requestId, choice)
        if (success) {
            _messages.update { list ->
                list.map {
                    if (it is ApprovalMessage && it.requestId == requestId) {
                        it.copy(choice = choice, resolved = true)
                    } else it
                }
            }
        }
        return success
    }

    suspend fun interrupt(): Result<Unit> {
        val sessionId = _currentSessionId.value ?: return Result.success(Unit)
        return try {
            wsClient.interruptSession(sessionId)
            _isGenerating.value = false
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun handleIncomingStreamEvent(event: org.hermes.android.data.model.DashboardStreamEvent) {
        if (event.type == "message.start") {
            _isGenerating.value = true
        } else if (event.type == "message.complete") {
            _isGenerating.value = false
        }

        eventState = eventState.copy(messages = _messages.value)
        val nextState = DashboardEventAdapter.applyEvent(eventState, event)
        eventState = nextState
        _messages.value = nextState.messages
    }
}
