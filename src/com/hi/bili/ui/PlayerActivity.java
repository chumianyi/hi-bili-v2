package com.hi.bili.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hi.bili.R;
import com.hi.bili.core.BiliApi;
import com.hi.bili.core.DanmakuView;
import com.hi.bili.core.MimianPlayer;
import com.hi.bili.model.DanmakuItem;
import com.hi.bili.util.JsonUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 播放页：MimianPlayer + SurfaceView + 弹幕叠加 + 自定义控制栏。
 */
public class PlayerActivity extends BaseActivity {

    private String mAid, mCid, mBvid, mTitle;
    private SurfaceView mSurface;
    private DanmakuView mDanmakuView;
    private MimianPlayer mPlayer;
    private SeekBar mSeekBar;
    private TextView mTvCurrent, mTvTotal, mTvDanmaku;
    private ImageView mBtnPlayPause;
    private LinearLayout mTopBar, mControls;
    private ProgressBar mLoading;
    private String mPlayUrl;
    private boolean mDanmakuOn = true;

    private Handler mProgressHandler = new Handler();
    private Runnable mProgressTask = new Runnable() {
        @Override
        public void run() {
            if (mPlayer != null) {
                try {
                    int pos = mPlayer.getCurrentPosition();
                    int dur = mPlayer.getDuration();
                    if (dur > 0) {
                        mSeekBar.setProgress((int) (pos * 1000f / dur));
                        mTvCurrent.setText(formatTime(pos));
                        mTvTotal.setText(formatTime(dur));
                    }
                    if (mDanmakuView != null && mDanmakuOn) {
                        mDanmakuView.setTime(pos / 1000f);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mProgressHandler.postDelayed(this, 500);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.PlayerTheme);
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_player);

        mAid = getIntent().getStringExtra("avid");
        mCid = getIntent().getStringExtra("cid");
        mBvid = getIntent().getStringExtra("bvid");
        mTitle = getIntent().getStringExtra("title");

        mSurface = (SurfaceView) findViewById(R.id.surface_view);
        mDanmakuView = (DanmakuView) findViewById(R.id.danmaku_view);
        mSeekBar = (SeekBar) findViewById(R.id.seek_bar);
        mTvCurrent = (TextView) findViewById(R.id.tv_current_time);
        mTvTotal = (TextView) findViewById(R.id.tv_total_time);
        mTvDanmaku = (TextView) findViewById(R.id.btn_danmaku_toggle);
        mBtnPlayPause = (ImageView) findViewById(R.id.btn_play_pause);
        mTopBar = (LinearLayout) findViewById(R.id.player_top_bar);
        mControls = (LinearLayout) findViewById(R.id.player_controls);
        mLoading = (ProgressBar) findViewById(R.id.player_loading);

        TextView title = (TextView) findViewById(R.id.player_title);
        title.setText(mTitle == null ? "" : mTitle);

        findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mBtnPlayPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePlay();
            }
        });

        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (fromUser && mPlayer != null) {
                    int dur = mPlayer.getDuration();
                    if (dur > 0) mPlayer.seekTo((int) (dur * progress / 1000f));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar sb) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar sb) {
            }
        });

        mTvDanmaku.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDanmakuOn = !mDanmakuOn;
                if (mDanmakuView != null) mDanmakuView.setDanmakuEnabled(mDanmakuOn);
                mTvDanmaku.setText(mDanmakuOn ? R.string.danmaku_on : R.string.danmaku_off);
            }
        });

        findViewById(R.id.btn_external).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playExternal();
            }
        });

        findViewById(R.id.btn_fullscreen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        });

        // 点击屏幕切换控制栏
        mSurface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleControls();
            }
        });

        loadPlayUrl();
        loadDanmaku();
    }

    private void loadPlayUrl() {
        mLoading.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String json = BiliApi.getPlayUrl(mAid, mCid, 32);
                    JSONObject root = new JSONObject(json);
                    JSONObject data = root.optJSONObject("data");
                    JSONArray durl = data == null ? null : data.optJSONArray("durl");
                    if (durl != null && durl.length() > 0) {
                        mPlayUrl = durl.optJSONObject(0).optString("url", "");
                    }
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mLoading.setVisibility(View.GONE);
                            startPlay();
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mLoading.setVisibility(View.GONE);
                            showToast(getString(R.string.play_failed));
                        }
                    });
                }
            }
        }).start();
    }

    private void startPlay() {
        if (mPlayUrl == null || mPlayUrl.length() == 0) {
            showToast(getString(R.string.play_failed));
            return;
        }
        try {
            mPlayer = new MimianPlayer(this, mSurface);
            mPlayer.setReferer("https://www.bilibili.com");
            mPlayer.setVideoPath(mPlayUrl);
            mPlayer.setOnPreparedListener(new MimianPlayer.OnPreparedListener() {
                @Override
                public void onPrepared() {
                    mPlayer.start();
                    mBtnPlayPause.setImageResource(R.drawable.ic_pause);
                    mProgressHandler.post(mProgressTask);
                }
            });
            mPlayer.setOnErrorListener(new MimianPlayer.OnErrorListener() {
                @Override
                public void onError(int what, int extra) {
                    showToast(getString(R.string.play_failed));
                }
            });
            mPlayer.prepare();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadDanmaku() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String xml = BiliApi.getDanmaku(mCid);
                    final ArrayList<DanmakuItem> list = JsonUtil.parseDanmakuXml(xml);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (mDanmakuView != null && list != null) {
                                mDanmakuView.setDanmakuList(list);
                                mDanmakuView.setDanmakuEnabled(mDanmakuOn);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void togglePlay() {
        if (mPlayer == null) return;
        if (mPlayer.isPlaying()) {
            mPlayer.pause();
            mBtnPlayPause.setImageResource(R.drawable.ic_play);
        } else {
            mPlayer.start();
            mBtnPlayPause.setImageResource(R.drawable.ic_pause);
        }
    }

    private void toggleControls() {
        int vis = mTopBar.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
        mTopBar.setVisibility(vis);
        mControls.setVisibility(vis);
    }

    private void playExternal() {
        if (mPlayUrl == null || mPlayUrl.length() == 0) {
            showToast(getString(R.string.play_failed));
            return;
        }
        try {
            Intent it = new Intent(Intent.ACTION_VIEW);
            it.setDataAndType(Uri.parse(mPlayUrl), "video/*");
            it.putExtra("Referer", "https://www.bilibili.com");
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(it);
        } catch (Exception e) {
            e.printStackTrace();
            showToast("没有可用的播放器");
        }
    }

    private String formatTime(int ms) {
        int sec = ms / 1000;
        int h = sec / 3600;
        int m = (sec % 3600) / 60;
        int s = sec % 60;
        if (h > 0) {
            return String.format("%d:%02d:%02d", h, m, s);
        }
        return String.format("%02d:%02d", m, s);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPlayer != null) mPlayer.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPlayer != null) mPlayer.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProgressHandler.removeCallbacks(mProgressTask);
        try {
            if (mPlayer != null) {
                mPlayer.release();
                mPlayer = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mDanmakuView != null) mDanmakuView.release();
    }
}
