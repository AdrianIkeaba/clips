package com.ghostdev.video.clips.data.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class Video(
    val id: String = UUID.randomUUID().toString(),
    val user_id: String,
    val title: String,
    val description: String? = null,
    val video_url: String,
    val thumbnail_url: String,
    val created_at: Instant = Clock.System.now(),
    val updated_at: Instant = Clock.System.now(),
    val view_count: Int = 0,
    val is_published: Boolean = true
)

data class VideoReel(
    val id: String = UUID.randomUUID().toString(),
    val user_id: String,
    val title: String,
    val description: String? = null,
    val video_url: String,
    val thumbnail_url: String,
    val isFavorited: Boolean = false,
    val display_name: String,
    val display_photo: String
)