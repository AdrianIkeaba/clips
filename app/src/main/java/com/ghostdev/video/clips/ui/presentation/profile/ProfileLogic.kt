package com.ghostdev.video.clips.ui.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostdev.video.clips.data.model.Video
import com.ghostdev.video.clips.data.network.SupabaseClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class ProfileLogic: ViewModel(), KoinComponent {
    private val _profileUiState = MutableStateFlow(ProfileUiState())
    val profileUiState: MutableStateFlow<ProfileUiState> = _profileUiState

    init {
        getUserProfile()
        getUserVideos()
    }

    private fun getUserProfile() {
        _profileUiState.value = _profileUiState.value.copy(loading = true)

        viewModelScope.launch {
            val result = SupabaseClient.getUserProfile()
            _profileUiState.value = if (result.isSuccess) {
                val (name, photo) = result.getOrDefault(Pair("Unknown", ""))
                _profileUiState.value.copy(
                    loading = false,
                    displayName = name,
                    photoUrl = photo
                )
            } else {
                _profileUiState.value.copy(
                    loading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to load user profile"
                )
            }
        }
    }

    fun getUploadedVideos() {
        _profileUiState.value = _profileUiState.value.copy(loadingVideos = true)

        viewModelScope.launch {
            val result = SupabaseClient.getUserUploadedVideos()
            _profileUiState.value = if (result.isSuccess) {
                _profileUiState.value.copy(
                    loadingVideos = false,
                    uploadedVideos = result.getOrDefault(emptyList())
                )
            } else {
                _profileUiState.value.copy(
                    loadingVideos = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to load user videos"
                )
            }
        }
    }

    private fun getUserVideos() {
        _profileUiState.value = _profileUiState.value.copy(loadingVideos = true)

        viewModelScope.launch {
            val result = SupabaseClient.getUserUploadedVideos()
            _profileUiState.value = if (result.isSuccess) {
                _profileUiState.value.copy(
                    loadingVideos = false,
                    uploadedVideos = result.getOrDefault(emptyList())
                )
            } else {
                _profileUiState.value.copy(
                    loadingVideos = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to load user videos"
                )
            }
        }
    }

    fun deleteVideo(
        videoId: String
    ) {
        _profileUiState.value = _profileUiState.value.copy(loading = true)
        viewModelScope.launch {
            val result = runCatching { SupabaseClient.deleteVideo(videoId) }
            _profileUiState.value = if (result.isSuccess) {
                _profileUiState.value.copy(
                    loading = false,
                    uploadedVideos = _profileUiState.value.uploadedVideos.filter { it.id != videoId }
                )
            } else {
                _profileUiState.value.copy(
                    loading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to delete video"
                )
            }
        }
    }

     fun getUserFavorites() {
         if (_profileUiState.value.favoriteVideos.isEmpty()) {
             _profileUiState.value = _profileUiState.value.copy(loadingVideos = true)

             viewModelScope.launch {
                 val result = SupabaseClient.getUserFavoritedVideos()
                 _profileUiState.value = if (result.isSuccess) {
                     _profileUiState.value.copy(
                         loadingVideos = false,
                         favoriteVideos = result.getOrDefault(emptyList())
                     )
                 } else {
                     _profileUiState.value.copy(
                         loadingVideos = false,
                         errorMessage = result.exceptionOrNull()?.message
                             ?: "Failed to load favorite videos"
                     )
                 }
             }
         }
     }

    fun unFavoriteVideo(
        videoId: String
    ) {
        _profileUiState.value = _profileUiState.value.copy(loading = true)
        viewModelScope.launch {
            val result = runCatching { SupabaseClient.unfavoriteVideo(videoId) }
            _profileUiState.value = if (result.isSuccess) {
                _profileUiState.value.copy(
                    loading = false,
                    favoriteVideos = _profileUiState.value.favoriteVideos.filter { it.id != videoId }
                )
            } else {
                _profileUiState.value.copy(
                    loading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to un-favorite video"
                )
            }
        }
    }
}

data class ProfileUiState(
    val loading: Boolean = false,
    val loadingVideos: Boolean = false,
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val errorMessage: String = "",
    val likedVideos: List<Video> = emptyList(),
    val favoriteVideos: List<Video> = emptyList(),
    val uploadedVideos: List<Video> = emptyList()
)