package com.hi.bili.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.hi.bili.R;
import com.hi.bili.core.HistoryDB;
import com.hi.bili.core.ImageLoader;
import com.hi.bili.core.Prefs;

/**
 * 设置：播放方式 / 主题 / 清缓存 / 清历史 / 检查更新 / 关于。
 */
public class SettingsActivity extends BaseActivity {

    private RadioGroup rgPlayer, rgTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        rgPlayer = (RadioGroup) findViewById(R.id.rg_player);
        rgTheme = (RadioGroup) findViewById(R.id.rg_theme);

        // 回显当前设置
        String playerType = Prefs.getPlayerType();
        rgPlayer.check("external".equals(playerType) ? R.id.rb_external : R.id.rb_internal);

        String themeMode = Prefs.getThemeMode();
        if ("dark".equals(themeMode)) rgTheme.check(R.id.rb_dark);
        else if ("auto".equals(themeMode)) rgTheme.check(R.id.rb_auto);
        else rgTheme.check(R.id.rb_light);

        rgPlayer.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Prefs.setPlayerType(checkedId == R.id.rb_external ? "external" : "internal");
            }
        });

        rgTheme.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                String mode = "light";
                if (checkedId == R.id.rb_dark) mode = "dark";
                else if (checkedId == R.id.rb_auto) mode = "auto";
                Prefs.setThemeMode(mode);
                Toast.makeText(SettingsActivity.this, R.string.theme_reboot_tip, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.row_clear_cache).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageLoader.clearCache();
                Toast.makeText(SettingsActivity.this, R.string.cache_cleared, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.row_clear_history).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HistoryDB.clearAll();
                Toast.makeText(SettingsActivity.this, R.string.history_cleared, Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.row_check_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkUpdate();
            }
        });

        findViewById(R.id.row_about).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new android.app.AlertDialog.Builder(SettingsActivity.this)
                        .setTitle(R.string.settings_about)
                        .setMessage(R.string.about_desc)
                        .setPositiveButton("确定", null)
                        .show();
            }
        });
    }

    private void checkUpdate() {
        showToast("正在检查更新...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    com.hi.bili.core.UpdateChecker.UpdateInfo info =
                            com.hi.bili.core.UpdateChecker.check("");
                    if (info != null && info.versionCode > 1) {
                        final String url = info.downloadUrl;
                        final String verName = info.versionName;
                        final String log = info.changelog;
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                new android.app.AlertDialog.Builder(SettingsActivity.this)
                                        .setTitle("发现新版本 " + verName)
                                        .setMessage(log)
                                        .setPositiveButton("下载", new android.content.DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(android.content.DialogInterface d, int w) {
                                                com.hi.bili.core.UpdateChecker.downloadAndInstall(SettingsActivity.this, url);
                                            }
                                        })
                                        .setNegativeButton("取消", null)
                                        .show();
                            }
                        });
                    } else {
                        showToast(getString(R.string.no_update));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showToast(getString(R.string.no_update));
                }
            }
        }).start();
    }
}
