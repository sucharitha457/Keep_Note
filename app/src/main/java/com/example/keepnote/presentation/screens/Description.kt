import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter

sealed class EditorBlock {
    data class TextBlock(val text: String) : EditorBlock()
    data class ImageBlock(val uri: Uri) : EditorBlock()
}

fun serializeEditorBlocks(blocks: List<EditorBlock>): String {
    return buildString {
        for (block in blocks) {
            when (block) {
                is EditorBlock.TextBlock -> {
                    val escapedText = block.text.replace("[", "\\[").replace("]", "\\]")
                    append("text[$escapedText]")
                }
                is EditorBlock.ImageBlock -> {
                    append("image[${block.uri}]")
                }

                else -> {}
            }
        }
    }.also { Log.d("Serialize", "Full serialized string: $it") }
}

fun deserializeToEditorBlocks(data: String): List<EditorBlock> {
    val blocks = mutableListOf<EditorBlock>()
    var currentIndex = 0
    if (data.isBlank()) {
        return emptyList()
    }
    val blockRegex = Regex("(text|image)\\[(.*?)](?=(text|image)\\[|$)", RegexOption.DOT_MATCHES_ALL)

    blockRegex.findAll(data).forEach { match ->
        val type = match.groupValues[1]
        var value = match.groupValues[2]

        if (type == "text") {
            value = value.replace("\\[", "[").replace("\\]", "]")
        }

        when (type) {
            "text" -> blocks.add(EditorBlock.TextBlock(value))
            "image" -> blocks.add(EditorBlock.ImageBlock(Uri.parse(value)))
            else -> Log.e("Deserialize", "Unknown block type: $type")
        }

        currentIndex = match.range.last + 1
    }
    return blocks.also { Log.d("Deserialize", "Deserialized blocks: $it") }
}

@Composable
fun ClickableMarkdownText(text: String) {
    val annotated = Markdown(text)
    val context = LocalContext.current

    ClickableText(
        text = annotated,
        style = TextStyle(fontSize = 18.sp),
        onClick = { offset ->
            annotated.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.item))
                    context.startActivity(intent)
                }
        }
    )
}


@Composable
fun EditorTextField(
    index: Int,
    text: String,
    onTextChange: (Int, String) -> Unit,
    onBackspaceAtStart: (Int) -> Unit,
    focusRequester: FocusRequester,
    enableToEdit: Boolean
) {
    var internalText by remember { mutableStateOf(text) }
    Column {
        BasicTextField(
            value = internalText,
            onValueChange = {
                internalText = it
                onTextChange(index, it)
            },
            enabled = enableToEdit,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .focusRequester(focusRequester)
                .onPreviewKeyEvent { event ->
                    if (event.key == Key.Backspace && internalText.isEmpty()) {
                        onBackspaceAtStart(index)
                        true
                    } else false
                },
            textStyle = TextStyle(
                fontSize = 18.sp,
                color = Color.Black
            ),
            cursorBrush = SolidColor(Color.Black),
            decorationBox = { innerTextField ->
                Box {
                    if (internalText.isEmpty() && index == 0) {
                        Text("Type here...", color = Color.Gray)
                    }
                    innerTextField()
                }
            },
            visualTransformation = { originalText ->
                val annotated = Markdown(originalText.text)

                TransformedText(
                    text = annotated,
                    offsetMapping = object : OffsetMapping {
                        override fun originalToTransformed(offset: Int): Int {
                            var transformedOffset = offset
                            var shift = 0
                            var current = 0

                            val combinedRegex = Regex("""\*\*[^*]*\*\*|\[[^\]]*]\([^\)]*\)|\[[^\]]*[^\]\(]*""")
                            for (match in combinedRegex.findAll(originalText.text)) {
                                val length = match.value.length
                                val matchStart = match.range.first
                                val matchEnd = match.range.last + 1

                                if (offset < matchStart) break

                                if (match.value.startsWith("**")) {
                                    shift += 4 // 2+2 for **
                                    transformedOffset -= 2
                                } else if (match.value.startsWith("[")) {
                                    val displayText = Regex("""\[(.*?)]""").find(match.value)?.groupValues?.get(1) ?: match.value
                                    shift += match.value.length - displayText.length
                                    transformedOffset -= (match.value.length - displayText.length)
                                }

                                current = matchEnd
                            }

                            return transformedOffset.coerceIn(0, annotated.length)
                        }

                        override fun transformedToOriginal(offset: Int): Int {
                            var originalOffset = offset
                            var shift = 0
                            var current = 0

                            val combinedRegex = Regex("""\*\*[^*]*\*\*|\[[^\]]*]\([^\)]*\)|\[[^\]]*[^\]\(]*""")
                            for (match in combinedRegex.findAll(originalText.text)) {
                                val matchStart = match.range.first
                                val matchEnd = match.range.last + 1

                                if (offset < matchStart - shift) break

                                if (match.value.startsWith("**")) {
                                    shift += 4 // "**" + "**"
                                    originalOffset += 2
                                } else if (match.value.startsWith("[")) {
                                    val displayText = Regex("""\[(.*?)]""").find(match.value)?.groupValues?.get(1) ?: match.value
                                    val diff = match.value.length - displayText.length
                                    shift += diff
                                    originalOffset += diff
                                }

                                current = matchEnd
                            }

                            return originalOffset.coerceIn(0, originalText.text.length)
                        }
                    }
                )
            }
        )
    }
}

fun Markdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var lastIndex = 0

        val regex = Regex("""\*\*(.*?)\*\*|\[([^\]]+)]\(([^)]+)\)""")

        regex.findAll(text).forEach { match ->
            val start = match.range.first
            val end = match.range.last + 1

            if (lastIndex < start) {
                append(text.substring(lastIndex, start))
            }

            when {
                match.groups[1] != null -> {
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(match.groups[1]!!.value)
                    }
                }

                match.groups[2] != null && match.groups[3] != null -> {
                    val displayText = match.groups[2]!!.value
                    val url = match.groups[3]!!.value
                    pushStringAnnotation(tag = "URL", annotation = url)
                    withStyle(style = SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline)) {
                        append(displayText)
                    }
                    pop()
                }

                else -> {
                    append(match.value)
                }
            }

            lastIndex = end
        }

        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }
}

@Composable
fun RichEditor(
    editorBlocks: SnapshotStateList<EditorBlock>,
    onTextChange: (Int, String) -> Unit,
    onBackspaceAtStart: (Int) -> Unit,
    focusRequesters: List<FocusRequester>,
    enableToEdit: Boolean,
    navController: NavController,
    firstBlockFocusRequester: FocusRequester? = null,
) {
    var imageurilist = mutableListOf<String>()
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        editorBlocks.forEachIndexed { index, block ->
            when (block) {
                is EditorBlock.TextBlock -> {
                    if (enableToEdit) {
                        EditorTextField(
                            index = index,
                            text = block.text,
                            enableToEdit = enableToEdit,
                            onTextChange = onTextChange,
                            onBackspaceAtStart = onBackspaceAtStart,
                            focusRequester = focusRequesters.getOrNull(index) ?: FocusRequester()
                        )
                    }else{
                        ClickableMarkdownText(text = block.text)
                    }
                }

                is EditorBlock.ImageBlock -> {
                    Log.d("ImageBlock", "Image URI: ${block.uri}")
                    Image(
                        painter = rememberAsyncImagePainter(model = block.uri),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .then(
                                if (index == 0 && firstBlockFocusRequester != null)
                                    Modifier.focusRequester(firstBlockFocusRequester)
                                else Modifier
                            )
                            .clickable {
                                if (enableToEdit){
                                    Toast.makeText(context,"Save note to view preview of image",Toast.LENGTH_SHORT).show()
                                }else{
                                    navController.navigate(navToImageScreen(imageurilist,imageurilist.indexOf(block.uri.toString())))
                                }
                            }
                            .clip(RoundedCornerShape(8.dp))
                            .padding(vertical = 8.dp)
                    )
                    imageurilist.add(block.uri.toString())
                }
                else -> Unit
            }
        }
    }
}

fun navToImageScreen(imageList: List<String>,startIndex: Int = 0): String {
    val encodedList = imageList.joinToString(separator = ",")
    return "image?imageList=$encodedList&startIndex=$startIndex"
}
