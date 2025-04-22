package com.example.keepnote.presentation.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument


@Composable
fun navigation(modifier: Modifier){
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        NavHost(
            navController, startDestination = navitem.Home.route) {
            composable(navitem.Home.route) {
                HomeScreen(modifier = modifier.padding(innerPadding), navController = navController)
            }
            composable(
                route = "note?noteId={noteId}",
                arguments = listOf(
                    navArgument("noteId") {
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val noteId = backStackEntry.arguments?.getString("noteId")
                DetailScreen(
                    modifier = modifier.padding(innerPadding),
                    navController = navController,
                    noteId = noteId
                )
            }
        }
    }

}
sealed class navitem(val route: String) {
    object Home : navitem("home")
    object Note : navitem("note?noteId={noteId}") {
        fun withNoteId(noteId: String?) = "note?noteId=$noteId"
    }
}