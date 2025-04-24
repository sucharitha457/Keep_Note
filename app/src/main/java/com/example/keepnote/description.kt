import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay

@Composable
fun RichTextEditorScreen(initialContent: String = "") {
    val blocks = remember { mutableStateListOf<EditorBlock>() }
    val focusRequesters = remember { mutableStateListOf<FocusRequester>() }
    var savedContent by remember { mutableStateOf(initialContent) }
    val pendingFocusIndex = remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(Unit) {
        blocks.clear()
        focusRequesters.clear()

        if (initialContent.isNotBlank()) {
            val deserialized = deserializeToEditorBlocks(initialContent)
            blocks.addAll(deserialized)
            focusRequesters.addAll(deserialized.map { FocusRequester() })
        } else {
            blocks.add(EditorBlock.TextBlock(""))
            focusRequesters.add(FocusRequester())
        }
    }

    LaunchedEffect(pendingFocusIndex.value) {
        pendingFocusIndex.value?.let { index ->
            focusRequesters.getOrNull(index)?.requestFocus()
            pendingFocusIndex.value = null
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            val lastIndex = blocks.lastIndex
            val lastBlock = blocks.getOrNull(lastIndex)

            if (lastBlock !is EditorBlock.TextBlock || lastBlock.text.isNotBlank()) {
                blocks.add(EditorBlock.ImageBlock(it))
                focusRequesters.add(FocusRequester())

                blocks.add(EditorBlock.TextBlock(""))
                focusRequesters.add(FocusRequester())

                pendingFocusIndex.value = blocks.lastIndex
            } else {
                blocks.add(lastIndex, EditorBlock.ImageBlock(it))
                focusRequesters.add(lastIndex, FocusRequester())

                pendingFocusIndex.value = lastIndex + 1
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        RichEditor(
            editorBlocks = blocks,
            onTextChange = { index, newText ->
                if (index in blocks.indices) {
                    blocks[index] = EditorBlock.TextBlock(newText)
                }
            },
            onBackspaceAtStart = { index ->
                if (index > 0 &&
                    blocks.getOrNull(index) is EditorBlock.TextBlock &&
                    blocks.getOrNull(index - 1) is EditorBlock.ImageBlock
                ) {
                    blocks.removeAt(index)
                    focusRequesters.removeAt(index)

                    blocks.removeAt(index - 1)
                    focusRequesters.removeAt(index - 1)

                    pendingFocusIndex.value = (index - 2).coerceAtLeast(0)
                }
            },
            focusRequesters = focusRequesters,
            enableToEdit = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { imagePickerLauncher.launch("image/*") },
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
        ) {
            Text("Add Image")
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                savedContent = serializeEditorBlocks(blocks)
                Log.d("content", "savedContent: $savedContent")
            }) {
                Text("Save")
            }

            Button(onClick = {
                blocks.clear()
                focusRequesters.clear()

                val restored = deserializeToEditorBlocks(savedContent)
                blocks.addAll(restored)
                focusRequesters.addAll(restored.map { FocusRequester() })
            }) {
                Text("Load")
            }
        }
    }
}


sealed class EditorBlock {
    data class TextBlock(val text: String) : EditorBlock()
    data class ImageBlock(val uri: Uri) : EditorBlock()
}

fun serializeEditorBlocks(blocks: List<EditorBlock>): String {
    return buildString {
        for (block in blocks) {
            when (block) {
                is EditorBlock.TextBlock -> {
                    // Escape [ and ] in text to avoid regex issues
                    val escapedText = block.text.replace("[", "\\[").replace("]", "\\]")
                    append("text[$escapedText]")
                    Log.d("Serialize", "Serialized text block: $escapedText")
                }
                is EditorBlock.ImageBlock -> {
                    append("image[${block.uri}]")
                    Log.d("Serialize", "Serialized image block: ${block.uri}")
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
        Log.w("Deserialize", "Empty input data")
        return emptyList()
    }
    val blockRegex = Regex("(text|image)\\[(.*?)](?=(text|image)\\[|$)", RegexOption.DOT_MATCHES_ALL)

    Log.d("Deserialize", "Input data: $data")

    blockRegex.findAll(data).forEach { match ->
        val type = match.groupValues[1]
        var value = match.groupValues[2]

        if (type == "text") {
            value = value.replace("\\[", "[").replace("\\]", "]")
        }

        Log.d("Deserialize", "Matched block: type=$type, value=$value")

        when (type) {
            "text" -> blocks.add(EditorBlock.TextBlock(value))
            "image" -> blocks.add(EditorBlock.ImageBlock(Uri.parse(value)))
            else -> Log.e("Deserialize", "Unknown block type: $type")
        }

        currentIndex = match.range.last + 1
    }

    if (currentIndex < data.length) {
        Log.w("Deserialize", "Unprocessed data: ${data.substring(currentIndex)}")
    }

    return blocks.also { Log.d("Deserialize", "Deserialized blocks: $it") }
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
        val regex = Regex("""(\*\*[^*]*\*\*|\[[^\]]*]\([^\)]*\)|\[[^\]]*[^]\(]*)""")
        var lastIndex = 0

        regex.findAll(text).forEach { match ->
            val start = match.range.first
            val end = match.range.last + 1
            val matchText = match.value

            append(text.substring(lastIndex, start))

            when {
                matchText.startsWith("**") && matchText.endsWith("**") -> {
                    val boldText = matchText.removeSurrounding("**")
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(boldText)
                    }
                }
                matchText.startsWith("[") && matchText.contains("](") && matchText.endsWith(")") -> {
                    val linkRegex = Regex("""\[([^\]]*)]\(([^)]*)\)""")
                    val linkMatch = linkRegex.find(matchText)
                    val displayText = linkMatch?.groupValues?.get(1) ?: matchText
                    val url = linkMatch?.groupValues?.get(2) ?: ""
                    pushStringAnnotation(tag = "URL", annotation = url)
                    withStyle(style = SpanStyle(color = Color.Blue)) {
                        append(displayText)
                    }
                    pop()
                }
                matchText.startsWith("[") -> {
                    val partialText = matchText.removeSurrounding("[", "]")
                    withStyle(style = SpanStyle(color = Color.Red)) {
                        append(partialText)
                    }
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
@Composable
fun RichEditor(
    editorBlocks: SnapshotStateList<EditorBlock>,
    onTextChange: (Int, String) -> Unit,
    onBackspaceAtStart: (Int) -> Unit,
    focusRequesters: List<FocusRequester>,
    enableToEdit: Boolean
) {
    var imageurilist = mutableListOf<Uri>()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        editorBlocks.forEachIndexed { index, block ->
            when (block) {
                is EditorBlock.TextBlock -> {
                    EditorTextField(
                        index = index,
                        text = block.text,
                        enableToEdit = enableToEdit,
                        onTextChange = onTextChange,
                        onBackspaceAtStart = onBackspaceAtStart,
                        focusRequester = focusRequesters.getOrNull(index) ?: FocusRequester()
                    )
                }

                is EditorBlock.ImageBlock -> {
                    Image(
                        painter = rememberAsyncImagePainter(model = block.uri),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clickable {

                            }
                            .clip(RoundedCornerShape(8.dp))
                            .padding(vertical = 8.dp)
                    )
                    imageurilist.add(block.uri)
                }

                else -> Unit
            }
        }
    }
}
