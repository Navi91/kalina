package com.android.kalina.api.icy;

import android.net.Uri;
import android.util.Log;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Dmitriy on 23.02.2018.
 */

public class IcyDataSource implements DataSource {
    private final static String TAG = IcyDataSource.class.getSimpleName();
    private final PlayerCallback playerCallback;

    private HttpURLConnection connection;
    private InputStream inputStream;
    private boolean metadataEnabled = true;

    public IcyDataSource(final PlayerCallback playerCallback) {
        this.playerCallback = playerCallback;
    }

    @Override
    public long open(final DataSpec dataSpec) throws IOException {
        Log.i(TAG, "open[" + dataSpec.position + "-" + dataSpec.length);
        URL url = new URL(dataSpec.uri.toString());
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestProperty("Icy-Metadata", "1");

        try {
            inputStream = getInputStream(connection);
        } catch (Exception e) {
            closeConnectionQuietly();
            throw new IOException(e.getMessage());
        }

        return dataSpec.length;
    }

    @Override
    public int read(final byte[] buffer, final int offset, final int readLength) throws
            IOException {
        return inputStream.read(buffer, offset, readLength);
    }

    @Override
    public Uri getUri() {
        return connection == null ? null : Uri.parse(connection.getURL().toString());
    }

    @Override
    public void close() throws IOException {
        try {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    throw new IOException(e.getMessage());
                }
            }
        } finally {
            inputStream = null;
            closeConnectionQuietly();
        }
    }

    /**
     * Gets the input stream from the connection.
     * Actually returns the underlying stream or IcyInputStream.
     */
    protected InputStream getInputStream(HttpURLConnection conn) throws Exception {
        String smetaint = conn.getHeaderField("icy-metaint");
        InputStream ret = conn.getInputStream();

        if (!metadataEnabled) {
            Log.i(TAG, "Metadata not enabled");
        } else if (smetaint != null) {
            int period = -1;
            try {
                period = Integer.parseInt(smetaint);
            } catch (Exception e) {
                Log.e(TAG, "The icy-metaint '" + smetaint + "' cannot be parsed: '" + e);
            }

            if (period > 0) {
                Log.i(TAG, "The dynamic metainfo is sent every " + period + " bytes");

                ret = new IcyInputStream(ret, period, playerCallback, null);
            }
        } else Log.i(TAG, "This stream does not provide dynamic metainfo");

        return ret;
    }

    /**
     * Closes the current connection quietly, if there is one.
     */
    private void closeConnectionQuietly() {
        if (connection != null) {
            try {
                connection.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error while disconnecting", e);
            }
            connection = null;
        }
    }

}
