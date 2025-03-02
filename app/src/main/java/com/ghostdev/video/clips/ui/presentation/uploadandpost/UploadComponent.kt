package com.ghostdev.video.clips.ui.presentation.uploadandpost

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.ghostdev.video.clips.R
import com.google.common.util.concurrent.ListenableFuture
import java.io.File

private var activeRecording: Recording? = null

@Composable
fun UploadComponent(
    modifier: Modifier = Modifier,
    navigateToHome: () -> Unit,
    navigateToReview: (String) -> Unit
) {

    UploadScreen(
        modifier = modifier,
        navigateBack = navigateToHome,
        navigateToReview = navigateToReview
    )
}

@Composable
fun UploadScreen(
    modifier: Modifier,
    navigateBack: () -> Unit,
    navigateToReview: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    rememberCoroutineScope()

    var rotationAngle by remember { mutableFloatStateOf(0f) }
    val animatedRotation: Float by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
        label = "Rotate Camera Switch"
    )

    // Camera states
    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashEnabled by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }

    // CameraX objects
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val videoCapture = remember { mutableStateOf<VideoCapture<Recorder>?>(null) }


    // Camera launcher for permissions
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val allGranted = permissions.entries.all { it.value }
            if (!allGranted) {
                Toast.makeText(context, "Camera permission is required!", Toast.LENGTH_SHORT).show()
            }
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                val videoPath = getFilePathFromUri(context, it)
                if (videoPath?.endsWith(".mp4") == true) {
                    navigateToReview(videoPath)
                } else {
                    Toast.makeText(context, "Please select a valid .mp4 video", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    // Request camera & storage permissions
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        CameraPreview(
            modifier = Modifier.fillMaxSize(),
            lensFacing = lensFacing,
            flashEnabled = flashEnabled,
            videoCapture = videoCapture,
            cameraProviderFuture = cameraProviderFuture,
            lifecycleOwner = lifecycleOwner
        )

        // Top Controls
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.Gray.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                IconButton(onClick = navigateBack) {
                    Icon(
                        painter = painterResource(R.drawable.close),
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
            Column {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.Gray.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = {
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK)
                            CameraSelector.LENS_FACING_FRONT else CameraSelector.LENS_FACING_BACK

                        rotationAngle += 360f
                    }) {
                        Icon(
                            painter = painterResource(R.drawable.camera_switch),
                            contentDescription = "Switch Camera",
                            tint = Color.White,
                            modifier = Modifier.rotate(animatedRotation)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.Gray.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = { flashEnabled = !flashEnabled }) {
                        Icon(
                            painter = painterResource(R.drawable.flash),
                            contentDescription = "Flash Toggle",
                            tint = if (flashEnabled) Color.Yellow else Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.Gray.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = { galleryLauncher.launch("video/*") },
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.gallery),
                            contentDescription = "Select from Gallery",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Bottom Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .wrapContentSize()
                        .background(Color(0xFF07A7F2).copy(alpha = 0.7f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Video",
                        color = Color.White,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Shutter Button
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            Color.Red,
                            if (isRecording) RoundedCornerShape(12.dp) else CircleShape
                        )
                        .border(
                            4.dp,
                            Color.White,
                            if (isRecording) RoundedCornerShape(12.dp) else CircleShape
                        )
                        .clickable {
                            if (isRecording) {
                                activeRecording?.stop()
                                activeRecording = null
                                isRecording = false
                            } else {
                                startVideoRecording(
                                    context,
                                    videoCapture.value,
                                    navigateToReview
                                )
                                isRecording = true
                            }
                        }
                )
            }
        }
    }
}

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    lensFacing: Int,
    flashEnabled: Boolean,
    videoCapture: MutableState<VideoCapture<Recorder>?>,
    cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
    lifecycleOwner: LifecycleOwner
) {
    val context = LocalContext.current
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(lensFacing, flashEnabled) {
        val cameraProvider = cameraProviderFuture.get()
        val cameraSelector = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        val preview = Preview.Builder()
            .setTargetRotation(Surface.ROTATION_0)
            .build()
            .also {
                it.surfaceProvider = previewView.surfaceProvider
            }


        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
            .build()
        val videoCaptureConfig = VideoCapture.withOutput(recorder)

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner, cameraSelector, preview, videoCaptureConfig
            )

            videoCapture.value = videoCaptureConfig

        } catch (exc: Exception) {
            Log.e("CameraPreview", "Camera binding failed", exc)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { previewView }
    )
}


// Function to record videos
private fun startVideoRecording(context: Context, videoCapture: VideoCapture<Recorder>?, navigateToReview: (String) -> Unit) {
    val file = File(getOutputDirectory(context), "${System.currentTimeMillis()}.mp4")
    val outputOptions = FileOutputOptions.Builder(file).build()

    if (activeRecording != null) {
        activeRecording?.stop()
        activeRecording = null
        return
    }

    val recording = videoCapture?.output
        ?.prepareRecording(context, outputOptions)
        ?.apply {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            withAudioEnabled()
        }
        ?.start(ContextCompat.getMainExecutor(context)) { event ->
            if (event is VideoRecordEvent.Finalize) {
                Toast.makeText(context, "Video Saved: ${file.absolutePath}", Toast.LENGTH_SHORT).show()
                navigateToReview(file.absolutePath)
            }
        }

    activeRecording = recording
}


// Function to get output directory
private fun getOutputDirectory(context: Context): File {
    val mediaDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return mediaDir ?: context.filesDir
}

fun getFilePathFromUri(context: Context, uri: Uri): String? {
    val contentResolver = context.contentResolver
    val fileExtension = getFileExtension(context, uri)

    // Ensure the file has a valid .mp4 extension
    if (fileExtension?.lowercase() != "mp4") {
        return null
    }

    // Create a file in your app's cache directory
    val file = File(context.cacheDir, "selected_video_${System.currentTimeMillis()}.mp4")

    try {
        // Open an input stream from the Uri and copy the file to your app's cache directory
        contentResolver.openInputStream(uri)?.use { inputStream ->
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return file.absolutePath
    } catch (e: Exception) {
        Log.e("getFilePathFromUri", "Error copying file from Uri", e)
        return null
    }
}

// Helper function to get the file extension from the Uri
private fun getFileExtension(context: Context, uri: Uri): String? {
    val mimeType = context.contentResolver.getType(uri)
    return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
}