package com.hi.bili.core;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * SharedPreferences 封装。
 */
public class Prefs {

    private static final String NAME = "hi_bili_prefs";
    private static SharedPreferences sp;

    private static final String KEY_COOKIES = "cookies";
    private static final String KEY_PLAYER_TYPE = "player_type";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_SEARCH_HISTORY = "search_history";
    private static final String KEY_WBI_IMG = "wbi_img_key";
    private static final String KEY_WBI_SUB = "wbi_sub_key";

    public static void init(Context ctx) {
        if (sp == null) {
            sp = ctx.getApplicationContext().getSharedPreferences(NAME, Context.MODE_PRIVATE);
        }
    }

    private static SharedPreferences sp() {
        return sp;
    }

    // ===== 登录态 =====

    /** 保存 Cookie 集合（序列化为 "k=v; k2=v2"） */
    public static void setCookies(Map<String, String> cookies) {
        if (cookies == null || cookies.isEmpty()) {
            sp().edit().putString(KEY_COOKIES, "").apply();
            return;
        }
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> e : cookies.entrySet()) {
            if (!first) sb.append("; ");
            sb.append(e.getKey()).append('=').append(e.getValue());
            first = false;
        }
        sp().edit().putString(KEY_COOKIES, sb.toString()).apply();
    }

    /** 读取已保存的 Cookie 集合 */
    public static Map<String, String> getCookies() {
        Map<String, String> map = new LinkedHashMap<String, String>();
        String raw = sp().getString(KEY_COOKIES, "");
        if (raw == null || raw.length() == 0) return map;
        String[] pairs = raw.split(";");
        for (String p : pairs) {
            p = p.trim();
            if (p.length() == 0) continue;
            int eq = p.indexOf('=');
            if (eq > 0) {
                String k = p.substring(0, eq).trim();
                String v = p.substring(eq + 1).trim();
                if (k.length() > 0) map.put(k, v);
            }
        }
        return map;
    }

    public static String getSessdata() {
        return getCookies().get("SESSDATA");
    }

    public static String getCsrf() {
        return getCookies().get("bili_jct");
    }

    public static String getDedeUserID() {
        return getCookies().get("DedeUserID");
    }

    public static boolean isLoggedIn() {
        String sess = getSessdata();
        return sess != null && sess.length() > 0;
    }

    public static void clearLogin() {
        sp().edit().remove(KEY_COOKIES).apply();
    }

    // ===== 设置 =====

    public static void setPlayerType(String type) {
        sp().edit().putString(KEY_PLAYER_TYPE, type).apply();
    }

    public static String getPlayerType() {
        return sp().getString(KEY_PLAYER_TYPE, "internal");
    }

    public static void setThemeMode(String mode) {
        sp().edit().putString(KEY_THEME_MODE, mode).apply();
    }

    public static String getThemeMode() {
        return sp().getString(KEY_THEME_MODE, "auto");
    }

    // ===== 搜索历史（以换行分隔存储） =====

    public static ArrayList<String> getSearchHistory() {
        ArrayList<String> list = new ArrayList<String>();
        String raw = sp().getString(KEY_SEARCH_HISTORY, "");
        if (raw == null || raw.length() == 0) return list;
        String[] arr = raw.split("\n");
        for (String s : arr) {
            if (s != null && s.trim().length() > 0) {
                list.add(s.trim());
            }
        }
        return list;
    }

    public static void addSearchHistory(String keyword) {
        if (keyword == null) return;
        keyword = keyword.trim();
        if (keyword.length() == 0) return;
        ArrayList<String> list = getSearchHistory();
        list.remove(keyword);           // 去重，最新的排最前
        list.add(0, keyword);
        while (list.size() > 20) {
            list.remove(list.size() - 1);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append('\n');
            sb.append(list.get(i));
        }
        sp().edit().putString(KEY_SEARCH_HISTORY, sb.toString()).apply();
    }

    public static void clearSearchHistory() {
        sp().edit().remove(KEY_SEARCH_HISTORY).apply();
    }

    // ===== WBI keys 缓存 =====

    public static void setWbiKeys(String imgKey, String subKey) {
        sp().edit().putString(KEY_WBI_IMG, imgKey == null ? "" : imgKey)
                .putString(KEY_WBI_SUB, subKey == null ? "" : subKey).apply();
    }

    public static String getWbiImgKey() {
        return sp().getString(KEY_WBI_IMG, "");
    }

    public static String getWbiSubKey() {
        return sp().getString(KEY_WBI_SUB, "");
    }
}
