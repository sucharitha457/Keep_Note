package com.example.keepnote.presentation.screens

import EditorBlock
import RichEditor
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.keepnote.R
import com.example.keepnote.data.local.NoteEntity
import com.example.keepnote.presentation.viewmodel.DetailViewmodel
import com.example.keepnote.ui.theme.Primary
import deserializeToEditorBlocks
import kotlinx.coroutines.launch
import serializeEditorBlocks


@Composable
fun DetailScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    detailViewmodel: DetailViewmodel = hiltViewModel(),
    noteId: String?
) {
    var existingNoteId by remember { mutableStateOf("") }
    var title by rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    var enableToEdit by rememberSaveable { mutableStateOf(noteId == null) }
    val isNewNote = noteId == null
    var description by remember { mutableStateOf("") }
    val blocks = remember { mutableStateListOf<EditorBlock>() }
    var isApiData by remember { mutableStateOf(false) }
    val savedDesription by remember {
        derivedStateOf {
            serializeEditorBlocks(blocks)
        }
    }
    val context = LocalContext.current
    val focusRequesters = remember { mutableStateListOf<FocusRequester>() }
    val pendingFocusIndex = remember { mutableStateOf<Int?>(null) }
    val nextFocusRequester = remember { FocusRequester() }

    LaunchedEffect(noteId) {
        noteId?.toIntOrNull()?.let { id ->
            val fetchedNote = detailViewmodel.getNote(id)
            if (fetchedNote != null) {
                title = fetchedNote.title
                isApiData = fetchedNote.isApiData
                existingNoteId = fetchedNote.noteId.toString()
                if (fetchedNote.isApiData == true) {
                    if (fetchedNote.image != null) {
                        var text = "image[${fetchedNote.image}]text[${fetchedNote.body}]"
                        description = text
                    } else {
                        description = "text[${fetchedNote.body}]"
                    }
                } else {
                    description = fetchedNote.body
                }
                Log.d("note", "DetailScreen: $fetchedNote")
                Log.d("note", "hello ${isApiData}")
            }
        } ?: run {
            isApiData = false
            existingNoteId = ""
        }
        blocks.clear()
        if (description != "") {
            Log.d("note in if ", "DetailScreen: $description")
            var deserialized = deserializeToEditorBlocks(description)
            blocks.addAll(deserialized)
            focusRequesters.addAll(deserialized.map { FocusRequester() })
        } else {
            Log.d("note in else ", "DetailScreen: $description")
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
    LaunchedEffect(Unit) {
        if (pendingFocusIndex.value == 0 && focusRequesters.isNotEmpty()) {
            focusRequesters[0].requestFocus()
        }
    }

    val imagePickerLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        it, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (e: SecurityException) {
                    e.printStackTrace()
                }

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


    Column(
        modifier = modifier.padding(horizontal = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { navController.popBackStack() }, modifier = Modifier
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Spacer(modifier = Modifier.weight(1f))
            if (!isApiData) {
                if (enableToEdit || isNewNote) {
                    Button(
                        colors = ButtonColors(containerColor = Primary, contentColor = Color.White , disabledContentColor = Color.LightGray, disabledContainerColor = Color.LightGray),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(horizontal = 4.dp),
                        onClick = {

                            if (title.trim().length != 0) {
                                Log.d("DetailScreen", "Saved: ")
                                val newNote = NoteEntity(
                                    id = noteId?.toIntOrNull() ?: 0,
                                    title = title,
                                    noteId = existingNoteId,
                                    archived = false,
                                    body = savedDesription,
                                    created_time = System.currentTimeMillis() / 1000,
                                    image = "",
                                    isApiData = isApiData
                                )
                                Log.d("DetailScreen", "Saved: ")
                                enableToEdit = false
                                Log.d("DetailScreen", "enableToEdit: $enableToEdit")
                                if (isNewNote) {
                                    detailViewmodel.saveNote(newNote)
                                } else {
                                    detailViewmodel.updateNote(newNote)
                                }
                            } else {
                                Log.d("DetailScreen", "not saved ")
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Title and Description cannot be empty")
                                }
                            }
                        }) {
                        Text("Save", fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        colors = ButtonColors(containerColor = Primary, contentColor = Color.White , disabledContentColor = Color.LightGray, disabledContainerColor = Color.LightGray),
                        shape = RoundedCornerShape(8.dp),
                        onClick = {
                            imagePickerLauncher.launch("image/*")
                        }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_attach_file_24),
                            contentDescription = "Attach File"
                        )
                    }
                } else {
                    Button(
                        colors = ButtonColors(containerColor = Primary, contentColor = Color.White , disabledContentColor = Color.LightGray, disabledContainerColor = Color.LightGray),
                        shape = RoundedCornerShape(8.dp),
                        onClick = {
                            enableToEdit = true
                        }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
                if (!isNewNote) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        colors = ButtonColors(containerColor = Primary, contentColor = Color.White , disabledContentColor = Color.LightGray, disabledContainerColor = Color.LightGray),

                        shape = RoundedCornerShape(8.dp),
                        onClick = {
                            noteId?.toIntOrNull()?.let {
                                detailViewmodel.deleteNote(it)
                                navController.popBackStack()
                            }
                        }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                } else if (isNewNote) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        colors = ButtonColors(containerColor = Primary, contentColor = Color.White , disabledContentColor = Color.LightGray, disabledContainerColor = Color.LightGray),

                        shape = RoundedCornerShape(8.dp),
                        onClick = {
                            title = ""
                            navController.popBackStack()
                        }) {
                        Text("Discard")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        BasicTextField(
            value = title,
            onValueChange = { title = it },
            textStyle = TextStyle(color = Color.Black, fontSize = 24.sp),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(nextFocusRequester)
                .padding(horizontal = 4.dp),
            decorationBox = { innerTextField ->
                if (title.isEmpty()) {
                    Text(text = "Title", color = Color.Gray, fontSize = 24.sp)
                }
                innerTextField()
            },
            maxLines = 1,
            enabled = if (isNewNote || enableToEdit) true else false,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusRequesters.firstOrNull()?.requestFocus()
                }
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        RichEditor(
            editorBlocks = blocks,
            enableToEdit = if (isNewNote || enableToEdit) true else false,
            onTextChange = { index, newText ->
                if (index in blocks.indices) {
                    blocks[index] = EditorBlock.TextBlock(newText)
                }
            },
            onBackspaceAtStart = { index ->
                if (index > 0 && blocks.getOrNull(index) is EditorBlock.TextBlock && blocks.getOrNull(
                        index - 1
                    ) is EditorBlock.ImageBlock
                ) {
                    blocks.removeAt(index)
                    focusRequesters.removeAt(index)

                    blocks.removeAt(index - 1)
                    focusRequesters.removeAt(index - 1)

                    pendingFocusIndex.value = (index - 2).coerceAtLeast(0)
                }
            },
            focusRequesters = focusRequesters,
            navController = navController,
            firstBlockFocusRequester = focusRequesters.firstOrNull()
        )
    }
}

