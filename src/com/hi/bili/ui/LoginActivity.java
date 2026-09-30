package com.hi.bili.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import com.hi.bili.R;
import com.hi.bili.core.BiliApi;
import com.hi.bili.core.ImageLoader;

import org.json.JSONObject;

import java.net.URLEncoder;

/**
 * 扫码登录：申请二维码 -> 轮询状态 -> 成功后返回。
 */
public class LoginActivity extends BaseActivity {

    private ImageView mQrView;
    private TextView mStatus;
    private Handler mHandler = new Handler();
    private String mQrcodeKey;
    private boolean mRunning = true;

    private Runnable mPollTask = new Runnable() {
        @Override
        public void run() {
            if (!mRunning) return;
            try {
                String json = BiliApi.pollQrcode(mQrcodeKey);
                JSONObject root = new JSONObject(json);
                int code = root.optInt("code", -1);
                if (code == 0) {
                    // 登录成功，刷新用户信息
                    try {
                        BiliApi.getNav();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    showToast(getString(R.string.login_success));
                    finish();
                    return;
                } else if (code == 86090) {
                    setStatus(getString(R.string.scan_confirmed));
                } else if (code == 86038) {
                    setStatus(getString(R.string.scan_expired));
                    generateQr();
                    return;
                } else {
                    setStatus(getString(R.string.scan_waiting));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            mHandler.postDelayed(this, 2000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mQrView = (ImageView) findViewById(R.id.iv_qrcode);
        mStatus = (TextView) findViewById(R.id.tv_qrcode_status);
        generateQr();
    }

    private void generateQr() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String json = BiliApi.generateQrcode();
                    JSONObject root = new JSONObject(json);
                    JSONObject data = root.optJSONObject("data");
                    final String url = data == null ? "" : data.optString("url", "");
                    mQrcodeKey = data == null ? "" : data.optString("qrcode_key", "");
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (url.length() > 0) {
                                // 用在线二维码服务渲染为图片(无第三方库)
                                String qr = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data="
                                        + URLEncoder.encode(url);
                                ImageLoader.display(mQrView, qr);
                                setStatus(getString(R.string.scan_waiting));
                                startPoll();
                            } else {
                                setStatus(getString(R.string.login_failed));
                            }
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            setStatus(getString(R.string.login_failed));
                        }
                    });
                }
            }
        }).start();
    }

    private void startPoll() {
        mHandler.removeCallbacks(mPollTask);
        mHandler.postDelayed(mPollTask, 2000);
    }

    private void setStatus(final String s) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mStatus.setText(s);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mRunning = false;
        mHandler.removeCallbacksAndMessages(null);
    }
}
