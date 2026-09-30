package com.hi.bili.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.hi.bili.R;

/**
 * 开屏页：全屏，居中图标 + 应用名，淡入缩放动画，3 秒后进入主界面。
 */
public class SplashActivity extends Activity {

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.SplashTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView icon = (ImageView) findViewById(R.id.splash_icon);
        TextView text = (TextView) findViewById(R.id.splash_text);

        // 自定义字体
        try {
            android.graphics.Typeface tf = android.graphics.Typeface
                    .createFromAsset(getAssets(), "fonts/Lato-Regular.ttf");
            text.setTypeface(tf);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 淡入 + 缩放动画，持续 1 秒
        AnimationSet set = new AnimationSet(true);
        AlphaAnimation alpha = new AlphaAnimation(0f, 1f);
        ScaleAnimation scale = new ScaleAnimation(0.8f, 1.0f, 0.8f, 1.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        alpha.setDuration(1000);
        scale.setDuration(1000);
        set.addAnimation(alpha);
        set.addAnimation(scale);

        icon.startAnimation(set);
        text.startAnimation(set);

        // 3 秒后跳转
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, 3000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacksAndMessages(null);
    }
}
