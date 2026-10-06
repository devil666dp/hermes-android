package org.hermes.android.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.hermes.android.data.model.ApiStatusResponse
import org.hermes.android.data.model.LoginRequest
import org.hermes.android.data.model.LoginResponse
import org.hermes.android.data.model.WsTicketResponse
import java.io.IOException

class HermesAuthRepository(context: Context) {

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        isLenient = true
    }
    private val prefs: SharedPreferences = context.getSharedPreferences("hermes_auth_prefs", Context.MODE_PRIVATE)

    val dashboardUrl = "https://hermes-3238-9119.prg1.zerops.app"
    val gatewayUrl = "https://hermes-3238-8642.prg1.zerops.app"

    private val cookieMap = mutableMapOf<String, Cookie>()

    val cookieJar = object : CookieJar {
        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            cookies.forEach { cookie ->
                cookieMap[cookie.name] = cookie
                prefs.edit().putString("cookie_${cookie.name}", cookie.value).apply()
            }
        }

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return cookieMap.values.toList()
        }
    }

    val httpClient: OkHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .build()

    init {
        listOf("hermes_session_at", "hermes_session_rt", "hermes_session_provider").forEach { name ->
            prefs.getString("cookie_$name", null)?.let { value ->
                cookieMap[name] = Cookie.Builder()
                    .domain("hermes-3238-9119.prg1.zerops.app")
                    .path("/")
                    .name(name)
                    .value(value)
                    .httpOnly()
                    .build()
            }
        }
    }

    fun hasValidSession(): Boolean {
        return cookieMap.containsKey("hermes_session_at")
    }

    suspend fun login(username: String, password: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val reqBody = json.encodeToString(
                LoginRequest.serializer(),
                LoginRequest(provider = "basic", username = username, password = password, next = "")
            )
            val request = Request.Builder()
                .url("$dashboardUrl/auth/password-login")
                .post(reqBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val body = response.body?.string().orEmpty()
            if (response.isSuccessful) {
                val loginRes = json.decodeFromString(LoginResponse.serializer(), body)
                if (loginRes.ok) {
                    Result.success(true)
                } else {
                    Result.failure(IOException(loginRes.error ?: "Authentication rejected"))
                }
            } else {
                Result.failure(IOException("HTTP ${response.code}: $body"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun mintWsTicket(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$dashboardUrl/api/auth/ws-ticket")
                .post("".toRequestBody("application/json".toMediaType()))
                .build()

            val response = httpClient.newCall(request).execute()
            val body = response.body?.string().orEmpty()
            if (response.isSuccessful) {
                val ticketRes = json.decodeFromString(WsTicketResponse.serializer(), body)
                Result.success(ticketRes.ticket)
            } else {
                Result.failure(IOException("Failed to mint ticket (HTTP ${response.code}): $body"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStatus(): Result<ApiStatusResponse> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$dashboardUrl/api/status")
                .get()
                .build()
            val response = httpClient.newCall(request).execute()
            val body = response.body?.string().orEmpty()
            if (response.isSuccessful) {
                Result.success(json.decodeFromString(ApiStatusResponse.serializer(), body))
            } else {
                Result.failure(IOException("Status check failed: HTTP ${response.code}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
