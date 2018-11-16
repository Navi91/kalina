package com.android.kalina.api.icy;

import android.media.AudioTrack;

/**
 * Created by Dmitriy on 23.02.2018.
 */

public interface PlayerCallback {

    /**
     * This method is called when the player is started.
     */
    public void playerStarted();


    /**
     * This method is called periodically by PCMFeed.
     *
     * @param isPlaying false means that the PCM data are being buffered,
     *          but the audio is not playing yet
     *
     * @param audioBufferSizeMs the buffered audio data expressed in milliseconds of playing
     * @param audioBufferCapacityMs the total capacity of audio buffer expressed in milliseconds of playing
     */
    public void playerPCMFeedBuffer( boolean isPlaying, int audioBufferSizeMs, int audioBufferCapacityMs );


    /**
     * This method is called when the player is stopped.
     * Note: __after__ this method the method playerException might be also called.
     * @param perf performance indicator - how much is decoder faster than audio player in %;
     *      if less than 0, then decoding of audio is slower than needed by audio player;
     *      example: perf = 53 means that audio decoder is 53% faster than audio player
     *          (production of audio data is 1.53 faster than consuption of audio data)
     */
    public void playerStopped( int perf );


    /**
     * This method is called when an exception is thrown by player.
     */
    public void playerException( Throwable t );


    /**
     * This method is called when the player receives a metadata information.
     * It can be either before starting the player (from HTTP header - all header pairs)
     * or during playback (metadata frame info).
     *
     * The list of available keys is not part of this project -
     * it is depending on the server implementation.
     *
     * @param key the metadata key - e.g. from HTTP header: "icy-genre", "icy-url", "content-type",..
     *      or from the dynamic metadata frame: e.g. "StreamTitle" or "StreamUrl"
     * @param value the metadata value
     */
    public void playerMetadata( String key, String value );


    /**
     * This method is called when the player creates a new AudioTrack object
     * - before any PCM data are fed into it.
     *
     * @param value the metadata value
     */
    public void playerAudioTrackCreated( AudioTrack audioTrack );

}
