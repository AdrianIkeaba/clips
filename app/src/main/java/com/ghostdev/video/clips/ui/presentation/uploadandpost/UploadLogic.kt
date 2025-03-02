package com.ghostdev.video.clips.ui.presentation.uploadandpost

import android.content.Context
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import android.net.Uri
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ghostdev.video.clips.data.model.Video
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import java.io.File
import com.ghostdev.video.clips.data.network.SupabaseClient

class UploadLogic: ViewModel(), KoinComponent {
    private val _uploadUiState = MutableStateFlow(UploadUiState())
    val uploadUiState: MutableStateFlow<UploadUiState> = _uploadUiState

    fun generateThumbnail(
        context: Context,
        videoPath: String,
    ) {
        viewModelScope.launch {
            try {
                val file = File(videoPath)

                if (!file.exists()) {
                    _uploadUiState.value = uploadUiState.value.copy(
                        error = "Video file not found: $videoPath",
                        errorType = ErrorType.THUMBNAIL_GENERATION
                    )
                    return@launch
                }

                val thumbnail = ThumbnailUtils.createVideoThumbnail(
                    file.absolutePath,
                    MediaStore.Images.Thumbnails.MINI_KIND
                )

                thumbnail?.let {
                    val uri = saveBitmapToCache(context, it)
                    _uploadUiState.value = uploadUiState.value.copy(thumbnailUri = uri.toString())
                } ?: run {
                    _uploadUiState.value = uploadUiState.value.copy(
                        error = "Failed to generate thumbnail",
                        errorType = ErrorType.THUMBNAIL_GENERATION
                    )
                }
            } catch (e: Exception) {
                _uploadUiState.value = uploadUiState.value.copy(
                    error = "Error generating thumbnail: ${e.localizedMessage}",
                    errorType = ErrorType.THUMBNAIL_GENERATION
                )
            }
        }
    }

    fun uploadVideo(
        context: Context,
        videoUrl: String,
        title: String,
        description: String,
    ) {
        if (title.isBlank()) {
            _uploadUiState.value = uploadUiState.value.copy(
                error = "Title cannot be empty",
                errorType = ErrorType.VALIDATION
            )
            return
        }

        if (uploadUiState.value.thumbnailUri.isEmpty()) {
            _uploadUiState.value = uploadUiState.value.copy(
                error = "Thumbnail not generated. Please try again.",
                errorType = ErrorType.THUMBNAIL_MISSING
            )
            return
        }

        _uploadUiState.value = uploadUiState.value.copy(
            isLoading = true,
            error = null,
            errorType = null
        )

        viewModelScope.launch {
            try {
                val videoFile = File(videoUrl)
                if (!videoFile.exists()) {
                    _uploadUiState.value = uploadUiState.value.copy(
                        isLoading = false,
                        error = "Video file not found",
                        errorType = ErrorType.UPLOAD
                    )
                    return@launch
                }

                val thumbnailUri = Uri.parse(uploadUiState.value.thumbnailUri)
                val thumbnailFile = File(thumbnailUri.path!!)
                if (!thumbnailFile.exists()) {
                    _uploadUiState.value = uploadUiState.value.copy(
                        isLoading = false,
                        error = "Thumbnail file not found",
                        errorType = ErrorType.UPLOAD
                    )
                    return@launch
                }

                val result = SupabaseClient.uploadVideo(
                    videoFile = videoFile.readBytes(),
                    thumbnailFile = thumbnailFile.readBytes(),
                    title = title,
                    description = description
                )

                result.fold(
                    onSuccess = { video ->
                        _uploadUiState.value = uploadUiState.value.copy(
                            isLoading = false,
                            isUploaded = true,
                            video = video
                        )
                    },
                    onFailure = { exception ->
                        _uploadUiState.value = uploadUiState.value.copy(
                            isLoading = false,
                            error = "Upload failed: ${exception.localizedMessage}",
                            errorType = ErrorType.UPLOAD
                        )
                        println(exception.localizedMessage)
                    }
                )
            } catch (e: Exception) {
                _uploadUiState.value = uploadUiState.value.copy(
                    isLoading = false,
                    error = "Unexpected error: ${e.localizedMessage}",
                    errorType = ErrorType.UNKNOWN
                )
            }
        }
    }

    fun clearError() {
        _uploadUiState.value = uploadUiState.value.copy(error = null, errorType = null)
    }

    fun resetUploadState() {
        _uploadUiState.value = UploadUiState()
    }
}

fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri {
    val file = File(context.cacheDir, "video_thumbnail_${System.currentTimeMillis()}.jpg")
    file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
    return Uri.fromFile(file)
}

enum class ErrorType {
    VALIDATION,
    THUMBNAIL_GENERATION,
    THUMBNAIL_MISSING,
    UPLOAD,
    UNKNOWN
}

data class UploadUiState(
    val isLoading: Boolean = false,
    val isUploaded: Boolean = false,
    val thumbnailUri: String = "",
    val error: String? = null,
    val errorType: ErrorType? = null,
    val video: Video? = null
)