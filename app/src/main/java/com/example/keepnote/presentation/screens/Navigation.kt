package com.example.keepnote.presentation.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument


@Composable
fun Navigation(){
    val navController = rememberNavController()

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        NavHost(
            navController,
            startDestination = NavItem.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(NavItem.Home.route) {
                HomeScreen(navController = navController)
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
                    navController = navController,
                    noteId = noteId
                )
            }
            composable(
                route = "image?imageList={imageList}&startIndex={startIndex}",
                arguments = listOf(
                    navArgument("imageList") {
                        nullable = true
                        defaultValue = null
                    },
                    navArgument("startIndex") {
                        type = NavType.IntType
                        defaultValue = 0
                    }
                ),
            ) {
                val encodedList = it.arguments?.getString("imageList")
                val imageList = encodedList?.split(",") ?: emptyList()
                val startIndex = it.arguments?.getInt("startIndex") ?: 0

                ImageScreen(
                    images = imageList,
                    startIndex = startIndex,
                    navController = navController
                )
            }

        }
    }
}

sealed class NavItem(val route: String) {
    object Home : NavItem("home")
    object Note : NavItem("note?noteId={noteId}") {
        fun withNoteId(noteId: String?) = "note?noteId=$noteId"
    }
}
