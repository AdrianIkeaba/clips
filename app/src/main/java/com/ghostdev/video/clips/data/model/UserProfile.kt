package com.ghostdev.video.clips.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val display_name: String,
    val created_at: String,
    val display_photo: String? = null
)