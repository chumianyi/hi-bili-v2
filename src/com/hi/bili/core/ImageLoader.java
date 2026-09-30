package com.hi.bili.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 自实现图片加载器：LruCache 内存缓存 + HttpURLConnection 下载。
 */
public class ImageLoader {

    private static LruCache<String, Bitmap> memoryCache;
    private static Handler mainHandler;

    public static void init(Context ctx) {
        if (memoryCache == null) {
            int maxMem = (int) (Runtime.getRuntime().maxMemory() / 1024);
            int cacheSize = maxMem / 8; // 取 1/8 堆内存
            memoryCache = new LruCache<String, Bitmap>(cacheSize) {
                @Override
                protected int sizeOf(String key, Bitmap bitmap) {
                    return bitmap.getByteCount() / 1024;
                }
            };
        }
        if (mainHandler == null) {
            mainHandler = new Handler(Looper.getMainLooper());
        }
    }

    public static void display(ImageView iv, String url) {
        display(iv, url, 0);
    }

    public static void display(final ImageView iv, final String url, final int placeholderResId) {
        if (iv == null || url == null) return;
        iv.setTag(url); // 防止列表复用错位

        if (placeholderResId != 0) {
            iv.setImageResource(placeholderResId);
        }

        final Bitmap cached = memoryCache.get(url);
        if (cached != null) {
            iv.setImageBitmap(cached);
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final Bitmap bmp = getBitmap(url);
                    if (bmp == null) return;
                    memoryCache.put(url, bmp);
                    mainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            // 仅当 ImageView 仍绑定该 url 时设置，避免错位
                            if (url.equals(iv.getTag())) {
                                iv.setImageBitmap(bmp);
                            }
                        }
                    });
                } catch (Exception e) {
                    // 忽略加载失败
                }
            }
        }).start();
    }

    /** 同步获取 Bitmap（子线程调用） */
    public static Bitmap getBitmap(String url) throws Exception {
        if (url == null || url.length() == 0) return null;
        Bitmap cached = memoryCache.get(url);
        if (cached != null) return cached;

        HttpURLConnection conn = null;
        BufferedInputStream bis = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestProperty("User-Agent", HttpUtil.DEFAULT_UA);
            conn.setRequestProperty("Referer", "https://www.bilibili.com");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(15000);
            bis = new BufferedInputStream(conn.getInputStream());
            // 先按采样率解码，避免 OOM
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(bis, null, opts);
            bis.close();

            bis = new BufferedInputStream(conn.getInputStream());
            opts.inJustDecodeBounds = false;
            opts.inSampleSize = calculateInSampleSize(opts, 480, 360);
            opts.inPreferredConfig = Bitmap.Config.RGB_565;
            return BitmapFactory.decodeStream(bis, null, opts);
        } finally {
            if (bis != null) {
                try { bis.close(); } catch (Exception ignore) {}
            }
            if (conn != null) conn.disconnect();
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options opts, int reqW, int reqH) {
        int height = opts.outHeight;
        int width = opts.outWidth;
        int inSampleSize = 1;
        if (height > reqH || width > reqW) {
            int halfH = height / 2;
            int halfW = width / 2;
            while ((halfH / inSampleSize) >= reqH && (halfW / inSampleSize) >= reqW) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    public static void clearCache() {
        if (memoryCache != null) {
            memoryCache.evictAll();
        }
    }
}
