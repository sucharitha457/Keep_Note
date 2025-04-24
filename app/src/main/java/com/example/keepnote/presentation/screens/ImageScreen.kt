package com.example.keepnote.presentation.screens

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import androidx.core.net.toUri
import kotlinx.coroutines.launch


private const val TAG = "ImageScreen"
@Composable
fun ImageScreen(images: List<String>, navController: NavHostController, startIndex: Int = 0) {
    val pagerState = rememberPagerState(
        initialPage = startIndex,
        pageCount = { images.size }
    )
    var isImageScaled by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var zoomOut by remember { mutableStateOf(false) }

    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    var scale by remember { mutableStateOf(1f) }
    val scaleMin = 1f
    val scaleMax = 5f
    val state = rememberTransformableState { zoomChange, _, _ ->
        // Apply zoom change, but ensure zoom does not go below 1
        scale *= zoomChange
        if (scale < scaleMin) {
            scale = scaleMin
        }
        isImageScaled = scale > 1f
        zoomOut = scale == 1f || scale < 1f
        Log.d("TAG", "ImageScreen: isImageScaled:$isImageScaled | scale:$scale | zoomOut:$zoomOut")
    }

    var zoom by remember { mutableStateOf(1f) }
    val imagemodifier = Modifier
        .fillMaxSize()
//        .graphicsLa

//                    .pointerInput(Unit) {
//                        detectHorizontalDragGestures { _, dragAmount ->
//                            scope.launch {
//                                Log.d("TAG", "detectHorizontalDragGestures: $zoomOut")
//                                if (zoomOut) {
//                                    if (dragAmount > 0) {
////                                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
////                                    } else {
////                                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
//                                    }
//                                }
//                            }
//                        }
//                    }

//        .pointerInput(Unit) {
//            detectTransformGestures(
//                onGesture = { _, pan, gestureZoom, _ ->
//                    // Apply zoom limit
//                    zoom = zoom.coerceIn(scaleMin, scaleMax)
//
//                    if (zoom > 1f) {
//                        offsetX += pan.x * 6
//                        offsetY += pan.y * 6
//
//                    } else {
//                        // Reset offset when zoom is at minimum (1f)
//                        offsetX = 1f
//                        offsetY = 1f
//                    }
//                }
//            )
//        }
//        .transformable(state = state)


    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = " ${pagerState.currentPage + 1}/${images.size}",
                modifier = Modifier.padding(start = 12.dp)
            )
        }

        HorizontalPager(
//            userScrollEnabled = zoomOut,
            state = pagerState,
            pageSize = PageSize.Fill,
            modifier = Modifier
                .fillMaxSize()
//                .pointerInput(Unit) {
//                    detectTransformGestures { _, pan, gestureZoom, _ ->
//
//                        Log.d(TAG, "detectHorizontalDragGestures: $pan")
////                        if (zoomOut) {
////                            scope.launch {
////                                if (dragAmount > 0) {
////                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
////                                } else {
////                                    pagerState.animateScrollToPage(pagerState.currentPage - 1)
////                                }
////                            }
////                        }
//                    }
//                }
        ) { page ->
            val uri = images[page].toUri()
            Image(
                painter = rememberAsyncImagePainter(uri),
                contentDescription = null,
                modifier = imagemodifier
            )
        }
    }
}

