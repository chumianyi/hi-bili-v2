package com.hi.bili.core;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import org.json.JSONObject;

import java.io.File;

/**
 * 检查更新与下载安装。
 */
public class UpdateChecker {

    public static class UpdateInfo {
        public String versionName;
        public int versionCode;
        public String downloadUrl;
        public String changelog;
    }

    /** 拉取并解析版本 JSON。约定字段：version_name / version_code / download_url / changelog */
    public static UpdateInfo check(String versionJsonUrl) throws Exception {
        String json = HttpUtil.get(versionJsonUrl);
        if (json == null) throw new Exception("empty response");
        JSONObject obj = new JSONObject(json);
        JSONObject data = obj.optJSONObject("data");
        JSONObject src = data != null ? data : obj;

        UpdateInfo info = new UpdateInfo();
        info.versionName = src.optString("version_name", src.optString("name", ""));
        info.versionCode = src.optInt("version_code", src.optInt("code", 0));
        info.downloadUrl = src.optString("download_url", src.optString("url", ""));
        info.changelog = src.optString("changelog", src.optString("notes", ""));
        return info;
    }

    /** 用 DownloadManager 下载 APK 并在完成后触发安装 */
    public static void downloadAndInstall(Context ctx, String url) {
        try {
            File apkFile = new File(ctx.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "update.apk");
            if (apkFile.exists()) apkFile.delete();

            DownloadManager.Request req = new DownloadManager.Request(Uri.parse(url));
            req.setTitle("Hi！bili 更新");
            req.setDescription("正在下载新版本");
            req.setDestinationUri(Uri.fromFile(apkFile));
            req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            req.setMimeType("application/vnd.android.package-archive");

            DownloadManager dm = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
            long id = dm.enqueue(req);

            // 轮询等待下载完成后启动安装
            final File file = apkFile;
            final long downloadId = id;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    waitForDownload(ctx, downloadId, file);
                }
            }).start();
        } catch (Exception e) {
            // 失败则用浏览器下载兜底
            try {
                Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ctx.startActivity(it);
            } catch (Exception ignore) {
            }
        }
    }

    private static void waitForDownload(Context ctx, long downloadId, File file) {
        DownloadManager dm = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);
        boolean done = false;
        while (!done) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {
            }
            DownloadManager.Query q = new DownloadManager.Query();
            q.setFilterById(downloadId);
            Cursor c = null;
            try {
                c = dm.query(q);
                if (c.moveToFirst()) {
                    int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        done = true;
                        installApk(ctx, file);
                    } else if (status == DownloadManager.STATUS_FAILED) {
                        done = true;
                    }
                }
            } catch (Exception ignore) {
            } finally {
                if (c != null) c.close();
            }
        }
    }

    private static void installApk(Context ctx, File file) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Uri uri = HibiFileProvider.getUriForFile(ctx, file);
                intent.setDataAndType(uri, "application/vnd.android.package-archive");
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            }
            ctx.startActivity(intent);
        } catch (Exception ignore) {
        }
    }
}
