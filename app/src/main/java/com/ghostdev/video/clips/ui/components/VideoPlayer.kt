package com.ghostdev.video.clips.ui.components

import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.ghostdev.video.clips.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(uri: String, isMuted: Boolean) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val dataSourceFactory = DefaultDataSource.Factory(context)
            val mediaSource: MediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(uri))

            setMediaSource(mediaSource)
            prepare()
            playWhenReady = true
            volume = if (isMuted) 0f else 1f
            videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            repeatMode = Player.REPEAT_MODE_ONE
        }
    }


    DisposableEffect(isMuted) {
        exoPlayer.volume = if (isMuted) 0f else 1f
        onDispose { }
    }

    var showPlayIcon by remember { mutableStateOf(false) }
    var showForwardIcon by remember { mutableStateOf(false) }
    var showRewindIcon by remember { mutableStateOf(false) }

    fun showTempIcon(stateUpdater: (Boolean) -> Unit) {
        stateUpdater(true)
        coroutineScope.launch {
            delay(1000)
            stateUpdater(false)
        }
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .pointerInput(Unit) {
            detectTapGestures(
                onTap = {
                    exoPlayer.playWhenReady = !exoPlayer.playWhenReady
                    showPlayIcon = !exoPlayer.playWhenReady
                },
                onDoubleTap = { offset ->
                    val screenWidth = size.width
                    when {
                        offset.x < screenWidth / 2 -> { // Left Side - Rewind
                            val rewindPosition = (exoPlayer.currentPosition - 5000).coerceAtLeast(0)
                            exoPlayer.seekTo(rewindPosition)
                            showTempIcon { showRewindIcon = it }
                        }

                        else -> { // Right Side - Fast Forward
                            val forwardPosition = (exoPlayer.currentPosition + 5000).coerceAtMost(exoPlayer.duration)
                            exoPlayer.seekTo(forwardPosition)
                            showTempIcon { showForwardIcon = it }
                        }
                    }
                }
            )
        }
    ) {
        AndroidView(
            factory = {
                PlayerView(context).apply {
                    hideController()
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                    player = exoPlayer
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Play Icon (Center)
        AnimatedVisibility(
            visible = showPlayIcon,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Image(
                painter = painterResource(id = R.drawable.play),
                contentDescription = "Play",
                modifier = Modifier.size(80.dp)
            )
        }

        // Rewind Icon (Left Side)
        AnimatedVisibility(
            visible = showRewindIcon,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(horizontal = 24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.backward),
                    contentDescription = "Rewind",
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "5s",
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray
                )
            }
        }

        // Forward Icon (Right Side)
        AnimatedVisibility(
            visible = showForwardIcon,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(horizontal = 24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.forward),
                    contentDescription = "Forward",
                    modifier = Modifier.size(32.dp)
                )
                Text(
                    text = "5s",
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }
}
