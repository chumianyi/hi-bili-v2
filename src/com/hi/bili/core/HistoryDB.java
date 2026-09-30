package com.hi.bili.core;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.hi.bili.model.VideoItem;

import java.util.ArrayList;

/**
 * 本地观看历史 SQLite 存储。
 */
public class HistoryDB {

    private static final String DB_NAME = "hi_bili_history.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE = "history";

    private static DBHelper helper;

    private static class DBHelper extends SQLiteOpenHelper {
        DBHelper(Context ctx) {
            super(ctx.getApplicationContext(), DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql = "CREATE TABLE " + TABLE + " (" +
                    "bvid TEXT PRIMARY KEY, " +
                    "aid TEXT, " +
                    "cid TEXT, " +
                    "title TEXT, " +
                    "pic TEXT, " +
                    "owner_name TEXT, " +
                    "owner_mid TEXT, " +
                    "view INTEGER, " +
                    "duration INTEGER, " +
                    "pubdate INTEGER, " +
                    "watch_time INTEGER)";
            db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE);
            onCreate(db);
        }
    }

    public static void init(Context ctx) {
        if (helper == null) {
            helper = new DBHelper(ctx);
        }
    }

    private static SQLiteDatabase db() {
        return helper.getWritableDatabase();
    }

    /** 添加/更新一条历史（重复观看更新 watch_time） */
    public static void addHistory(VideoItem item) {
        if (item == null || item.bvid == null || item.bvid.length() == 0) return;
        try {
            ContentValues cv = new ContentValues();
            cv.put("bvid", item.bvid);
            cv.put("aid", item.aid);
            cv.put("cid", item.cid);
            cv.put("title", item.title);
            cv.put("pic", item.pic);
            cv.put("owner_name", item.ownerName);
            cv.put("owner_mid", item.ownerMid);
            cv.put("view", item.view);
            cv.put("duration", item.duration);
            cv.put("pubdate", item.pubdate);
            cv.put("watch_time", System.currentTimeMillis());
            db().insertWithOnConflict(TABLE, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
        } catch (Exception e) {
            // 容错
        }
    }

    /** 按观看时间倒序返回全部历史 */
    public static ArrayList<VideoItem> getAllHistory() {
        ArrayList<VideoItem> list = new ArrayList<VideoItem>();
        Cursor c = null;
        try {
            c = db().rawQuery("SELECT * FROM " + TABLE + " ORDER BY watch_time DESC", null);
            while (c.moveToNext()) {
                VideoItem item = new VideoItem();
                item.bvid = c.getString(c.getColumnIndex("bvid"));
                item.aid = c.getString(c.getColumnIndex("aid"));
                item.cid = c.getString(c.getColumnIndex("cid"));
                item.title = c.getString(c.getColumnIndex("title"));
                item.pic = c.getString(c.getColumnIndex("pic"));
                item.ownerName = c.getString(c.getColumnIndex("owner_name"));
                item.ownerMid = c.getString(c.getColumnIndex("owner_mid"));
                item.view = c.getInt(c.getColumnIndex("view"));
                item.duration = c.getInt(c.getColumnIndex("duration"));
                item.pubdate = c.getLong(c.getColumnIndex("pubdate"));
                list.add(item);
            }
        } catch (Exception e) {
            // 容错
        } finally {
            if (c != null) c.close();
        }
        return list;
    }

    public static void deleteHistory(String bvid) {
        try {
            db().delete(TABLE, "bvid=?", new String[]{bvid});
        } catch (Exception e) {
            // 容错
        }
    }

    public static void clearAll() {
        try {
            db().delete(TABLE, null, null);
        } catch (Exception e) {
            // 容错
        }
    }

    public static boolean exists(String bvid) {
        Cursor c = null;
        try {
            c = db().rawQuery("SELECT 1 FROM " + TABLE + " WHERE bvid=? LIMIT 1", new String[]{bvid});
            return c.moveToFirst();
        } catch (Exception e) {
            return false;
        } finally {
            if (c != null) c.close();
        }
    }
}
