package org.hermes.android.data.network

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.*
import okhttp3.*
import org.hermes.android.data.model.*
import org.hermes.android.data.repository.HermesAuthRepository
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    ERROR
}

class HermesWebSocketClient(
    private val authRepo: HermesAuthRepository
) {
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        isLenient = true
    }
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val nextId = AtomicInteger(1)

    private var webSocket: WebSocket? = null
    private val pendingRequests = ConcurrentHashMap<Int, CompletableDeferred<JsonElement>>()

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _events = MutableSharedFlow<DashboardStreamEvent>(extraBufferCapacity = 128)
    val events: SharedFlow<DashboardStreamEvent> = _events.asSharedFlow()

    suspend fun connect(): Result<Unit> = withContext(Dispatchers.IO) {
        if (_connectionStatus.value == ConnectionStatus.CONNECTED) {
            return@withContext Result.success(Unit)
        }

        _connectionStatus.value = ConnectionStatus.CONNECTING
        val ticketResult = authRepo.mintWsTicket()
        if (ticketResult.isFailure) {
            _connectionStatus.value = ConnectionStatus.ERROR
            return@withContext Result.failure(ticketResult.exceptionOrNull() ?: IOException("Failed to obtain WS ticket"))
        }

        val ticket = ticketResult.getOrThrow()
        val wsUrl = "wss://hermes-3238-9119.prg1.zerops.app/api/ws?ticket=$ticket"
        val request = Request.Builder().url(wsUrl).build()

        val connectDeferred = CompletableDeferred<Unit>()

        webSocket = authRepo.httpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _connectionStatus.value = ConnectionStatus.CONNECTED
                if (!connectDeferred.isCompleted) connectDeferred.complete(Unit)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleIncomingMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _connectionStatus.value = ConnectionStatus.ERROR
                if (!connectDeferred.isCompleted) {
                    connectDeferred.completeExceptionally(t)
                }
                failPendingRequests(t)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _connectionStatus.value = ConnectionStatus.DISCONNECTED
            }
        })

        try {
            withTimeout(15000) {
                connectDeferred.await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            _connectionStatus.value = ConnectionStatus.ERROR
            Result.failure(e)
        }
    }

    private fun handleIncomingMessage(text: String) {
        scope.launch {
            try {
                val element = json.parseToJsonElement(text)
                if (element is JsonObject) {
                    val method = element["method"]?.jsonPrimitive?.contentOrNull
                    if (method == "event") {
                        val params = element["params"]?.jsonObject ?: return@launch
                        val type = params["type"]?.jsonPrimitive?.contentOrNull.orEmpty()
                        val sessionId = params["session_id"]?.jsonPrimitive?.contentOrNull
                        val payload = params["payload"]
                        _events.emit(DashboardStreamEvent(type = type, session_id = sessionId, payload = payload))
                    } else if (element.containsKey("id")) {
                        val id = element["id"]?.jsonPrimitive?.intOrNull ?: return@launch
                        val deferred = pendingRequests.remove(id)
                        if (element.containsKey("error") && element["error"] != null && element["error"] !is JsonNull) {
                            deferred?.completeExceptionally(IOException(element["error"].toString()))
                        } else {
                            deferred?.complete(element["result"] ?: JsonNull)
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore parse errors on malformed frames
            }
        }
    }

    private fun failPendingRequests(t: Throwable) {
        val iterator = pendingRequests.entries.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            entry.value.completeExceptionally(t)
            iterator.remove()
        }
    }

    suspend fun request(method: String, params: JsonObject = JsonObject(emptyMap())): JsonElement {
        val id = nextId.getAndIncrement()
        val req = JsonRpcRequest(id = id, method = method, params = params)
        val deferred = CompletableDeferred<JsonElement>()
        pendingRequests[id] = deferred

        val jsonStr = json.encodeToString(JsonRpcRequest.serializer(), req)
        val sent = webSocket?.send(jsonStr) ?: false
        if (!sent) {
            pendingRequests.remove(id)
            throw IOException("WebSocket not connected")
        }

        return withTimeout(30000) {
            deferred.await()
        }
    }

    suspend fun createSession(profile: String = "default"): String {
        val params = buildJsonObject { put("profile", profile) }
        val res = request("session.create", params)
        return res.jsonObject["session_id"]?.jsonPrimitive?.contentOrNull.orEmpty()
    }

    suspend fun resumeSession(sessionId: String, profile: String = "default"): String {
        val params = buildJsonObject {
            put("session_id", sessionId)
            put("profile", profile)
        }
        val res = request("session.resume", params)
        return res.jsonObject["session_id"]?.jsonPrimitive?.contentOrNull ?: sessionId
    }

    suspend fun submitPrompt(sessionId: String, text: String, profile: String = "default") {
        val params = buildJsonObject {
            put("session_id", sessionId)
            put("text", text)
            put("profile", profile)
        }
        request("prompt.submit", params)
    }

    suspend fun submitBackground(sessionId: String, text: String, profile: String = "default") {
        val params = buildJsonObject {
            put("session_id", sessionId)
            put("text", text)
            put("profile", profile)
        }
        request("prompt.background", params)
    }

    suspend fun executeSlash(sessionId: String, command: String, args: String = ""): String {
        val params = buildJsonObject {
            put("session_id", sessionId)
            put("command", command)
            put("args", args)
        }
        val res = request("slash.exec", params)
        return res.jsonObject["output"]?.jsonPrimitive?.contentOrNull.orEmpty()
    }

    suspend fun respondClarify(requestId: String, answer: String): Boolean {
        val params = buildJsonObject {
            put("request_id", requestId)
            put("answer", answer)
        }
        val res = request("clarify.respond", params)
        return res.jsonObject["status"]?.jsonPrimitive?.contentOrNull == "ok"
    }

    suspend fun respondApproval(sessionId: String, requestId: String, choice: ApprovalChoice, all: Boolean = false): Boolean {
        val params = buildJsonObject {
            put("session_id", sessionId)
            put("request_id", requestId)
            put("choice", choice.value)
            put("all", all)
        }
        val res = request("approval.respond", params)
        val resolved = res.jsonObject["resolved"]?.jsonPrimitive?.intOrNull
        return resolved == 1
    }

    suspend fun interruptSession(sessionId: String) {
        val params = buildJsonObject { put("session_id", sessionId) }
        request("session.interrupt", params)
    }

    fun disconnect() {
        webSocket?.close(1000, "Normal Closure")
        _connectionStatus.value = ConnectionStatus.DISCONNECTED
    }
}
