package org.hermes.android.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Design tokens and shapes matching the assistant-ui element catalog specification.
 * Corresponds to paper, field, inkButton, and ghostButton tokens in assistant-ui.
 */
object AssistantUiTokens {
    val PaperShape: Shape = RoundedCornerShape(24.dp)
    val FieldShape: Shape = RoundedCornerShape(18.dp)
    val PillShape: Shape = CircleShape

    @Composable
    fun paperBorder(strokeWidth: Dp = 1.dp): BorderStroke {
        return BorderStroke(strokeWidth, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    }

    @Composable
    fun fieldBorder(strokeWidth: Dp = 1.dp): BorderStroke {
        return BorderStroke(strokeWidth, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    }

    @Composable
    fun paperColor(): Color {
        return MaterialTheme.colorScheme.surfaceContainerLow
    }

    @Composable
    fun fieldColor(): Color {
        return MaterialTheme.colorScheme.surfaceContainer
    }

    @Composable
    fun inkContainerColor(): Color {
        return MaterialTheme.colorScheme.primary
    }

    @Composable
    fun inkContentColor(): Color {
        return MaterialTheme.colorScheme.onPrimary
    }

    @Composable
    fun stopContainerColor(): Color {
        return MaterialTheme.colorScheme.errorContainer
    }

    @Composable
    fun stopContentColor(): Color {
        return MaterialTheme.colorScheme.onErrorContainer
    }
}
