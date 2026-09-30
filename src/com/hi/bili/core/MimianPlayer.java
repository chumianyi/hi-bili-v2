package com.hi.bili.core;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.HashMap;
import java.util.Map;

/**
 * 自研播放器：基于 MediaPlayer + SurfaceView 封装。
 * 通过 setDataSource(Context, Uri, Map headers) 携带 Referer 请求头。
 */
public class MimianPlayer {

    public interface OnPreparedListener {
        void onPrepared();
    }

    public interface OnErrorListener {
        void onError(int what, int extra);
    }

    public interface OnCompletionListener {
        void onCompletion();
    }

    private final Context ctx;
    private final SurfaceView surface;
    private MediaPlayer player;
    private String videoPath;
    private String referer = "https://www.bilibili.com";

    private OnPreparedListener preparedListener;
    private OnErrorListener errorListener;
    private OnCompletionListener completionListener;

    public MimianPlayer(Context ctx, SurfaceView surface) {
        this.ctx = ctx;
        this.surface = surface;
    }

    public void setVideoPath(String url) {
        this.videoPath = url;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    /** 准备播放（同步设置，异步 prepareAsync 完成后回调） */
    public void prepare() {
        release();
        try {
            player = new MediaPlayer();
            SurfaceHolder holder = surface.getHolder();
            player.setDisplay(holder);

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("Referer", referer);
            headers.put("User-Agent", HttpUtil.DEFAULT_UA);
            player.setDataSource(ctx, Uri.parse(videoPath), headers);

            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    if (preparedListener != null) preparedListener.onPrepared();
                }
            });
            player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    if (errorListener != null) errorListener.onError(what, extra);
                    return true;
                }
            });
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    if (completionListener != null) completionListener.onCompletion();
                }
            });
            player.prepareAsync();
        } catch (Exception e) {
            if (errorListener != null) errorListener.onError(0, 0);
        }
    }

    public void start() {
        try {
            if (player != null && !player.isPlaying()) {
                player.start();
            }
        } catch (Exception ignore) {
        }
    }

    public void pause() {
        try {
            if (player != null && player.isPlaying()) {
                player.pause();
            }
        } catch (Exception ignore) {
        }
    }

    public void stop() {
        try {
            if (player != null) {
                player.stop();
            }
        } catch (Exception ignore) {
        }
    }

    public void release() {
        try {
            if (player != null) {
                player.reset();
                player.release();
                player = null;
            }
        } catch (Exception ignore) {
        }
    }

    public boolean isPlaying() {
        try {
            return player != null && player.isPlaying();
        } catch (Exception e) {
            return false;
        }
    }

    public int getCurrentPosition() {
        try {
            return player != null ? player.getCurrentPosition() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public int getDuration() {
        try {
            return player != null ? player.getDuration() : 0;
        } catch (Exception e) {
            return 0;
        }
    }

    public void seekTo(int msec) {
        try {
            if (player != null) player.seekTo(msec);
        } catch (Exception ignore) {
        }
    }

    public void setOnPreparedListener(OnPreparedListener l) {
        this.preparedListener = l;
    }

    public void setOnErrorListener(OnErrorListener l) {
        this.errorListener = l;
    }

    public void setOnCompletionListener(OnCompletionListener l) {
        this.completionListener = l;
    }
}
