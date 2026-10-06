package org.hermes.android

import android.app.Application
import org.hermes.android.data.network.HermesWebSocketClient
import org.hermes.android.data.repository.ChatRepository
import org.hermes.android.data.repository.HermesAuthRepository

class HermesApp : Application() {

    lateinit var authRepository: HermesAuthRepository
        private set

    lateinit var webSocketClient: HermesWebSocketClient
        private set

    lateinit var chatRepository: ChatRepository
        private set

    override fun onCreate() {
        super.onCreate()
        authRepository = HermesAuthRepository(this)
        webSocketClient = HermesWebSocketClient(authRepository)
        chatRepository = ChatRepository(authRepository, webSocketClient)
    }
}
