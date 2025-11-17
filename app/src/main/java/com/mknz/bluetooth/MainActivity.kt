package com.mknz.bluetooth

import android.content.ComponentName
import android.graphics.BitmapFactory
import android.media.MediaMetadata
import android.media.browse.MediaBrowser
import android.media.session.MediaController
import android.media.session.PlaybackState
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.mknz.bluetooth.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    companion object {
        private const val BT_BROWSED_PACKAGE = "com.android.bluetooth"
        private const val BT_BROWSED_SERVICE =
            "com.android.bluetooth.avrcpcontroller.BluetoothMediaBrowserService"
    }

    private lateinit var binding: ActivityMainBinding

    private var browser: MediaBrowser? = null

    private var controller: MediaController? = null

    private var duration = 0L

    private val browserCallback = object : MediaBrowser.ConnectionCallback() {
        override fun onConnected() {
            if (browser == null) {
                return
            }
            controller = MediaController(this@MainActivity, browser!!.sessionToken)
            controller!!.registerCallback(controllerCallback)
            if (controller!!.playbackState?.state == PlaybackState.STATE_PLAYING) {
                binding.actionButton.setIconResource(R.drawable.pause)
            }
            if (controller!!.metadata != null) {
                updateMetadata(controller!!.metadata!!)
            }
            if (controller!!.playbackState != null) {
                updatePlaybackState(controller!!.playbackState!!)
            }
        }

        override fun onConnectionFailed() {
            reconnect()
        }

        override fun onConnectionSuspended() {
            reconnect()
        }
    }

    private val controllerCallback = object : MediaController.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadata?) {
            if (metadata != null) {
                updateMetadata(metadata)
            }
        }

        override fun onPlaybackStateChanged(state: PlaybackState?) {
            if (state != null) {
                updatePlaybackState(state)
            }
        }

        override fun onSessionDestroyed() {
            reconnect()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.title.isSelected = true
        binding.artist.isSelected = true

        binding.nextButton.setOnClickListener {
            controller?.transportControls?.skipToNext()
        }

        binding.previousButton.setOnClickListener {
            controller?.transportControls?.skipToPrevious()
        }

        binding.actionButton.setOnClickListener {
            controller?.let {
                if (it.playbackState?.state == PlaybackState.STATE_PLAYING) {
                    it.transportControls.pause()
                } else {
                    it.transportControls.play()
                }
            }
        }

        reconnect()
    }

    fun reconnect() {
        controller?.unregisterCallback(controllerCallback)
        browser?.disconnect()
        browser = MediaBrowser(
            this,
            ComponentName(BT_BROWSED_PACKAGE, BT_BROWSED_SERVICE),
            browserCallback,
            null
        )
        browser?.connect()
    }

    fun updateMetadata(metadata: MediaMetadata) {
        duration = metadata.getLong(MediaMetadata.METADATA_KEY_DURATION)

        val title = metadata.getString(MediaMetadata.METADATA_KEY_TITLE)
        if (title == null || title == "") {
            binding.title.setText(R.string.unknown_title)
        } else {
            binding.title.text = title
        }

        val artist = metadata.getString(MediaMetadata.METADATA_KEY_ARTIST)
        if (artist == null || artist == "") {
            binding.artist.setText(R.string.unknown_artist)
        } else {
            binding.artist.text = artist
        }

        val albumArt = metadata.getString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI)
        if (albumArt == null) {
            binding.art.setImageResource(R.drawable.music_note_inset)
        } else {
            val art = contentResolver.openInputStream(albumArt.toUri())
            if (art != null) {
                val bitmap = BitmapFactory.decodeStream(art)
                binding.art.setImageBitmap(bitmap)
                art.close()
            }
        }
    }

    fun updatePlaybackState(playbackState: PlaybackState) {
        if (playbackState.state == PlaybackState.STATE_PLAYING) {
            binding.actionButton.setIconResource(R.drawable.pause)
        } else if (playbackState.state != PlaybackState.STATE_PLAYING) {
            binding.actionButton.setIconResource(R.drawable.play)
        }
        binding.time.text =
            "${getFormattedTime(playbackState.position, duration)} / ${getFormattedTime(duration, duration)}"
    }

    private fun getFormattedTime(value: Long, total: Long): String {
        val totalHours = (((total / 1000) / 60) / 60) % 60
        val seconds = (value / 1000) % 60
        val minutes = ((value / 1000) / 60) % 60
        val hours = (((value / 1000) / 60) / 60) % 60
        val str = "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
        if (totalHours > 0) {
            return "${hours.toString().padStart(2, '0')}:$str"
        }
        return str
    }
}
