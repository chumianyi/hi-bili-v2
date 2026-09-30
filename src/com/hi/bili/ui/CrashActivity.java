package com.hi.bili.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.hi.bili.R;

/**
 * 崩溃显示页：读取崩溃日志并提供重启入口。
 */
public class CrashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);

        TextView logView = (TextView) findViewById(R.id.tv_crash_log);
        String log = getIntent().getStringExtra("crash_log");
        if (log == null || log.length() == 0) {
            log = "无堆栈信息。";
        }
        logView.setText(log);

        findViewById(R.id.btn_restart).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(CrashActivity.this, MainActivity.class);
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(it);
                finish();
            }
        });
    }
}
