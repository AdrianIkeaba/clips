package com.ghostdev.video.clips.ui.presentation.uploadandpost

import android.net.Uri
import android.widget.VideoView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.rememberAsyncImagePainter
import com.ghostdev.video.clips.R
import org.koin.androidx.compose.koinViewModel

@Composable
fun DescriptionComponent(
    modifier: Modifier = Modifier,
    mediaPath: String,
    navigateToUploadScreen: () -> Unit,
    navigateToFeed: () -> Unit,
    viewModel: UploadLogic = koinViewModel()
) {
    val state by viewModel.uploadUiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(mediaPath) {
        viewModel.generateThumbnail(context, mediaPath)
    }

    LaunchedEffect(state.isUploaded) {
        if (state.isUploaded) {
            navigateToFeed()
        }
    }

    DescriptionScreen(
        modifier = modifier,
        mediaPath = mediaPath,
        navigateToUploadScreen = navigateToUploadScreen,
        isLoading = state.isLoading,
        error = state.error,
        uploadVideo = { title, caption ->
            viewModel.uploadVideo(
                context = context,
                videoUrl = mediaPath,
                title = title,
                description = caption
            )
        },
        clearError = viewModel::clearError
    )
}

@Composable
fun DescriptionScreen(
    modifier: Modifier = Modifier,
    mediaPath: String,
    navigateToUploadScreen: () -> Unit,
    isLoading: Boolean = false,
    error: String? = null,
    uploadVideo: (title: String, caption: String) -> Unit = { _, _ -> },
    clearError: () -> Unit = {}
) {
    val context = LocalContext.current
    val isVideo = mediaPath.endsWith(".mp4")
    val mediaUri = Uri.parse(mediaPath)
    var title by remember { mutableStateOf("") }
    var caption by remember { mutableStateOf("") }

    // Show error dialog if there's an error
    if (error != null) {
        AlertDialog(
            onDismissRequest = clearError,
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                Button(
                    onClick = clearError,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF07A7F2))
                ) {
                    Text("OK")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
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

        // Back Button
        IconButton(
            onClick = navigateToUploadScreen,
            modifier = modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_back),
                contentDescription = "Back",
                tint = Color.White
            )
        }

        // Title, Caption Input and Post Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title TextField
            TextField(
                value = title,
                onValueChange = { title = it },
                textStyle = TextStyle(color = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color.Black
                ),
                placeholder = {
                    Text(
                        text = "Add a title...",
                        color = Color.Gray
                    )
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Caption TextField
            TextField(
                value = caption,
                onValueChange = { caption = it },
                textStyle = TextStyle(color = Color.Black),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color.Black
                ),
                placeholder = {
                    Text(
                        text = "Add a caption...",
                        color = Color.Gray
                    )
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { uploadVideo(title, caption) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF07A7F2)
                ),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Uploading...",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                } else {
                    Icon(
                        painter = painterResource(id = R.drawable.post),
                        contentDescription = "Post",
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Post it!",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}