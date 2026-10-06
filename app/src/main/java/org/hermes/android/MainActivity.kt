package org.hermes.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import org.hermes.android.ui.chat.ChatScreen
import org.hermes.android.ui.chat.ChatViewModel
import org.hermes.android.ui.theme.HermesTheme

class MainActivity : ComponentActivity() {

    private val chatViewModel: ChatViewModel by viewModels {
        val app = application as HermesApp
        ChatViewModel.Factory(app.chatRepository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HermesTheme {
                ChatScreen(viewModel = chatViewModel)
            }
        }
    }
}
