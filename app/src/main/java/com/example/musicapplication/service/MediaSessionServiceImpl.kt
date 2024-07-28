package com.example.musicapplication.service

import android.content.Intent
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

class MediaSessionServiceImpl : MediaLibraryService() {
    /* This is the session which will delegate everything you need about audio playback such as notifications, pausing player, resuming player, listening to states, etc */
    private lateinit var mediaLibrarySession: MediaLibrarySession

    /* This is the service side player, the media controller in the activity will control this one, so don't worry about it */
    private lateinit var player: Player

    override fun onCreate() {
        super.onCreate()
        /* Step 1 out of 2: Instantiate the player (ExoPlayer) */
        player = ExoPlayer.Builder(this).build()

        /* Step 2 out of 2: Instantiate the session (most important part) */
        mediaLibrarySession =
            MediaLibrarySession.Builder(this, player, MediaLibrarySessionCallBack()).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaLibrarySession
    }

    // Remember to release the player and media session in onDestroy
    override fun onDestroy() {
        mediaLibrarySession?.run {
            this.player.release()
            this.release()
        }
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaLibrarySession.player
        if (player.playWhenReady) {
            // Make sure the service is not in foreground.
            player.pause()
        }
        stopSelf()
    }

    inner class MediaLibrarySessionCallBack : MediaLibrarySession.Callback {
        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            /* This is the trickiest part, if you don't do this here, nothing will play */
            val updatedMediaItems =
                mediaItems.map { it.buildUpon().setUri(it.mediaId).build() }.toMutableList()
            return Futures.immediateFuture(updatedMediaItems)
        }
    }
}