package com.ghostdev.video.clips.data.network

import com.ghostdev.video.clips.BuildConfig
import com.ghostdev.video.clips.data.model.Favorite
import com.ghostdev.video.clips.data.model.UserProfile
import com.ghostdev.video.clips.data.model.Video
import com.ghostdev.video.clips.data.model.VideoReel
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Count
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.Storage
import io.github.jan.supabase.storage.storage
import java.util.UUID

object SupabaseClient {
    val client = createSupabaseClient(
        /*SUPABASE URL AND KEY CAN BE GOTTEN PRIVATELY IN SUPABASE.
        AND STORED IN YOUR '.local.properties' file.
        FOR TESTING, IT IS KEPT HERE
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_KEY
         */
        supabaseUrl = "https://vpfyxdyljvlcccahfujk.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InZwZnl4ZHlsanZsY2NjYWhmdWprIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NDA2NTE4NDAsImV4cCI6MjA1NjIyNzg0MH0.kJyYu9Z4KyJLEW4r0zva6V_3FTdYAEOJU7hiQB7bbEI"
    ) {
        install(Auth)
        install(Postgrest)
        install(Storage)
    }

    suspend fun setFavorite(videoID: String) {
        try {
            val userID = client.auth.currentUserOrNull()?.id
                ?: return

            client.postgrest["favorites"].upsert(
                mapOf(
                    "video_id" to videoID,
                    "user_id" to userID
                )
            )
        } catch (e: Exception) {
            println("Error updating favorite: ${e.message}")
        }
    }

    suspend fun deleteFavorite(videoId: String) {
        try {
            val userID = client.auth.currentUserOrNull()?.id
                ?: return

            client.postgrest["favorites"].delete {
                filter {
                    eq("video_id", videoId)
                    eq("user_id", userID)
                }
            }
        } catch (e: Exception) {
            println("Error updating favorite: ${e.message}")
        }
    }


    suspend fun getUserProfile(): Result<Pair<String, String>> {
        return try {
            val userID = client.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("User not authenticated"))

            val profile = client.postgrest["profiles"]
                .select {
                    filter { eq("id", userID) }
                    limit(1)
                }
                .decodeSingle<UserProfile>()


            val displayName = profile.display_name
            val displayPhoto = profile.display_photo ?: ""

            Result.success(Pair(displayName, displayPhoto))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    suspend fun uploadVideo(
        videoFile: ByteArray,
        thumbnailFile: ByteArray,
        title: String,
        description: String? = null
    ): Result<Video> {
        return try {
            // Get current user
            val userID = client.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("User not authenticated"))

            // Upload video file
            val videoID = UUID.randomUUID().toString()
            val videoPath = "$userID/$videoID.mp4"
            client.storage.from("videos").upload(videoPath, videoFile)

            // Upload thumbnail
            val thumbnailPath = "thumbnails/$userID/$videoID.jpg"
            client.storage.from("thumbnails").upload(thumbnailPath, thumbnailFile)

            // Get the public URLs
            val videoUrl = client.storage.from("videos").publicUrl(videoPath)
            val thumbnailUrl = client.storage.from("thumbnails").publicUrl(thumbnailPath)

            // Create video record in database
            val video = Video(
                id = videoID,
                user_id = userID,
                title = title,
                description = description,
                video_url = videoUrl,
                thumbnail_url = thumbnailUrl,
            )

            client.postgrest.from("videos").insert(video)

            Result.success(video)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Modify the SupabaseClient's getVideoById function to never filter out videos based on user ID
    suspend fun getVideoById(videoId: String): Result<VideoReel> {
        return try {
            val userID = client.auth.currentUserOrNull()?.id

            // Get the requested video
            val video = client.postgrest["videos"]
                .select {
                    filter { eq("id", videoId) }
                    limit(1)
                }
                .decodeSingleOrNull<Video>() ?: return Result.failure(Exception("Video not found"))

            // Get the profile of the video creator
            val profile = client.postgrest["profiles"]
                .select {
                    filter { eq("id", video.user_id) }
                    limit(1)
                }
                .decodeSingleOrNull<UserProfile>()

            // Check if the current user has favorited this video
            val isFavorited = userID?.let { hasUserFavoritedVideo(video.id) } ?: false

            // Create a VideoReel object with all the information
            val videoReel = VideoReel(
                id = video.id,
                user_id = video.user_id,
                title = video.title,
                description = video.description,
                video_url = video.video_url,
                thumbnail_url = video.thumbnail_url,
                isFavorited = isFavorited,
                display_name = profile?.display_name ?: "Unknown",
                display_photo = profile?.display_photo ?: ""
            )

            Result.success(videoReel)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Modify the getVideoReels function to accept a parameter that determines whether to include user's videos
    suspend fun getVideoReels(pageSize: Long = 100, includeUserVideos: Boolean = false): Result<List<VideoReel>> {
        return try {
            val userID = client.auth.currentUserOrNull()?.id

            // Fetch videos along with user profile data using a join query
            val videoResults = client.postgrest["videos"]
                .select {
                    order("created_at", Order.DESCENDING)
                    limit(pageSize)
                }
                .decodeList<Video>()

            // Fetch user profiles separately to avoid potential join limitations
            val userIds = videoResults.map { it.user_id }.distinct()

            val profiles = mutableMapOf<String, UserProfile>()
            for (userId in userIds) {
                try {
                    val profile = client.postgrest["profiles"]
                        .select {
                            filter { eq("id", userId) }
                            limit(1)
                        }
                        .decodeSingleOrNull<UserProfile>()

                    if (profile != null) {
                        profiles[profile.id] = profile
                    }
                } catch (e: Exception) {
                    continue
                }
            }

            // Only filter out user's videos if includeUserVideos is false
            val filteredVideos = if (includeUserVideos) {
                videoResults
            } else {
                videoResults.filter { it.user_id != userID }
            }

            val videoReels = filteredVideos.map { video ->
                val profile = profiles[video.user_id]
                val isFavorited = userID?.let { hasUserFavoritedVideo(video.id) } ?: false

                VideoReel(
                    id = video.id,
                    user_id = video.user_id,
                    title = video.title,
                    description = video.description,
                    video_url = video.video_url,
                    thumbnail_url = video.thumbnail_url,
                    isFavorited = isFavorited,
                    display_name = profile?.display_name ?: "Unknown",
                    display_photo = profile?.display_photo ?: ""
                )
            }.shuffled()

            Result.success(videoReels)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteVideo(videoID: String): Result<Unit> {
        return try {
            val userID = client.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("User not authenticated"))

            // Get the video record
            client.postgrest["videos"]
                .select {
                    filter {
                        eq("id", videoID)
                        eq("user_id", userID)
                    }
                }
                .decodeSingleOrNull<Video>() ?: return Result.failure(Exception("Video not found"))

            // File paths
            val videoPath = "$userID/$videoID.mp4"
            val thumbnailPath = "thumbnails/$userID/$videoID.jpg"

            // Delete from storage (ensure it executes properly)
            client.storage.from("videos").delete(listOf(videoPath))
            client.storage.from("thumbnails").delete(listOf(thumbnailPath))

            // Delete from database
            client.postgrest["videos"].delete {
                filter {
                    eq("id", videoID)
                    eq("user_id", userID)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unfavoriteVideo(videoID: String): Result<Unit> {
        return try {
            val userID = client.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("User not authenticated"))

            client.postgrest["favorites"]
                .delete {
                    filter {
                        eq("video_id", videoID)
                        eq("user_id", userID)
                    }
                }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun hasUserFavoritedVideo(videoID: String): Boolean {
        val userID = client.auth.currentUserOrNull()?.id ?: return false

        val response = client.postgrest["favorites"]
            .select {
                filter {
                    eq("video_id", videoID)
                    eq("user_id", userID)
                }
                count(Count.EXACT)
            }

        val countValue = response.countOrNull() ?: 0
        return countValue > 0
    }

    suspend fun getUserUploadedVideos(): Result<List<Video>> {
        return try {
            val userID = client.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("User not authenticated"))

            val videos = client.postgrest["videos"]
                .select {
                    filter {
                        eq("user_id", userID)
                    }
                    order("created_at", Order.DESCENDING)
                }
                .decodeList<Video>()

            Result.success(videos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserFavoritedVideos(): Result<List<Video>> {
        return try {
            val userID = client.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("User not authenticated"))

            val favorites = client.postgrest["favorites"]
                .select {
                    filter {
                        eq("user_id", userID)
                    }
                }
                .decodeList<Favorite>()

            if (favorites.isEmpty()) {
                return Result.success(emptyList())
            }

            val videoIds = favorites.map { it.video_id }

            val videos = mutableListOf<Video>()

            videoIds.forEach { videoId ->
                val video = client.postgrest["videos"]
                    .select {
                        filter {
                            eq("id", videoId)
                        }
                    }
                    .decodeSingleOrNull<Video>()

                video?.let { videos.add(it) }
            }

            val sortedVideos = videos.sortedByDescending { it.created_at }

            Result.success(sortedVideos)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}