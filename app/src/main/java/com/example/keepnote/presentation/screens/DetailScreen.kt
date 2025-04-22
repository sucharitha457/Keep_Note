package com.example.keepnote.presentation.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.keepnote.R
import com.example.keepnote.data.local.NoteEntity
import com.example.keepnote.presentation.viewmodel.DetailViewmodel
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun DetailScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    detailViewmodel: DetailViewmodel = hiltViewModel(),
    noteId: String?
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var enableToEdit by rememberSaveable { mutableStateOf(noteId == null) }
    val isNewNote = noteId == null

    LaunchedEffect(noteId) {
        noteId?.toIntOrNull()?.let { id ->
            detailViewmodel.getNote(id)?.let { note ->
                title = note.title
                description = note.body
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->

        Column(modifier = modifier.padding(paddingValues).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                FloatingActionButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }

                Spacer(modifier = Modifier.weight(1f))

                if (enableToEdit) {
                    FloatingActionButton(onClick = {
                        Log.d("DEBUG", "Title: '${title}' (${title.length})")
                        Log.d("DEBUG", "Description: '${description}' (${description.length})")
                        if (title.trim().length != 0 || description.trim().length != 0) {
                            Log.d("DetailScreen", "Saved: ")
                            val newNote = NoteEntity(
                                title = title,
                                noteId = noteId ?: Random.nextInt(100000, 999999).toString(),
                                archived = false,
                                body = description,
                                created_time = System.currentTimeMillis(),
                                image = ""
                            )

                            detailViewmodel.saveNote(newNote)
                            Log.d("DetailScreen", "Saved: $newNote")
                            navController.popBackStack()
                        } else {
                            Log.d("DetailScreen", "not saved ")
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Title and Description cannot be empty")
                            }
                        }
                    }) {
                        Text("Save")
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    FloatingActionButton(onClick = {

                    }) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_attach_file_24),
                            contentDescription = "Attach File"
                        )
                    }
                }
                else {
                    FloatingActionButton(onClick = {
                        enableToEdit = true
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
                if (!isNewNote) {
                    Spacer(modifier = Modifier.width(8.dp))
                    FloatingActionButton(onClick = {
                        noteId?.toIntOrNull()?.let {
                            detailViewmodel.deleteNote(it)
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                } else {
                    Spacer(modifier = Modifier.width(8.dp))
                    FloatingActionButton(onClick = {
                        title = ""
                        description = ""
                        navController.popBackStack()
                    }) {
                        Text("Discard")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            BasicTextField(
                value = title,
                onValueChange = { title = it },
                textStyle = TextStyle(color = Color.Black, fontSize = 24.sp),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    if (title.isEmpty()) {
                        Text(text = "Title", color = Color.Gray, fontSize = 24.sp)
                    }
                    innerTextField()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            BasicTextField(
                value = description,
                onValueChange = { description = it },
                textStyle = TextStyle(color = Color.Black, fontSize = 18.sp),
                modifier = Modifier.fillMaxSize(),
                decorationBox = { innerTextField ->
                    if (description.isEmpty()) {
                        Text(text = "Type here...", color = Color.Gray, fontSize = 18.sp)
                    }
                    innerTextField()
                }
            )
        }
    }
}
