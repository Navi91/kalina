package com.android.kalina.ui.activity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.android.kalina.R;
import com.android.kalina.api.radio.RadioService;
import com.android.kalina.ui.Studio21Activity;
import com.android.kalina.ui.fragment.RadioFragment;

public class RadioActivity extends Studio21Activity {
    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int BUFFER_SEGMENT_COUNT = 256;

    private static final String OPEN_CHAT_EXTRA = "open_chat_extra";

    private MediaBrowserCompat mediaBrowser;
    private RadioFragment radioFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setContentView(R.layout.a_radio);
        Log.d("trace", "Create");

        radioFragment = (RadioFragment) getSupportFragmentManager().findFragmentById(R.id.radio);

        mediaBrowser = new MediaBrowserCompat(this, new ComponentName(this, RadioService.class), mConnectionCallback, null);

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent.hasExtra(OPEN_CHAT_EXTRA)) {
            startActivity(new Intent(this, ChatActivity.class));
        }
    }

    private void connectToSession(MediaSessionCompat.Token token) throws RemoteException {
        MediaControllerCompat mediaController = new MediaControllerCompat(this, token);
        MediaControllerCompat.setMediaController(this, mediaController);
//        mediaController.registerCallback(mediaControllerCallback);

        if (radioFragment != null) {
            radioFragment.onConnected();
        }

        onMediaControllerConnected();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mediaBrowser.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        MediaControllerCompat controllerCompat = MediaControllerCompat.getMediaController(this);
        if (controllerCompat != null) {
            controllerCompat.unregisterCallback(mediaControllerCallback);
        }
        mediaBrowser.disconnect();
    }

    protected void onMediaControllerConnected() {
        // empty implementation, can be overridden by clients.
    }

    private final MediaControllerCompat.Callback mediaControllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onPlaybackStateChanged(@NonNull PlaybackStateCompat state) {
                }

                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                }
            };

    private final MediaBrowserCompat.ConnectionCallback mConnectionCallback =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    try {
                        connectToSession(mediaBrowser.getSessionToken());
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            };

}
