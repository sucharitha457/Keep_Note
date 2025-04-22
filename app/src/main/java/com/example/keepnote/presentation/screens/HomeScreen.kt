package com.example.keepnote.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.keepnote.data.local.NoteEntity
import com.example.keepnote.data.remote.dto.NoteDto
import com.example.keepnote.domain.model.noteData
import com.example.keepnote.presentation.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(viewModel: NoteViewModel = hiltViewModel(),navController: NavController) {
    val notes by viewModel.notes.collectAsState()
    val loading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    if (loading) {
        CircularProgressIndicator()
    }

    error?.let {
        Text(text = "Error: $it")
    }
    Box {
        displayNotes(notes,navController)
        FloatingActionButton(
            onClick = {
                navController.navigate(navitem.Note.route)
            },
            modifier = Modifier
                .padding(40.dp)
                .align(androidx.compose.ui.Alignment.BottomEnd)
        ) {
            Text(
                text = "+",
                fontSize = 30.sp,
                color = Color.White,
                modifier = Modifier.padding(6.dp)
            )
        }
    }
}

@Composable
fun displayNotes(notes: List<NoteEntity>,navController: NavController) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        verticalItemSpacing = 8.dp,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(notes.size) { index ->
            noteCard(notes[index],navController = navController)
        }
    }
}

@Composable
fun noteCard(note : NoteEntity,navController: NavController){
    Card (
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .clickable {
                navController.navigate(navitem.Note.withNoteId(note.id.toString()))
            },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Text(note.title,modifier = Modifier.padding(8.dp), fontSize = 30.sp)
            Text(text = convertUnixToDateTime( note.created_time))
        }
    }
}


fun convertUnixToDateTime(unixTime: Long): String {
    val date = Date(unixTime * 1000)
    val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return format.format(date)
}