package org.hermes.android.data.network

import kotlinx.serialization.json.*
import org.hermes.android.data.model.*

data class DashboardEventState(
    val messages: List<ChatMessage> = emptyList(),
    val reasoningSegmentClosed: Boolean = false
)

object DashboardEventAdapter {

    fun applyEvent(
        state: DashboardEventState,
        event: DashboardStreamEvent,
        activeTurn: ActiveTurn? = null,
        now: Long = System.currentTimeMillis()
    ): DashboardEventState {
        val payloadObj = event.payload as? JsonObject ?: JsonObject(emptyMap())

        return when (event.type) {
            "message.start" -> state.copy(reasoningSegmentClosed = false)

            "message.delta" -> {
                val chunk = extractText(payloadObj, "text", "delta")
                if (chunk.isEmpty()) state
                else state.copy(
                    messages = appendAssistantDelta(state.messages, chunk, activeTurn, now),
                    reasoningSegmentClosed = false
                )
            }

            "reasoning.delta" -> {
                val chunk = extractText(payloadObj, "text", "delta", "reasoning")
                if (chunk.isEmpty()) state
                else state.copy(
                    messages = appendReasoningDelta(state.messages, chunk, state.reasoningSegmentClosed, now),
                    reasoningSegmentClosed = false
                )
            }

            "reasoning.available" -> {
                val text = extractText(payloadObj, "text", "delta", "reasoning")
                if (text.isEmpty()) state
                else state.copy(
                    messages = appendReasoningSnapshot(state.messages, text, now),
                    reasoningSegmentClosed = false
                )
            }

            "tool.start", "tool.progress", "tool.generating", "tool.complete" -> {
                val toolName = extractText(payloadObj, "name", "tool", "function", "function_name")
                if (toolName.equals("clarify", ignoreCase = true)) {
                    state.copy(reasoningSegmentClosed = true)
                } else {
                    val callId = extractText(payloadObj, "tool_id", "tool_call_id", "callId", "id")
                        .ifEmpty { "$toolName:${previewFromPayload(payloadObj)}" }
                    val isComplete = event.type == "tool.complete"
                    val isFailed = payloadObj["error"] != null || payloadObj["status"]?.jsonPrimitive?.contentOrNull == "failed"
                    val status = if (isComplete) (if (isFailed) "failed" else "completed") else "running"
                    val args = previewFromPayload(payloadObj)
                    val result = if (isComplete) resultFromPayload(payloadObj) else null

                    state.copy(
                        messages = appendToolEvent(state.messages, callId, toolName, args, status, result),
                        reasoningSegmentClosed = true
                    )
                }
            }

            "clarify.request" -> {
                val requestId = extractText(payloadObj, "request_id", "id")
                val question = extractText(payloadObj, "question", "message", "text")
                if (question.isBlank()) state
                else {
                    val choicesArray = payloadObj["choices"]?.jsonArray
                    val choices = choicesArray?.mapNotNull { it.jsonPrimitive.contentOrNull } ?: emptyList()
                    val id = "clarify-${requestId.ifEmpty { "$now-${state.messages.size}" }}"

                    val clarifyMsg = ClarifyMessage(
                        id = id,
                        requestId = requestId,
                        question = question,
                        choices = choices,
                        unavailable = requestId.isEmpty()
                    )
                    state.copy(
                        messages = state.messages + clarifyMsg,
                        reasoningSegmentClosed = true
                    )
                }
            }

            "approval.request" -> {
                val requestId = extractText(payloadObj, "request_id", "id")
                    .ifEmpty { "approval-${event.session_id ?: "session"}-$now" }
                val command = extractText(payloadObj, "command", "tool", "action")
                val description = extractText(payloadObj, "description", "reason", "message")
                    .ifEmpty { "Hermes is requesting permission to execute a system command." }

                val approvalMsg = ApprovalMessage(
                    id = "approval-$requestId",
                    requestId = requestId,
                    command = command,
                    description = description,
                    unavailable = false
                )
                state.copy(
                    messages = state.messages + approvalMsg,
                    reasoningSegmentClosed = true
                )
            }

            "message.complete" -> {
                val finalText = extractText(payloadObj, "text", "rendered")
                val finalReasoning = extractText(payloadObj, "reasoning")
                val messagesWithReasoning = if (finalReasoning.isNotEmpty() && !hasReasoningSinceLastUser(state.messages)) {
                    state.messages + ReasoningMessage(
                        id = "reasoning-dashboard-$now-${state.messages.size}",
                        text = finalReasoning
                    )
                } else state.messages

                state.copy(
                    messages = completeAssistant(messagesWithReasoning, finalText, activeTurn, now),
                    reasoningSegmentClosed = false
                )
            }

            else -> state
        }
    }

    private fun extractText(obj: JsonObject, vararg keys: String): String {
        for (key in keys) {
            val element = obj[key]
            if (element != null && element !is JsonNull) {
                if (element is JsonPrimitive) return element.content
            }
        }
        return ""
    }

    private fun previewFromPayload(payload: JsonObject): String {
        val direct = extractText(payload, "preview", "label", "command", "context", "message")
        if (direct.isNotEmpty()) return direct
        return payload["args"]?.toString() ?: payload["input"]?.toString() ?: ""
    }

    private fun resultFromPayload(payload: JsonObject): String {
        val res = payload["result"]?.let { if (it is JsonPrimitive) it.content else it.toString() }
            ?: payload["output"]?.let { if (it is JsonPrimitive) it.content else it.toString() }
            ?: ""
        val error = payload["error"]?.let { if (it is JsonPrimitive) it.content else it.toString() }
        return if (!error.isNullOrEmpty() && res.isNotEmpty()) "$res\n\n$error" else error ?: res
    }

    private fun appendAssistantDelta(
        messages: List<ChatMessage>,
        chunk: String,
        activeTurn: ActiveTurn?,
        now: Long
    ): List<ChatMessage> {
        val last = messages.lastOrNull()
        return if (last is ChatBubbleMessage && last.role == "agent" && last.error == null) {
            messages.dropLast(1) + last.copy(
                content = last.content + chunk,
                pending = true,
                turnId = last.turnId ?: activeTurn?.turnId
            )
        } else {
            messages + ChatBubbleMessage(
                id = "agent-stream-$now-${messages.size}",
                role = "agent",
                content = chunk,
                pending = true,
                turnId = activeTurn?.turnId
            )
        }
    }

    private fun appendReasoningDelta(
        messages: List<ChatMessage>,
        chunk: String,
        forceNewSegment: Boolean,
        now: Long
    ): List<ChatMessage> {
        val last = messages.lastOrNull()
        return if (!forceNewSegment && last is ReasoningMessage) {
            messages.dropLast(1) + last.copy(text = last.text + chunk, isStreaming = true)
        } else {
            messages + ReasoningMessage(
                id = "reasoning-$now-${messages.size}",
                text = chunk,
                isStreaming = true
            )
        }
    }

    private fun appendReasoningSnapshot(
        messages: List<ChatMessage>,
        text: String,
        now: Long
    ): List<ChatMessage> {
        return messages + ReasoningMessage(
            id = "reasoning-snap-$now-${messages.size}",
            text = text,
            isStreaming = false
        )
    }

    private fun appendToolEvent(
        messages: List<ChatMessage>,
        callId: String,
        name: String,
        args: String,
        status: String,
        result: String?
    ): List<ChatMessage> {
        val existingIndex = messages.indexOfLast { it is ToolCallMessage && it.callId == callId }
        val updated = messages.toMutableList()

        if (existingIndex >= 0) {
            val current = updated[existingIndex] as ToolCallMessage
            updated[existingIndex] = current.copy(
                name = name.ifEmpty { current.name },
                args = args.ifEmpty { current.args },
                status = status
            )
        } else {
            updated.add(
                ToolCallMessage(
                    id = "tool-call-$callId",
                    callId = callId,
                    name = name.ifEmpty { "tool" },
                    args = args,
                    status = status
                )
            )
        }

        if (result != null) {
            val resultExists = updated.any { it is ToolResultMessage && it.callId == callId && it.content == result }
            if (!resultExists) {
                updated.add(
                    ToolResultMessage(
                        id = "tool-res-$callId-${updated.size}",
                        callId = callId,
                        name = name.ifEmpty { "tool" },
                        content = result
                    )
                )
            }
        }

        return updated
    }

    private fun hasReasoningSinceLastUser(messages: List<ChatMessage>): Boolean {
        for (i in messages.indices.reversed()) {
            val m = messages[i]
            if (m.role == "user") break
            if (m is ReasoningMessage && m.text.isNotBlank()) return true
        }
        return false
    }

    private fun completeAssistant(
        messages: List<ChatMessage>,
        finalText: String,
        activeTurn: ActiveTurn?,
        now: Long
    ): List<ChatMessage> {
        if (finalText.isBlank()) {
            return messages.map {
                if (it is ChatBubbleMessage && it.role == "agent" && it.pending) it.copy(pending = false) else it
            }
        }

        val lastAgentIndex = messages.indexOfLast {
            it is ChatBubbleMessage && it.role == "agent" && (activeTurn == null || it.turnId == activeTurn.turnId)
        }

        return if (lastAgentIndex >= 0) {
            val current = messages[lastAgentIndex] as ChatBubbleMessage
            val merged = LossyTextReconciler.mergeStreamedWithFinal(current.content, finalText)
            val updated = messages.toMutableList()
            updated[lastAgentIndex] = current.copy(content = merged, pending = false)
            updated
        } else {
            messages + ChatBubbleMessage(
                id = "agent-final-$now-${messages.size}",
                role = "agent",
                content = finalText,
                pending = false,
                turnId = activeTurn?.turnId
            )
        }
    }
}
