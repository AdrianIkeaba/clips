# Clips

Clips is a video surfing app that allows users to browse, upload, and interact with short-form videos. The app provides a seamless and engaging experience similar to TikTok and YouTube Shorts.

## Features
- **User Authentication**: Sign up and log in using email and password.
- **Video Upload**: Users can upload their own videos to share with others.
- **Video Surfing**: Swipe vertically to browse through videos.
- **Favorite & Unfavorite**: Save videos to a favorites list.
- **Delete Videos**: Users can delete their own uploaded videos.
- **Error Handling**: Smooth error recovery for a seamless user experience.

## Tech Stack
- **Frontend**: Kotlin, Jetpack Compose
- **Backend**: Supabase (Authentication, Database, Storage)
- **State Management**: ViewModel, MutableState
- **Asynchronous Data Handling**: Kotlin Coroutines, Flows.
- **Video Processing**: ExoPlayer for video playback

## Installation
1. Clone the repository:
   ```sh
   https://github.com/AdrianIkeaba/clips
   ```
2. Open the project in Android Studio.
3. Sync the project dependencies.
4. Run the app on an emulator or physical device.

## Setup
- Create a Supabase project and configure authentication, storage, and database. (Default Supabase URL AND KEY already included for testing)
- Add your Supabase URL and API key to the project.
- Ensure necessary permissions for video upload and playback.

## Approach

The development of **Clips** was focused on ensuring a smooth and interactive user experience. The core principles included:
- **Optimized Video Playback**: Efficiently handling video loading and playback to prevent lag.
- **Asynchronous Operations**: Using coroutines and Flow to manage background tasks without UI freezes.
- **Composable UI Components**: Building reusable Jetpack Compose components for scalability.
- **State Management**: Leveraging `ViewModel` and `StateFlow` to manage app state effectively.

## Assumptions

- Users have stable internet access for video streaming and interactions.
- Supabase handles authentication securely and efficiently.
- Video uploads have a reasonable size limit to avoid performance bottlenecks.

## Additional Features Implemented

- **Offline Mode (Planned)**: Caching previously watched videos for offline playback.
- **Comments & Engagement (Planned)**: Users can comment on and interact with videos beyond likes.
- **Custom Animations**: Smooth transitions between videos and UI elements.

## Contribution

1. Fork the repository.
2. Create a new branch for your feature/fix.
3. Commit and push your changes.
4. Open a pull request with a detailed explanation.
