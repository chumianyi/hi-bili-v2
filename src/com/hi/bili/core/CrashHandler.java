package com.hi.bili.core;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;

import com.hi.bili.ui.CrashActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 全局崩溃捕获：写入日志文件并启动 CrashActivity 展示日志。
 */
public class CrashHandler implements Thread.UncaughtExceptionHandler {

    private static CrashHandler instance;
    private final Context ctx;
    private final Thread.UncaughtExceptionHandler defaultHandler;

    private CrashHandler(Context ctx) {
        this.ctx = ctx.getApplicationContext();
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    public static void init(Context ctx) {
        if (instance == null) {
            instance = new CrashHandler(ctx);
            Thread.setDefaultUncaughtExceptionHandler(instance);
        }
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        String log = buildLog(t, e);
        writeLog(log);
        try {
            Intent intent = new Intent(ctx, CrashActivity.class);
            intent.putExtra("crash_log", log);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            ctx.startActivity(intent);
        } catch (Exception ignore) {
        }
        // 结束当前进程
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }

    private String buildLog(Thread t, Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println("Time: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        pw.println("Device: " + Build.MANUFACTURER + " " + Build.MODEL);
        pw.println("Android: " + Build.VERSION.RELEASE + (Build.VERSION.SDK_INT));
        pw.println("Thread: " + t.getName());
        pw.println("----------------------------------------");
        e.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    private void writeLog(String log) {
        FileOutputStream fos = null;
        try {
            File dir = new File(ctx.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "crash");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, "crash_" + System.currentTimeMillis() + ".log");
            fos = new FileOutputStream(file);
            fos.write(log.getBytes("UTF-8"));
        } catch (Exception ignore) {
        } finally {
            if (fos != null) {
                try { fos.close(); } catch (Exception ignore) {}
            }
        }
    }
}
