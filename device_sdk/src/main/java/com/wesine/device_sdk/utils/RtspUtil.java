package com.wesine.device_sdk.utils;

import android.net.Uri;

import org.videolan.libvlc.LibVLC;
import org.videolan.libvlc.Media;
import org.videolan.libvlc.MediaPlayCallback;
import org.videolan.libvlc.MediaPlayer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

/**
 * Created by doug on 18-2-27.
 */

public class RtspUtil {
    private MediaPlayer mMediaPlayer;

    private LibVLC mVlc;

    private static RtspUtil sInstance = new RtspUtil();

    private ByteBuffer mByteBuffer;

    public static RtspUtil getInstance() {
        return sInstance;
    }

    public interface RtspCallback {
        void onPreviewFrame(ByteBuffer buffer, int width, int height);
    }

    private RtspUtil() {

    }

    public void createPlayer(String url, final int width, final int height, final RtspCallback callback) {
        releasePlayer();

        mByteBuffer = ByteBuffer.allocateDirect(width * height * 4)
                .order(ByteOrder.nativeOrder());

        try {
            ArrayList<String> options = new ArrayList<String>();
            options.add("--aout=opensles");
            options.add("--audio-time-stretch"); // time stretching
            options.add("-vvv"); // verbosity
            mVlc = new LibVLC(Device.getApp(), options);

            // Create media player
            mMediaPlayer = new MediaPlayer(mVlc);
            mMediaPlayer.setVideoFormat("RGBA", width, height, width * 4);
            mMediaPlayer.setVideoCallback(mByteBuffer, new MediaPlayCallback() {
                @Override
                public void onDisplay(final ByteBuffer byteBuffer) {
                    callback.onPreviewFrame(byteBuffer, width, height);
                }
            });

            Media m = new Media(mVlc, Uri.parse(url));
            int cache = 600;//1500
            m.addOption(":network-caching=" + cache);
            m.addOption(":file-caching=" + cache);
            m.addOption(":live-cacheing=" + cache);
            m.addOption(":sout-mux-caching=" + cache);
            m.addOption(":codec=mediacodec,iomx,all");
            mMediaPlayer.setMedia(m);
            mMediaPlayer.play();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void releasePlayer() {
        if (mVlc == null) {
            return;
        }

        mMediaPlayer.setVideoCallback(null, null);
        mMediaPlayer.stop();

        mVlc.release();
        mVlc = null;
    }
}
