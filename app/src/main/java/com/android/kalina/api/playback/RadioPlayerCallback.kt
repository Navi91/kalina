package com.android.kalina.api.playback

import android.media.AudioTrack
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.android.kalina.api.icy.PlayerCallback

/**
 * Created by Dmitriy on 24.02.2018.
 */
abstract class RadioPlayerCallback : PlayerCallback, Player.EventListener {
    override fun playerStarted() {
    }

    override fun playerPCMFeedBuffer(isPlaying: Boolean, audioBufferSizeMs: Int, audioBufferCapacityMs: Int) {
    }

    override fun playerStopped(perf: Int) {
    }

    override fun playerException(t: Throwable?) {
    }

    abstract override fun playerMetadata(key: String?, value: String?)

    override fun playerAudioTrackCreated(audioTrack: AudioTrack?) {
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
    }

    override fun onSeekProcessed() {
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
    }

    abstract override fun onPlayerError(error: ExoPlaybackException?)

    override fun onLoadingChanged(isLoading: Boolean) {
    }

    override fun onPositionDiscontinuity(reason: Int) {
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
    }

    override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
    }

    abstract override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int)
}