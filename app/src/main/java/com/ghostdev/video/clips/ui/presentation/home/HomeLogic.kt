package com.ghostdev.video.clips.ui.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostdev.video.clips.data.model.VideoReel
import com.ghostdev.video.clips.data.network.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class HomeLogic: ViewModel(), KoinComponent {
    private val _homeUiState = MutableStateFlow(HomeUiState())
    val homeUiState: MutableStateFlow<HomeUiState> = _homeUiState

    fun loadVideos() {
        viewModelScope.launch {
            _homeUiState.value = _homeUiState.value.copy(loading = true)

            // Don't include user's own videos in the general feed
            val result = SupabaseClient.getVideoReels(includeUserVideos = false)

            result.onSuccess { videos ->
                _homeUiState.value = HomeUiState(loading = false, videos = videos)
            }.onFailure {
                _homeUiState.value = _homeUiState.value.copy(loading = false)
            }
        }
    }

    fun loadSingleVideo(videoId: String) {
        viewModelScope.launch {
            _homeUiState.update { it.copy(loading = true) }
            try {
                val video = SupabaseClient.getVideoById(videoId)
                video.onSuccess { videoReel ->
                    _homeUiState.update {
                        it.copy(videos = listOf(videoReel), loading = false)
                    }
                }.onFailure {
                    _homeUiState.update { it.copy(loading = false, videos = emptyList()) }
                }
            } catch (e: Exception) {
                _homeUiState.update { it.copy(loading = false, videos = emptyList()) }
            }
        }
    }

    fun toggleFavorite(videoId: String) {
        viewModelScope.launch {
            val originalState = _homeUiState.value.videos.find { it.id == videoId }?.isFavorited ?: false

            _homeUiState.value = _homeUiState.value.copy(
                videos = _homeUiState.value.videos.map { video ->
                    if (video.id == videoId) video.copy(isFavorited = !video.isFavorited) else video
                }
            )

            try {
                if (originalState) {
                    SupabaseClient.deleteFavorite(videoId)
                } else {
                    SupabaseClient.setFavorite(videoId)
                }
            } catch (e: Exception) {
                _homeUiState.value = _homeUiState.value.copy(
                    videos = _homeUiState.value.videos.map { video ->
                        if (video.id == videoId) video.copy(isFavorited = originalState) else video
                    }
                )
            }
        }
    }
}


data class HomeUiState(
    val loading: Boolean = false,
    val videos: List<VideoReel> = emptyList()
)