package com.ghostdev.video.clips.ui.presentation.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.ghostdev.video.clips.R
import com.ghostdev.video.clips.data.model.Video
import com.ghostdev.video.clips.ui.components.LoadingScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileComponent(
    modifier: Modifier = Modifier,
    navigateToHome: (Boolean) -> Unit = {},
    navigateToVideoPreview: (String) -> Unit = {}, // New navigation function
    navigateToUpload: () -> Unit = {},
    isDarkTheme: Boolean,
    viewModel: ProfileLogic = koinViewModel()
) {
    val state = viewModel.profileUiState.collectAsStateWithLifecycle()

    if (state.value.loading) {
        LoadingScreen()
    } else {
        ProfileScreen(
            modifier = modifier,
            isDarkTheme = isDarkTheme,
            navigateBack = {
                navigateToHome(false)
            },
            navigateToVideoPreview = navigateToVideoPreview,
            navigateToUpload = navigateToUpload,
            profileImage = state.value.photoUrl,
            displayName = state.value.displayName,
            uploadedVideos = state.value.uploadedVideos,
            onDeleteVideo = { videoId ->
                viewModel.deleteVideo(videoId)
            },
            loadingVideos = state.value.loadingVideos,
            getUserFavorites = {
                viewModel.getUserFavorites()
            },
            favoriteVideos = state.value.favoriteVideos,
            onUnFavoriteVideo = { videoId ->
                viewModel.unFavoriteVideo(videoId)
            },
            getUploadedVideos = {
                viewModel.getUploadedVideos()
            }
        )
    }
}

@Composable
private fun ProfileScreen(
    modifier: Modifier,
    isDarkTheme: Boolean,
    navigateBack: () -> Unit,
    navigateToVideoPreview: (String) -> Unit = {},
    navigateToUpload: () -> Unit,
    profileImage: String = "",
    displayName: String = "",
    uploadedVideos: List<Video> = emptyList(),
    onDeleteVideo: (String) -> Unit = {},
    loadingVideos: Boolean,
    getUserFavorites: () -> Unit,
    favoriteVideos: List<Video>,
    onUnFavoriteVideo: (String) -> Unit = {},
    getUploadedVideos: () -> Unit = {}
) {
    var selectedItem by remember { mutableStateOf("My posts") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        ProfileHeader(navigateBack, isDarkTheme)

        Spacer(modifier = Modifier.height(24.dp))

        ProfileDetails(isDarkTheme, profileImage, displayName)

        Spacer(modifier = Modifier.height(24.dp))

        ProfileTabs(
            selectedItem = selectedItem,
            onTabSelected = {
                selectedItem = it
                when (it) {
                    "Favourites" -> getUserFavorites()
                    "My posts" -> getUploadedVideos()
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        ProfileItems(
            uploadedVideos = uploadedVideos,
            loadingVideos = loadingVideos,
            onDeleteVideo = onDeleteVideo,
            selectedItem = selectedItem,
            favoriteVideos = favoriteVideos,
            onUnFavoriteVideo = {
                onUnFavoriteVideo(it)
            },
            onVideoClick = { videoId ->
                navigateToVideoPreview(videoId)
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        ExtendedFloatingActionButton(
            onClick = { navigateToUpload() },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 48.dp, end = 24.dp),
            containerColor = Color(0xFF07A7F2)
        ) {
            Icon(
                painter = painterResource(R.drawable.add),
                contentDescription = "Post",
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))

            Text(text = "Upload", fontWeight = FontWeight.SemiBold)
        }
    }
}


@Composable
private fun ProfileHeader(
    navigateBack: () -> Unit,
    isDarkTheme: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { navigateBack() }) {
            Icon(
                painter = painterResource(R.drawable.arrow_back),
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = "Your profile",
            color = if (isDarkTheme) Color.White else Color.Black,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ProfileDetails(
    isDarkTheme: Boolean,
    profileImage: String = "",
    displayName: String = ""
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        if (profileImage != "") {
            AsyncImage(
                model = profileImage,
                contentDescription = "User profile",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Fit
            )
        } else {
            Image(
                painter = painterResource(R.drawable.default_profile),
                contentDescription = "User profile",
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray),
                contentScale = ContentScale.Fit
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = displayName,
            color = if (isDarkTheme) Color.White else Color.Black,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}


@Composable
private fun ProfileTabs(
    selectedItem: String,
    onTabSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        listOf("My posts", "Favourites").forEach { title ->
            TabItem(
                title = title,
                isSelected = selectedItem == title,
                onClick = { onTabSelected(title) }
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
    }
}

@Composable
private fun TabItem(
    title: String,
    isSelected: Boolean,
    onClick: (String) -> Unit
) {
    Text(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFF07A7F2) else Color.Transparent)
            .clickable { onClick(title) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        text = title,
        fontSize = 18.sp,
        color = Color.White
    )
}


@Composable
fun ProfileItems(
    selectedItem: String,
    uploadedVideos: List<Video> = emptyList(),
    favoriteVideos: List<Video> = emptyList(),
    loadingVideos: Boolean = false,
    onDeleteVideo: (String) -> Unit = {},
    onUnFavoriteVideo: (String) -> Unit = {},
    onVideoClick: (String) -> Unit = {} // New parameter for video click
) {
    val videosToDisplay = when (selectedItem) {
        "My posts" -> uploadedVideos
        "Favourites" -> favoriteVideos
        else -> emptyList()
    }

    when {
        loadingVideos -> {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
        videosToDisplay.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No videos found...",
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        }
        else -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp, bottom = 72.dp), // Add bottom padding for FAB
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(videosToDisplay.size) { index ->
                    VideosItem(
                        selectedItem = selectedItem,
                        thumbnailUrl = videosToDisplay[index].thumbnail_url,
                        onDeleteVideo = {
                            if (selectedItem == "My posts") {
                                onDeleteVideo(videosToDisplay[index].id)
                            } else if (selectedItem == "Favourites") {
                                onUnFavoriteVideo(videosToDisplay[index].id)
                            }
                        },
                        onVideoClick = {
                            onVideoClick(videosToDisplay[index].id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun VideosItem(
    selectedItem: String,
    thumbnailUrl: String,
    modifier: Modifier = Modifier,
    onDeleteVideo: () -> Unit = {},
    onVideoClick: () -> Unit = {} // New parameter for video click
) {
    val thumbnailUri by remember { mutableStateOf<String?>(thumbnailUrl) }
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .height(250.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onVideoClick() } // Click to view the video
    ) {
        AsyncImage(
            model = thumbnailUri,
            contentDescription = "Video thumbnail",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(6.dp)
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                .padding(horizontal = 5.dp, vertical = 3.dp)
                .clickable { showDialog = true }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedItem == "Favourites") "Unfavorite" else "Delete",
                    fontSize = 12.sp,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    painter = painterResource(if (selectedItem == "Favourites") R.drawable.star else R.drawable.delete),
                    contentDescription = if (selectedItem == "Favourites") "Unfavorite video" else "Delete video",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Play icon overlay in the center
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.play),
                contentDescription = "Play video",
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    .padding(12.dp)
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = if (selectedItem == "Favourites") "Unfavorite Video" else "Delete Video") },
            text = { Text(text = if (selectedItem == "Favourites") "Are you sure you want to remove this video from favorites?" else "Are you sure you want to delete this video?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteVideo()
                        showDialog = false
                    }
                ) {
                    Text(
                        if (selectedItem == "Favourites") "Unfavorite" else "Delete",
                        color = if (selectedItem == "Favourites") Color.Yellow else Color.Red
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}