package org.hermes.android.data.model

import kotlinx.serialization.Serializable

@Serializable
enum class ApprovalChoice(val value: String) {
    ONCE("once"),
    ALWAYS("always"),
    REJECT("reject")
}

@Serializable
data class Attachment(
    val name: String,
    val path: String? = null,
    val kind: String = "file", // "image", "file", "text"
    val sizeBytes: Long = 0,
    val previewUrl: String? = null
)

sealed interface ChatMessage {
    val id: String
    val role: String // "user" or "agent"
}

@Serializable
data class ChatBubbleMessage(
    override val id: String,
    override val role: String, // "user" or "agent"
    val content: String = "",
    val attachments: List<Attachment> = emptyList(),
    val error: String? = null,
    val pending: Boolean = false,
    val localOnly: Boolean = false,
    val turnId: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isSlashLoader: Boolean = false
) : ChatMessage

@Serializable
data class ReasoningMessage(
    override val id: String,
    override val role: String = "agent",
    val text: String = "",
    val durationSeconds: Double? = null,
    val isStreaming: Boolean = false
) : ChatMessage

@Serializable
data class ToolCallMessage(
    override val id: String,
    override val role: String = "agent",
    val callId: String,
    val name: String,
    val args: String = "",
    val status: String = "running" // "running", "completed", "failed"
) : ChatMessage

@Serializable
data class ToolResultMessage(
    override val id: String,
    override val role: String = "agent",
    val callId: String,
    val name: String,
    val content: String = "",
    val attachments: List<Attachment> = emptyList()
) : ChatMessage

@Serializable
data class ClarifyMessage(
    override val id: String,
    override val role: String = "agent",
    val requestId: String,
    val question: String,
    val choices: List<String> = emptyList(),
    val answer: String? = null,
    val resolved: Boolean = false,
    val unavailable: Boolean = false
) : ChatMessage

@Serializable
data class ApprovalMessage(
    override val id: String,
    override val role: String = "agent",
    val requestId: String,
    val command: String,
    val description: String,
    val choices: List<ApprovalChoice> = listOf(ApprovalChoice.ONCE, ApprovalChoice.ALWAYS, ApprovalChoice.REJECT),
    val choice: ApprovalChoice? = null,
    val resolved: Boolean = false,
    val unavailable: Boolean = false
) : ChatMessage

@Serializable
data class ActiveTurn(
    val turnId: String,
    val userId: String,
    val startIndex: Int,
    val status: String = "running" // "running", "failed", "completed"
)

@Serializable
data class UsageState(
    val promptTokens: Long = 0,
    val completionTokens: Long = 0,
    val totalTokens: Long = 0,
    val cost: Double? = null,
    val contextTokens: Long? = null,
    val contextWindowTokens: Long? = null,
    val cacheReadTokens: Long? = null,
    val cacheWriteTokens: Long? = null
) {
    val tokensUsed: Long get() = totalTokens
    val contextPercent: Int get() = if (contextTokens != null && contextWindowTokens != null && contextWindowTokens > 0) {
        ((contextTokens.toDouble() / contextWindowTokens) * 100).toInt()
    } else 0
}
