package com.android.kalina.api.icy;

import android.content.Context;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.TransferListener;

/**
 * Created by Dmitriy on 23.02.2018.
 */

public final class IcyDataSourceFactory implements DataSource.Factory {

    /* Main class variables */
    private final Context context;
    private final TransferListener<? super DataSource> listener;
    private final DataSource.Factory baseDataSourceFactory;
    private boolean enableShoutcast = false;
    private PlayerCallback playerCallback;


    /* Constructor */
    public IcyDataSourceFactory(Context context,
                                String userAgent,
                                boolean enableShoutcast,
                                PlayerCallback playerCallback) {
        // use next Constructor
        this(context,
                userAgent,
                null,
                enableShoutcast,
                playerCallback);
    }


    /* Constructor */
    public IcyDataSourceFactory(Context context,
                                String userAgent,
                                TransferListener<? super DataSource> listener,
                                boolean enableShoutcast,
                                PlayerCallback playerCallback) {
        // use next Constructor
        this(context,
                listener,
                new DefaultHttpDataSourceFactory(userAgent, listener),
                enableShoutcast,
                playerCallback);
    }


    /* Constructor */
    public IcyDataSourceFactory(Context context,
                                TransferListener<? super DataSource> listener,
                                DataSource.Factory baseDataSourceFactory,
                                boolean enableShoutcast,
                                PlayerCallback playerCallback) {
        this.context = context.getApplicationContext();
        this.listener = listener;
        this.baseDataSourceFactory = baseDataSourceFactory;
        this.enableShoutcast = enableShoutcast;
        this.playerCallback = playerCallback;
    }


    @Override
    public DataSource createDataSource() {
        // toggle Shoutcast extraction
        if (enableShoutcast) {
            return new IcyDataSource(playerCallback);
        } else {
            return new DefaultDataSource(context, listener, baseDataSourceFactory.createDataSource());
        }
    }

}
