package com.example.keepnote.presentation.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.keepnote.data.local.NoteEntity
import com.example.keepnote.presentation.viewmodel.NoteViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun HomeScreen(viewModel: NoteViewModel = hiltViewModel(),navController: NavController) {
    val notes by viewModel.getNotes().collectAsState(initial = emptyList())
    val loading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    if (loading) {
        CircularProgressIndicator()
    }

    LaunchedEffect (key1 = true){
        Log.d("HomeScreen", "LaunchedEffect triggered $notes")
    }

    error?.let {
        Text(text = "Error: $it")
    }
    Box {
        displayNotes(notes,navController)
        FloatingActionButton(
            onClick = { navController.navigate(navItem.Note.route) },
            modifier = Modifier
                .padding(24.dp)
                .align(androidx.compose.ui.Alignment.BottomEnd),
            containerColor = Color.Black
        ) {
            Text("+", fontSize = 30.sp, color = Color.White)
        }
    }
}

@Composable
fun displayNotes(notes: List<NoteEntity>, navController: NavController) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier.fillMaxSize().padding(8.dp),
        verticalItemSpacing = 8.dp,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        var leftHeight = 0
        var rightHeight = 0

        items(notes.size) { index ->
            val note = notes[index]
            val fullWidth = (leftHeight == rightHeight)

            noteCard(
                note = note,
                navController = navController,
                fullWidth = fullWidth
            )

            val randomHeight = note.title.length % 3 + 1
            if (fullWidth) {
                leftHeight += randomHeight
                rightHeight += randomHeight
            } else if (leftHeight <= rightHeight) {
                leftHeight += randomHeight
            } else {
                rightHeight += randomHeight
            }
        }
    }
}

@Composable
fun noteCard(note: NoteEntity, navController: NavController, fullWidth: Boolean) {
    val noteColors = listOf(
        Color(0xFFFFF176),
        Color(0xFF81C784),
        Color(0xFF64B5F6),
        Color(0xFFBA68C8),
        Color(0xFFFF8A65),
        Color(0xFFFFA726),
        Color(0xFFA1887F),
        Color(0xFF4DD0E1),
    )
    val backgroundColor = noteColors[note.id % noteColors.size]

    Card(
        modifier = Modifier
            .then(
                if (fullWidth) Modifier.fillMaxWidth()
                else Modifier.fillMaxWidth(0.5f)
            )
            .wrapContentHeight()
            .clickable {
                navController.navigate(navItem.Note.withNoteId(note.id.toString()))
            },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = note.title,
                fontSize = 18.sp,
                color = Color.Black
            )
            Text(
                text = convertUnixToDate(note.createdTime),
                fontSize = 14.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

fun convertUnixToDate(unixTime: Long): String {
    Log.d("HomeScreen", "convertUnixToDate: $unixTime")
    val date = Date(unixTime * 1000)
    val format = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return format.format(date)
}
