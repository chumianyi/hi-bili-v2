package com.hi.bili.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 时间/数字格式化工具
 */
public class TimeUtil {

    /**
     * 秒 -> "MM:SS" 或 "HH:MM:SS"
     */
    public static String formatDuration(int seconds) {
        if (seconds < 0) seconds = 0;
        int h = seconds / 3600;
        int m = (seconds % 3600) / 60;
        int s = seconds % 60;
        String mm = m < 10 ? "0" + m : String.valueOf(m);
        String ss = s < 10 ? "0" + s : String.valueOf(s);
        if (h > 0) {
            return h + ":" + mm + ":" + ss;
        }
        return mm + ":" + ss;
    }

    /**
     * 数字 -> "1.2万" / "1.2亿"
     */
    public static String formatNumber(int num) {
        if (num < 0) num = 0;
        if (num < 10000) {
            return String.valueOf(num);
        } else if (num < 100000000) {
            float w = num / 10000f;
            return formatFloat(w) + "万";
        } else {
            float y = num / 100000000f;
            return formatFloat(y) + "亿";
        }
    }

    private static String formatFloat(float v) {
        // 保留最多一位小数，去掉多余的0
        String s = String.format(Locale.getDefault(), "%.1f", v);
        if (s.endsWith(".0")) {
            s = s.substring(0, s.length() - 2);
        }
        return s;
    }

    /**
     * 时间戳(秒) -> "yyyy-MM-dd"
     */
    public static String formatDate(long timestampSec) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            return sdf.format(new Date(timestampSec * 1000L));
        } catch (Exception e) {
            return "";
        }
    }
}
