package org.hermes.android.ui.chat.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.hermes.android.data.model.*

@Composable
fun MessageList(
    messages: List<ChatMessage>,
    onClarifyAnswer: (String, String) -> Unit,
    onApprovalChoice: (String, ApprovalChoice) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        items(messages, key = { it.id }) { message ->
            when (message) {
                is ChatBubbleMessage -> ChatBubble(message)
                is ReasoningMessage -> ReasoningAccordion(message)
                is ToolCallMessage -> {
                    val matchingResult = messages.find {
                        it is ToolResultMessage && it.callId == message.callId
                    } as? ToolResultMessage
                    ToolActivityCard(call = message, result = matchingResult)
                }
                is ToolResultMessage -> {
                    // Handled inside ToolActivityCard paired with ToolCallMessage
                }
                is ClarifyMessage -> ClarifyCard(
                    message = message,
                    onAnswerSelected = { answer -> onClarifyAnswer(message.requestId, answer) }
                )
                is ApprovalMessage -> ApprovalCard(
                    message = message,
                    onChoiceSelected = { choice -> onApprovalChoice(message.requestId, choice) }
                )
            }
        }
    }
}
