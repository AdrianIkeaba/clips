package com.ghostdev.video.clips.ui.presentation.uploadandpost

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil3.compose.rememberAsyncImagePainter

@Composable
fun ReviewComponent(
    modifier: Modifier = Modifier,
    mediaPath: String,
    navigateToUploadScreen: () -> Unit,
    navigateToDescriptionScreen: () -> Unit
) {
    ReviewScreen(
        modifier = modifier,
        mediaPath = mediaPath,
        navigateToUploadScreen = navigateToUploadScreen,
        navigateToDescriptionScreen = navigateToDescriptionScreen
    )
}

@Composable
private fun ReviewScreen(
    modifier: Modifier = Modifier,
    mediaPath: String,
    navigateToUploadScreen: () -> Unit,
    navigateToDescriptionScreen: () -> Unit
) {
    val isVideo = mediaPath.endsWith(".mp4")
    val mediaUri = Uri.parse(mediaPath)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        if (isVideo) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { context ->
                    VideoView(context).apply {
                        setVideoURI(mediaUri)
                        setOnPreparedListener { mediaPlayer ->
                            mediaPlayer.isLooping = true
                            start()
                        }
                    }
                }
            )
        } else {
            Image(
                painter = rememberAsyncImagePainter(mediaPath),
                contentDescription = "Captured Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = navigateToUploadScreen,
                    modifier = Modifier
                        .weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDE3030).copy(0.8f)
                    )
                ) {
                    Text(
                        modifier = Modifier
                            .padding(4.dp),
                        text = "Try Again",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = {
                        navigateToDescriptionScreen()
                    },
                    modifier = Modifier
                        .weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF07A7F2).copy(0.8f)
                    )
                ) {
                    Text(
                        modifier = Modifier
                            .padding(4.dp),
                        text = "Use",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}
