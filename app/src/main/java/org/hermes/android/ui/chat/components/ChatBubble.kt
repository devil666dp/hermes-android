package org.hermes.android.ui.chat.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hermes.android.data.model.ChatBubbleMessage
import org.hermes.android.ui.theme.AssistantUiTokens

/**
 * Port of assistant-ui/elements/message-pair and streaming-text
 * Clean typography, role-aware containers, and message action buttons.
 */
@Composable
fun ChatBubble(message: ChatBubbleMessage) {
    val context = LocalContext.current
    val isUser = message.role == "user"
    val alignment = if (isUser) Alignment.End else Alignment.Start

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorAlpha"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        if (!isUser) {
            // Assistant avatar label
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(18.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }
                Text(
                    text = "Hermes",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Surface(
            shape = if (isUser) {
                RoundedCornerShape(20.dp, 20.dp, 6.dp, 20.dp)
            } else {
                AssistantUiTokens.PaperShape
            },
            color = if (isUser) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                AssistantUiTokens.paperColor()
            },
            border = if (isUser) null else AssistantUiTokens.paperBorder(),
            tonalElevation = if (isUser) 2.dp else 1.dp,
            modifier = Modifier.widthIn(max = 350.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                if (message.content.isNotEmpty()) {
                    Text(
                        text = message.content,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            lineHeight = 22.sp,
                            fontSize = 14.sp
                        ),
                        color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }

                // Streaming cursor indicator
                if (message.pending) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .size(width = 8.dp, height = 14.dp)
                            .alpha(cursorAlpha)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }

                if (message.error != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Error: ${message.error}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // Assistant actions row (copy, etc.)
                if (!isUser && message.content.isNotEmpty() && !message.pending) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Hermes Message", message.content)
                                clipboard.setPrimaryClip(clip)
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy message",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
