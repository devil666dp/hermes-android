package org.hermes.android.ui.chat.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.hermes.android.ui.theme.AssistantUiTokens

/**
 * Port of assistant-ui/elements/markdown-text and syntax-highlighter.
 * Parses and renders Markdown with fenced code blocks, inline code chips, headers, and bullet lists.
 */
@Composable
fun AssistantMarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val blocks = remember(text) { parseMarkdownBlocks(text) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Header -> {
                    val fontSize = when (block.level) {
                        1 -> 18.sp
                        2 -> 16.sp
                        else -> 15.sp
                    }
                    Text(
                        text = block.text,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = fontSize,
                            lineHeight = (fontSize.value * 1.3).sp
                        ),
                        color = textColor,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }
                is MarkdownBlock.CodeBlock -> {
                    CodeBlockView(
                        language = block.language,
                        code = block.code
                    )
                }
                is MarkdownBlock.BulletItem -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = (block.indent * 12).dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = buildInlineAnnotatedString(block.text, textColor),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 14.sp,
                                lineHeight = 21.sp
                            ),
                            color = textColor
                        )
                    }
                }
                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = buildInlineAnnotatedString(block.text, textColor),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        ),
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
fun CodeBlockView(
    language: String,
    code: String
) {
    val context = LocalContext.current
    var isCopied by remember { mutableStateOf(false) }

    LaunchedEffect(isCopied) {
        if (isCopied) {
            kotlinx.coroutines.delay(2000)
            isCopied = false
        }
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E1E24),
        border = AssistantUiTokens.fieldBorder(),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
    ) {
        Column {
            // Header bar with language & copy button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF16161A))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = language.ifBlank { "code" },
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color(0xFFAAAAAA)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(2.dp)
                ) {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Code", code)
                            clipboard.setPrimaryClip(clip)
                            isCopied = true
                        },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = if (isCopied) Icons.Default.Check else Icons.Default.ContentCopy,
                            contentDescription = "Copy code",
                            tint = if (isCopied) Color(0xFF4CAF50) else Color(0xFFAAAAAA),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            // Code content with horizontal scroll
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp)
            ) {
                Text(
                    text = code,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    ),
                    color = Color(0xFFECEFF1)
                )
            }
        }
    }
}

sealed class MarkdownBlock {
    data class Header(val level: Int, val text: String) : MarkdownBlock()
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock()
    data class BulletItem(val indent: Int, val text: String) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
}

fun parseMarkdownBlocks(markdown: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = markdown.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]

        // Check for fenced code block (```)
        if (line.trimStart().startsWith("```")) {
            val lang = line.trim().removePrefix("```").trim()
            val codeLines = mutableListOf<String>()
            i++
            while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                codeLines.add(lines[i])
                i++
            }
            blocks.add(MarkdownBlock.CodeBlock(lang, codeLines.joinToString("\n")))
            i++
            continue
        }

        // Headers: #, ##, ###
        if (line.startsWith("#")) {
            val level = line.takeWhile { it == '#' }.length
            if (level in 1..4 && line.getOrNull(level) == ' ') {
                val headerText = line.substring(level + 1).trim()
                blocks.add(MarkdownBlock.Header(level, headerText))
                i++
                continue
            }
        }

        // Bullet item: - or *
        if (line.trimStart().startsWith("- ") || line.trimStart().startsWith("* ")) {
            val indent = line.takeWhile { it == ' ' }.length / 2
            val text = line.trimStart().removePrefix("- ").removePrefix("* ").trim()
            blocks.add(MarkdownBlock.BulletItem(indent, text))
            i++
            continue
        }

        // Paragraph line
        if (line.isNotBlank()) {
            val paraLines = mutableListOf<String>()
            while (i < lines.size && lines[i].isNotBlank() && !lines[i].startsWith("#") && !lines[i].trimStart().startsWith("```") && !lines[i].trimStart().startsWith("- ")) {
                paraLines.add(lines[i])
                i++
            }
            blocks.add(MarkdownBlock.Paragraph(paraLines.joinToString("\n")))
            continue
        }

        i++
    }

    return blocks
}

fun buildInlineAnnotatedString(text: String, baseColor: Color): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        val regex = Regex("(`[^`]+`)|(\\*\\*[^*]+\\*\\*)")
        val matches = regex.findAll(text)

        for (match in matches) {
            val range = match.range
            if (range.first > cursor) {
                append(text.substring(cursor, range.first))
            }

            val value = match.value
            if (value.startsWith("`") && value.endsWith("`")) {
                // Inline code
                val inlineCode = value.removeSurrounding("`")
                pushStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        background = Color(0x33888888),
                        fontWeight = FontWeight.Medium
                    )
                )
                append(" $inlineCode ")
                pop()
            } else if (value.startsWith("**") && value.endsWith("**")) {
                // Bold
                val boldText = value.removeSurrounding("**")
                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                append(boldText)
                pop()
            }

            cursor = range.last + 1
        }

        if (cursor < text.length) {
            append(text.substring(cursor))
        }
    }
}
