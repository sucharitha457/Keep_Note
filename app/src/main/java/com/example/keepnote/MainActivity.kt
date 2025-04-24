package com.example.keepnote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.keepnote.presentation.screens.Navigation
import com.example.keepnote.presentation.viewmodel.NoteViewModel
import com.example.keepnote.ui.theme.KeepNoteTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContent {
            KeepNoteTheme {
                val noteViewModel:NoteViewModel = hiltViewModel()
                LaunchedEffect(key1 = true){
                    noteViewModel.refreshFromApi()
                }
                Navigation()
            }
        }
    }
}
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    KeepNoteTheme {
        Greeting("Android")
    }
}