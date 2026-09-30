package com.hi.bili.core;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HttpURLConnection 封装。
 * - 默认带浏览器 UA
 * - 已登录时自动附带 Cookie（从 Prefs 读取）
 */
public class HttpUtil {

    public static final String DEFAULT_UA =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    private static final int CONNECT_TIMEOUT = 15000;
    private static final int READ_TIMEOUT = 20000;

    /** 构造自动附带的 Cookie 头 */
    private static String buildCookieHeader() {
        try {
            Map<String, String> cookies = Prefs.getCookies();
            if (cookies == null || cookies.isEmpty()) return null;
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, String> e : cookies.entrySet()) {
                if (!first) sb.append("; ");
                sb.append(e.getKey()).append('=').append(e.getValue());
                first = false;
            }
            return sb.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private static void applyCommonHeaders(HttpURLConnection conn, Map<String, String> headers) {
        conn.setRequestProperty("User-Agent", DEFAULT_UA);
        String cookie = buildCookieHeader();
        if (cookie != null && cookie.length() > 0) {
            conn.setRequestProperty("Cookie", cookie);
        }
        if (headers != null) {
            for (Map.Entry<String, String> e : headers.entrySet()) {
                conn.setRequestProperty(e.getKey(), e.getValue());
            }
        }
    }

    /** GET 请求，返回响应体字符串 */
    public static String get(String url) throws Exception {
        return get(url, null);
    }

    /** GET 请求带请求头 */
    public static String get(String url, Map<String, String> headers) throws Exception {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setInstanceFollowRedirects(true);
            applyCommonHeaders(conn, headers);

            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 400) ? conn.getInputStream() : conn.getErrorStream();
            return readStream(is);
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    /** POST 请求，body 为表单参数 */
    public static String post(String url, Map<String, String> params) throws Exception {
        return post(url, params, null);
    }

    /** POST 请求带请求头 */
    public static String post(String url, Map<String, String> params, Map<String, String> headers) throws Exception {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            applyCommonHeaders(conn, headers);

            String body = encodeParams(params);
            DataOutputStream dos = null;
            try {
                dos = new DataOutputStream(conn.getOutputStream());
                dos.write(body.getBytes("UTF-8"));
                dos.flush();
            } finally {
                if (dos != null) dos.close();
            }

            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 400) ? conn.getInputStream() : conn.getErrorStream();
            return readStream(is);
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    /** 下载文件到指定路径 */
    public static void download(String url, String savePath, Map<String, String> headers) throws Exception {
        HttpURLConnection conn = null;
        BufferedInputStream bis = null;
        FileOutputStream fos = null;
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(30000);
            applyCommonHeaders(conn, headers);
            int code = conn.getResponseCode();
            if (code < 200 || code >= 400) {
                throw new Exception("download http code " + code);
            }
            bis = new BufferedInputStream(conn.getInputStream());
            fos = new FileOutputStream(savePath);
            byte[] buffer = new byte[8192];
            int n;
            while ((n = bis.read(buffer)) != -1) {
                fos.write(buffer, 0, n);
            }
            fos.flush();
        } finally {
            if (fos != null) fos.close();
            if (bis != null) bis.close();
            if (conn != null) conn.disconnect();
        }
    }

    /** 获取响应头（用于登录时取 Set-Cookie） */
    public static Map<String, String> getResponseHeaders(String url, Map<String, String> headers) throws Exception {
        HttpURLConnection conn = null;
        Map<String, String> result = new HashMap<String, String>();
        try {
            conn = (HttpURLConnection) new URL(url).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(CONNECT_TIMEOUT);
            conn.setReadTimeout(READ_TIMEOUT);
            applyCommonHeaders(conn, headers);
            // 触发请求
            conn.getResponseCode();
            Map<String, List<String>> map = conn.getHeaderFields();
            for (Map.Entry<String, List<String>> e : map.entrySet()) {
                String k = e.getKey();
                if (k == null) continue;
                List<String> v = e.getValue();
                if (v != null && !v.isEmpty()) {
                    result.put(k, v.get(0));
                }
            }
            return result;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    private static String encodeParams(Map<String, String> params) throws Exception {
        if (params == null || params.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (!first) sb.append('&');
            sb.append(URLEncoder.encode(e.getKey(), "UTF-8"));
            sb.append('=');
            String v = e.getValue() == null ? "" : e.getValue();
            sb.append(URLEncoder.encode(v, "UTF-8"));
            first = false;
        }
        return sb.toString();
    }

    private static String readStream(InputStream is) throws Exception {
        if (is == null) return "";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } finally {
            if (reader != null) reader.close();
        }
    }
}
