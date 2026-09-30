package com.hi.bili.core;

import android.app.Application;
import android.os.Build;

import com.hi.bili.R;

/**
 * Application：初始化全局组件，并按系统版本选择主题。
 */
public class HibiApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Prefs.init(this);
        HistoryDB.init(this);
        ImageLoader.init(this);
        CrashHandler.init(this);

        // API >= 28 使用 M3 主题，否则 M2
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            setTheme(R.style.AppTheme_M3);
        } else {
            setTheme(R.style.AppTheme);
        }
        // API >= 31 (Android 12+) 系统会在 M3 下自动尝试动态取色（Material You）
    }
}
