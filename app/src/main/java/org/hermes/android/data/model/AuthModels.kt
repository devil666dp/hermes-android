package org.hermes.android.data.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val provider: String = "basic",
    val username: String,
    val password: String,
    val next: String = ""
)

@Serializable
data class LoginResponse(
    val ok: Boolean,
    val next: String? = null,
    val error: String? = null
)

@Serializable
data class WsTicketResponse(
    val ticket: String,
    val ttl_seconds: Int = 30
)

@Serializable
data class ApiStatusResponse(
    val status: String? = null,
    val version: String? = null,
    val gateway_running: Boolean = false,
    val active_runs: Int = 0,
    val platform: String? = null
)
