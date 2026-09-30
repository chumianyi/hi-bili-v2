package com.hi.bili.core;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * 自定义 FileProvider（纯 AOSP 无 androidx 依赖）。
 * 用于 Android 7.0+ 安装 APK 时共享 file:// URI。
 */
public class HibiFileProvider extends ContentProvider {

    public static final String AUTHORITY = "com.hi.bili.fileprovider";

    @Override
    public boolean onCreate() {
        return true;
    }

    /** 把 File 包装为 content:// URI */
    public static Uri getUriForFile(Context context, File file) {
        try {
            return Uri.parse("content://" + AUTHORITY + "/?" + file.getAbsolutePath());
        } catch (Exception e) {
            return Uri.fromFile(file);
        }
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        try {
            String path = uri.getQuery();
            if (path == null || path.length() == 0) {
                path = uri.getPath();
                if (path != null && path.startsWith("/")) {
                    path = path.substring(1);
                }
            }
            File file = new File(path);
            if (file.exists()) {
                return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY);
            }
        } catch (Exception e) {
            throw new FileNotFoundException("Cannot open: " + uri);
        }
        throw new FileNotFoundException("No file for " + uri);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                       String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return "application/vnd.android.package-archive";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                     String[] selectionArgs) {
        return 0;
    }
}
