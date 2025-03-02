package com.ghostdev.video.clips.data.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Like(
    val id: String = UUID.randomUUID().toString(),
    val user_id: String,
    val video_id: String,
    val created_at: String = System.currentTimeMillis().toString()
)