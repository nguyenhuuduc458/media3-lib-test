package com.example.musicapplication

import android.content.ComponentName
import android.content.IntentFilter
import android.media.MediaMetadataRetriever
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.musicapplication.receiver.NetworkChangeReceiver
import com.example.musicapplication.service.MediaSessionServiceImpl
import com.example.musicapplication.ui.theme.MusicApplicationTheme
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.launch

/**
 * Best resource implementation for media3
 * https://stackoverflow.com/questions/72560520/how-to-use-media3-with-exoplayer-mediasessionservice
 */
class MainActivity : ComponentActivity() {

    /* This is the global variable of the player
      (which is basically a media controller)
      you're going to use to control playback,
      you're not gonna need anything else other than this,
      which is created from the media controller */
    private lateinit var player: Player

    private lateinit var receiver: NetworkChangeReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url =
            "https://upload.wikimedia.org/wikipedia/commons/e/ed/The_49th_Street_Galleria_-_Chris_Zabriskie.ogg"
        register()
        setContent {
            // Set the content view using Jetpack Compose
            MainScreen {
                MediaControlScreen(
                    onPlay = { onPlaySong(url) },
                    onPause = { onPauseSong() },
                    onSkip = { onSkipSong() }
                )
            }
        }

        lifecycleScope.launch {
            initializeController()
        }
    }

    private fun initializeController() {
        try {
            /* Creating session token (links our UI with service and starts it) */
            val token = SessionToken(
                this@MainActivity,
                ComponentName(this, MediaSessionServiceImpl::class.java)
            )

            /* Instantiating our MediaController and linking it to the service using the session token */
            val controllerFuture = MediaController.Builder(this@MainActivity, token).buildAsync()
            controllerFuture.addListener(
                {
                    player = controllerFuture.get()
                },
                MoreExecutors.directExecutor()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun onPlaySong(uri: String) {
        /* We use setMediaId as a unique identifier for the media (which is needed for mediasession and we do NOT use setUri because we're gonna do
      something like setUri(mediaItem.mediaId) when we need to load the media like we did above in the MusicPlayerService and more precisely when we were building the session */
        val media = MediaItem.Builder()
            .setMediaId(uri) /* setMediaId and NOT setUri */
            .build()
        player.setMediaItem(media)
        player.prepare()
        player.play()
    }

    private fun extractMetadata(url: String): MediaMetadata.Builder? {
        val metadataRetriever = MediaMetadataRetriever()
        try {
            metadataRetriever.setDataSource(url, HashMap<String, String>())

            val title = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val artist =
                metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
            val album = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            val genre = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
            val duration =
                metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                    ?.toLong()

            return MediaMetadata.Builder()
                .setTitle(title)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            metadataRetriever.release()
        }
        return null
    }

    private fun onPauseSong() {
        player.pause()
    }


    private fun onSkipSong() {

    }

    override fun onDestroy() {
        player.release()
        unregister()
        super.onDestroy()
    }

    private fun register() {
        receiver = NetworkChangeReceiver()
        val intentFilter = IntentFilter("android.net.conn.CONNECTIVITY_CHANGE")
        registerReceiver(receiver, intentFilter)
    }

    private fun unregister() {
        unregisterReceiver(receiver)
    }
}

@Composable
fun MainScreen(content: @Composable () -> Unit) {
    MusicApplicationTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            content()
        }
    }
}

@Composable
fun MediaControlScreen(onPlay: () -> Unit, onPause: () -> Unit, onSkip: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        OutlinedButton(onClick = onPlay, modifier = Modifier.padding(8.dp)) {
            Text("Play")
        }
        OutlinedButton(onClick = onPause, modifier = Modifier.padding(8.dp)) {
            Text("Pause")
        }
        OutlinedButton(onClick = onSkip, modifier = Modifier.padding(8.dp)) {
            Text("Skip")
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun PreviewScreen() {
    MainScreen {
        MediaControlScreen(onPlay = { /*TODO*/ }, onPause = { /*TODO*/ }) {

        }
    }
}

