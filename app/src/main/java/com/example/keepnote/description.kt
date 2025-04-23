import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.keepnote.presentation.viewmodel.Markdown

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
            focusRequesters = focusRequesters
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
                is EditorBlock.TextBlock -> append("text[${block.text}]")
                is EditorBlock.ImageBlock -> append("image[${block.uri}]")
                else -> {}
            }
        }
    }
}

fun deserializeToEditorBlocks(data: String): List<EditorBlock> {
    val regex = Regex("(text|image)\\[(.*?)]")
    return regex.findAll(data).map { match ->
        val type = match.groupValues[1]
        val value = match.groupValues[2]
        when (type) {
            "text" -> EditorBlock.TextBlock(value)
            "image" -> EditorBlock.ImageBlock(Uri.parse(value))
            else -> error("Unknown block type")
        }
    }.toList()
}

@Composable
fun EditorTextField(
    index: Int,
    text: String,
    onTextChange: (Int, String) -> Unit,
    onBackspaceAtStart: (Int) -> Unit,
    focusRequester: FocusRequester
) {
    var internalText by remember { mutableStateOf(text) }

    Column {
        BasicTextField(
            value = internalText,
            onValueChange = {
                internalText = it
                onTextChange(index, it)
            },
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
                val transformedText = Markdown(originalText.text)
                TransformedText(
                    text = transformedText,
                    offsetMapping = object : OffsetMapping {
                        override fun originalToTransformed(offset: Int): Int {
                            var count = 0
                            var transformedIndex = 0
                            val regex = Regex("""(\*\*[^*]+\*\*|\[([^\]]+)]\([^\)]+\))""")
                            regex.findAll(originalText.text).forEach { match ->
                                if (match.range.first < offset) {
                                    val matchedText = match.value
                                    if (matchedText.startsWith("**") && matchedText.endsWith("**")) {
                                        transformedIndex -= 4
                                    } else if (matchedText.startsWith("[") && matchedText.contains("](") && matchedText.endsWith(")")) {
                                        val originalLinkTextLength = matchedText.indexOf("]") - 1 - matchedText.indexOf("[")
                                        val fullLinkLength = matchedText.length
                                        transformedIndex -= (fullLinkLength - originalLinkTextLength)
                                    }
                                    count++
                                }
                            }
                            return (offset + transformedIndex).coerceIn(0, transformedText.length)
                        }

                        override fun transformedToOriginal(offset: Int): Int {
                            var count = 0
                            var originalIndex = 0
                            val regex = Regex("""(\*\*[^*]+\*\*|\[([^\]]+)]\([^\)]+\))""")
                            regex.findAll(originalText.text).forEach { match ->
                                val matchedText = match.value
                                val matchStart = match.range.first
                                val matchEnd = match.range.last + 1

                                if (count < offset) {
                                    if (matchedText.startsWith("**") && matchedText.endsWith("**")) {
                                        val boldLength = matchedText.length
                                        if (offset >= matchStart - originalIndex && offset < matchEnd - originalIndex - 2) {
                                            return offset + originalIndex + 2
                                        }
                                        originalIndex += 4
                                    } else if (matchedText.startsWith("[") && matchedText.contains("](") && matchedText.endsWith(")")) {
                                        val originalLinkTextLength = matchedText.indexOf("]") - 1 - matchedText.indexOf("[")
                                        val fullLinkLength = matchedText.length
                                        if (offset >= matchStart - originalIndex && offset < matchEnd - originalIndex - (fullLinkLength - originalLinkTextLength)) {
                                            return offset + originalIndex + (fullLinkLength - originalLinkTextLength)
                                        }
                                        originalIndex += (fullLinkLength - originalLinkTextLength)
                                    }
                                }
                                count += matchedText.length
                            }
                            return offset + originalIndex
                        }
                    }
                )
            }
        )
    }
}


@Composable
fun RichEditor(
    editorBlocks: SnapshotStateList<EditorBlock>,
    onTextChange: (Int, String) -> Unit,
    onBackspaceAtStart: (Int) -> Unit,
    focusRequesters: List<FocusRequester>
) {
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
                            .clip(RoundedCornerShape(8.dp))
                            .padding(vertical = 8.dp)
                    )
                }

                else -> Unit
            }
        }
    }
}
