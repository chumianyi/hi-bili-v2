package com.hi.bili.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.hi.bili.R;

/**
 * Activity 基类：负责按系统版本切换 M2 / M3 主题，并提供通用 UI 工具方法。
 * 纯 Java 7 实现。
 */
public abstract class BaseActivity extends Activity {

    protected Handler mHandler;
    private ProgressDialog mLoadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 主题必须在 super.onCreate 之前设置
        if (Build.VERSION.SDK_INT >= 28) {
            // Android 9+ 使用更现代的 M3 色板
            setTheme(R.style.AppTheme_M3);
            // Android 12+ 可进一步尝试系统动态取色(动态主题)，此处保持 M3 色板稳定可用
            if (Build.VERSION.SDK_INT >= 31) {
                try {
                    // 预留：如需 Material You 动态取色，可在此处应用系统动态主题
                } catch (Exception ignored) {
                }
            }
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        mHandler = new Handler(Looper.getMainLooper());
    }

    /** 在主线程弹出 Toast */
    protected void showToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(BaseActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** 显示加载框 */
    protected void showLoading(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mLoadingDialog == null) {
                        mLoadingDialog = new ProgressDialog(BaseActivity.this);
                        mLoadingDialog.setCancelable(false);
                    }
                    mLoadingDialog.setMessage(msg);
                    if (!mLoadingDialog.isShowing()) {
                        mLoadingDialog.show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /** 隐藏加载框 */
    protected void hideLoading() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
                        mLoadingDialog.dismiss();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    protected void restartTo(Class<?> cls) {
        Intent it = new Intent(this, cls);
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(it);
        finish();
    }
}
