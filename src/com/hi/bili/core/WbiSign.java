package com.hi.bili.core;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * B 站 WBI 签名实现。
 * 1. 从 nav 接口的 wbi_img 提取 img_key / sub_key
 * 2. 用 mixinKeyEncTab 混淆得到 mixin_key (前32位)
 * 3. 参数加 wts 时间戳，按 key 排序后 URL 编码拼接，追加 mixin_key 做 MD5 得到 w_rid
 */
public class WbiSign {

    private static final int[] MIXIN_KEY_ENC_TAB = new int[]{
            46, 47, 18, 2, 53, 8, 23, 32, 15, 50, 10, 31, 58, 3, 45, 35,
            27, 43, 5, 49, 33, 9, 42, 19, 29, 28, 14, 39, 12, 38, 41, 13,
            37, 48, 7, 16, 24, 55, 40, 61, 26, 17, 0, 1, 60, 51, 30, 4,
            22, 25, 54, 21, 56, 59, 6, 63, 57, 62, 11, 36, 20, 34, 44, 52
    };

    private static String imgKey = null;
    private static String subKey = null;
    private static String mixinKey = null;

    /**
     * 从 wbi_img 的 url 提取文件名（去扩展名）作为 key。
     */
    private static String extractKey(String url) {
        if (url == null) return "";
        try {
            String file = url;
            int slash = file.lastIndexOf('/');
            if (slash >= 0) file = file.substring(slash + 1);
            int dot = file.lastIndexOf('.');
            if (dot >= 0) file = file.substring(0, dot);
            return file;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 从 nav 接口的 wbi_img 更新并缓存 keys。
     */
    public static void updateKeys(String imgUrl, String subUrl) {
        try {
            imgKey = extractKey(imgUrl);
            subKey = extractKey(subUrl);
            mixinKey = makeMixinKey(imgKey, subKey);
            // 持久化到 Prefs，避免每次都请求 nav
            if (imgKey.length() > 0 && subKey.length() > 0) {
                Prefs.setWbiKeys(imgKey, subKey);
            }
        } catch (Exception e) {
            // 忽略
        }
    }

    private static String makeMixinKey(String img, String sub) {
        String raw = img + sub;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < MIXIN_KEY_ENC_TAB.length; i++) {
            int idx = MIXIN_KEY_ENC_TAB[i];
            if (idx < raw.length()) {
                sb.append(raw.charAt(idx));
            }
        }
        String m = sb.toString();
        return m.length() > 32 ? m.substring(0, 32) : m;
    }

    /**
     * 确保 mixinKey 可用；若内存/Prefs 均无，则返回 false（调用方需先调用 nav）。
     */
    private static synchronized String ensureMixinKey() {
        if (mixinKey != null && mixinKey.length() == 32) {
            return mixinKey;
        }
        String ik = Prefs.getWbiImgKey();
        String sk = Prefs.getWbiSubKey();
        if (ik != null && ik.length() > 0 && sk != null && sk.length() > 0) {
            imgKey = ik;
            subKey = sk;
            mixinKey = makeMixinKey(ik, sk);
        }
        return mixinKey;
    }

    /**
     * 对参数进行 WBI 签名，返回追加了 w_rid 和 wts 的新 Map。
     */
    @SuppressWarnings("unchecked")
    public static Map<String, String> sign(Map<String, String> params) {
        TreeMap<String, String> sorted = new TreeMap<String, String>();
        if (params != null) sorted.putAll(params);
        sorted.put("wts", String.valueOf(System.currentTimeMillis() / 1000));

        String key = ensureMixinKey();
        if (key == null || key.length() < 32) {
            // 无 key 时仅返回带 wts 的参数
            return sorted;
        }

        String query = buildQuery(sorted);
        String wRid = md5(query + key);
        sorted.put("w_rid", wRid);
        return sorted;
    }

    /**
     * 获取带签名的完整 URL。
     */
    public static String signUrl(String baseUrl, Map<String, String> params) {
        Map<String, String> signed = sign(params);
        String query = buildQuery(signed);
        if (baseUrl.contains("?")) {
            return baseUrl + "&" + query;
        }
        return baseUrl + "?" + query;
    }

    private static String buildQuery(Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<String, String>> it = params.entrySet().iterator();
        boolean first = true;
        while (it.hasNext()) {
            Map.Entry<String, String> e = it.next();
            if (!first) sb.append('&');
            try {
                sb.append(URLEncoder.encode(e.getKey(), "UTF-8"));
                sb.append('=');
                sb.append(URLEncoder.encode(e.getValue() == null ? "" : e.getValue(), "UTF-8"));
            } catch (UnsupportedEncodingException ignore) {
            }
            first = false;
        }
        return sb.toString();
    }

    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < digest.length; i++) {
                String hex = Integer.toHexString(0xff & digest[i]);
                if (hex.length() == 1) sb.append('0');
                sb.append(hex);
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
