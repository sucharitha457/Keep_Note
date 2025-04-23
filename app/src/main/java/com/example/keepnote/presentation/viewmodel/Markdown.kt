package com.example.keepnote.presentation.viewmodel

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color

fun Markdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        val regex = Regex("""(\*\*[^*]+\*\*|\[([^\]]+)]\([^\)]+\))""")
        var lastIndex = 0

        regex.findAll(text).forEach { match ->
            val start = match.range.first
            val end = match.range.last + 1

            append(text.substring(lastIndex, start))

            val matchText = match.value
            when {
                matchText.startsWith("**") && matchText.endsWith("**") -> {
                    val boldText = matchText.removeSurrounding("**")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(boldText)
                    }
                }

                matchText.startsWith("[") && matchText.contains("](") && matchText.endsWith(")") -> {
                    val displayText = Regex("""\[(.*?)\]""").find(matchText)?.groupValues?.get(1) ?: ""
                    val url = Regex("""\((.*?)\)""").find(matchText)?.groupValues?.get(1) ?: ""

                    pushStringAnnotation(tag = "URL", annotation = url)
                    withStyle(style = SpanStyle(color = Color.Blue)) {
                        append(displayText)
                    }
                    pop()
                }

                else -> append(matchText)
            }

            lastIndex = end
        }

        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}
