package org.hermes.android.ui.chat.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.hermes.android.data.model.*

/**
 * Chat thread message list integrating assistant-ui components:
 * - Empty state with starter prompts
 * - Thread message pairing (user bubbles, assistant replies, reasoning, tool activity)
 * - Scroll anchor pill when user scrolls away from bottom
 */
@Composable
fun MessageList(
    messages: List<ChatMessage>,
    onClarifyAnswer: (String, String) -> Unit,
    onApprovalChoice: (String, ApprovalChoice) -> Unit,
    onSelectStarterPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Determine if user has scrolled away from bottom
    val isScrolledToBottom by remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            if (totalItems == 0) return@derivedStateOf true
            val lastVisibleIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleIndex >= totalItems - 2
        }
    }

    // Auto-scroll when new message parts arrive if already at bottom
    LaunchedEffect(messages.size, messages.lastOrNull()?.let { (it as? ChatBubbleMessage)?.content?.length }) {
        if (messages.isNotEmpty() && isScrolledToBottom) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        if (messages.isEmpty()) {
            ChatEmptyState(
                onSelectPrompt = onSelectStarterPrompt,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
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

        // assistant-ui Scroll Anchor pill
        ScrollAnchor(
            visible = !isScrolledToBottom && messages.isNotEmpty(),
            onClick = {
                coroutineScope.launch {
                    if (messages.isNotEmpty()) {
                        listState.animateScrollToItem(messages.size - 1)
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }
}
