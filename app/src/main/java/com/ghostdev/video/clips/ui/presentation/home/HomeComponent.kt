package com.ghostdev.video.clips.ui.presentation.home

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.ghostdev.video.clips.R
import com.ghostdev.video.clips.data.model.VideoReel
import com.ghostdev.video.clips.ui.components.VideoPlayer
import org.koin.androidx.compose.koinViewModel

@Composable
fun HomeComponent(
    isPreview: Boolean = false,
    previewVideoId: String? = null,
    modifier: Modifier = Modifier,
    navigateToProfile: () -> Unit = {},
    navigateBack: () -> Unit = {},
    viewmodel: HomeLogic = koinViewModel()
) {
    val state = viewmodel.homeUiState.collectAsStateWithLifecycle()

    // If it's a preview and we have a videoId, load just that video
    LaunchedEffect(isPreview, previewVideoId) {
        if (isPreview && previewVideoId != null) {
            viewmodel.loadSingleVideo(previewVideoId)
        } else {
            viewmodel.loadVideos()
        }
    }

    HomeScreen(
        modifier = modifier,
        navigateToProfile = navigateToProfile,
        loading = state.value.loading,
        videos = state.value.videos,
        reload = {
            if (isPreview && previewVideoId != null) {
                viewmodel.loadSingleVideo(previewVideoId)
            } else {
                viewmodel.loadVideos()
            }
        },
        toggleFavorite = {
            viewmodel.toggleFavorite(it)
        },
        isPreview = isPreview,
        navigateBack = {
            navigateBack()
        }
    )
}

@Composable
private fun HomeScreen(
    modifier: Modifier = Modifier,
    navigateToProfile: () -> Unit,
    loading: Boolean,
    videos: List<VideoReel> = emptyList(),
    reload: () -> Unit,
    toggleFavorite: (String) -> Unit,
    isPreview: Boolean,
    navigateBack: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        ReelsList(
            loading = loading,
            videos = videos,
            toggleFavorite = toggleFavorite
        )

        ReelsHeader(
            modifier = modifier,
            navigateToProfile = navigateToProfile,
            reload = reload,
            isPreview = isPreview,
            navigateBack = navigateBack
        )
    }
}

@Composable
private fun ReelsHeader(
    modifier: Modifier,
    navigateToProfile: () -> Unit,
    reload: () -> Unit,
    isPreview: Boolean,
    navigateBack: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = 12.dp,
                vertical = 12.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (isPreview) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.Gray.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        navigateBack()
                    }
                ) {
                    Icon(
                        modifier = Modifier
                            .size(24.dp),
                        painter = painterResource(R.drawable.arrow_back),
                        tint = Color.White,
                        contentDescription = null
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .background(Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    text = "Clips.",
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 21.sp
                )
            }
        }

        if (!isPreview) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color.Gray.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = {
                        navigateToProfile()
                    }
                ) {
                    Icon(
                        modifier = Modifier
                            .size(24.dp),
                        painter = painterResource(R.drawable.profile),
                        tint = Color.White,
                        contentDescription = null
                    )
                }
            }
        } else {
            Spacer(
                modifier = Modifier
                    .width(8.dp)
            )
        }
    }
}

@Composable
fun ReelsList(
    loading: Boolean = true,
    videos: List<VideoReel>,
    toggleFavorite: (String) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { videos.size })
    var isMuted by remember { mutableStateOf(false) }

    if (loading) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color(0xFFFFFFFF)
            )
        }
    } else {
        if (videos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize()) {
                Text(
                    text = "No clips available yet :(",
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        } else {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                Box(modifier = Modifier.fillMaxSize()) {
                    VideoPlayer(uri = videos[page].video_url, isMuted = isMuted)

                    Column(
                        modifier = Modifier.align(Alignment.BottomStart)
                            .padding(bottom = 32.dp)
                    ) {
                        ReelsFooter(
                            reel = videos[page],
                            toggleAudio = { isMuted = !isMuted },
                            isMuted = isMuted,
                            toggleFavorite = { toggleFavorite(videos[page].id) }
                        )
                    }
                }
            }
        }
    }
}



@Composable
private fun ReelsFooter(
    reel: VideoReel,
    toggleAudio: () -> Unit = {},
    isMuted: Boolean = false,
    toggleFavorite: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        FooterUserData(
            reel = reel,
            modifier = Modifier.weight(8f),
            toggleAudio = toggleAudio,
            isMuted = isMuted
        )

        FooterUserAction(
            reel = reel,
            modifier = Modifier.weight(2f).align(Alignment.Bottom),
            toggleFavorite = toggleFavorite
        )
    }
}


@Composable
private fun FooterUserAction(
    reel: VideoReel,
    modifier: Modifier = Modifier,
    toggleFavorite: () -> Unit
) {
    Column(
        modifier = modifier.fillMaxHeight(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End
    ) {
        UserAction(
            toggleFavorite = toggleFavorite,
            painter = if (reel.isFavorited) R.drawable.starred else R.drawable.star,
            tint = if (reel.isFavorited) Color.Yellow else Color.White
        )

        Spacer(modifier = Modifier.height(100.dp))

        SpinningAsyncImage(model = reel.display_photo)
    }
}


//@Composable
//private fun UserActionWithText(
//    painter: Int,
//    text: String
//) {
//    Icon(
//        modifier = Modifier
//            .size(34.dp),
//        painter = painterResource(painter),
//        tint = Color.White,
//        contentDescription = null
//    )
//
//    Spacer(
//        modifier = Modifier
//            .height(6.dp)
//    )
//
//    Text(
//        text = text,
//        color = Color.White,
//        fontSize = 15.sp,
//        fontWeight = FontWeight.SemiBold
//    )
//
//}

@Composable
private fun UserAction(
    painter: Int,
    tint: Color,
    toggleFavorite: () -> Unit
) {
    Icon(
        modifier = Modifier.size(34.dp).clickable { toggleFavorite() },
        painter = painterResource(painter),
        tint = tint,
        contentDescription = null
    )
}


@Composable
private fun FooterUserData(
    reel: VideoReel,
    modifier: Modifier,
    toggleAudio: () -> Unit = {},
    isMuted: Boolean = false
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (reel.display_photo.isEmpty()) {
                Image(
                    painter = painterResource(R.drawable.default_profile),
                    contentDescription = "user profile",
                    modifier = Modifier
                        .size(34.dp)
                        .background(color = Color.Gray, shape = CircleShape)
                )
            } else {
                AsyncImage(
                    model = reel.display_photo,
                    contentDescription = "user profile",
                    modifier = Modifier
                        .size(34.dp)
                        .background(color = Color.Gray, shape = CircleShape)
                        .clip(CircleShape)
                )
            }

            Spacer(
                modifier = Modifier
                    .width(12.dp)
            )

            Text(
                text = reel.display_name,
                color = Color.White,
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(
                modifier = Modifier
                    .width(12.dp)
            )
        }

        Spacer(
            modifier = Modifier
                .height(12.dp)
        )

        Text(
            text = reel.description ?: "",
            color = Color.White
        )

        Spacer(
            modifier = Modifier
                .height(12.dp)
        )

        // Audio
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = reel.display_name,
                color = Color.White
            )

            Spacer(
                modifier = Modifier
                    .width(12.dp)
            )

            Canvas(
                modifier = Modifier
                    .size(5.dp),
                onDraw = {
                    drawCircle(
                        color = Color.White,
                        radius = 8f
                    )
                }
            )

            Spacer(
                modifier = Modifier
                    .width(12.dp)
            )

            Row(
                modifier = Modifier.clickable { toggleAudio() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Audio", color = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(if (isMuted) R.drawable.audio_not_playing else R.drawable.audio_playing),
                    contentDescription = "Toggle Audio"
                )
            }
        }
    }
}

@Composable
fun SpinningAsyncImage(model: String) {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    if (model.isEmpty()) {
        Image(
            painter = painterResource(R.drawable.default_profile),
            contentDescription = null,
            modifier = Modifier
                .size(28.dp)
                .background(color = Color.Gray, shape = CircleShape)
                .rotate(rotation)
        )
    } else {
        AsyncImage(
            model = model,
            modifier = Modifier
                .size(28.dp)
                .background(color = Color.Gray, shape = CircleShape)
                .clip(CircleShape)
                .rotate(rotation),
            contentDescription = null
        )
    }
}
