package com.hi.bili.util;

import android.util.Xml;

import com.hi.bili.model.CommentItem;
import com.hi.bili.model.DanmakuItem;
import com.hi.bili.model.PageItem;
import com.hi.bili.model.UserInfo;
import com.hi.bili.model.VideoDetail;
import com.hi.bili.model.VideoItem;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;

/**
 * org.json / XML 解析封装
 */
public class JsonUtil {

    public static String optString(JSONObject obj, String key) {
        if (obj == null) return "";
        try {
            if (obj.isNull(key)) return "";
            String v = obj.optString(key, "");
            return v == null ? "" : v;
        } catch (Exception e) {
            return "";
        }
    }

    public static int optInt(JSONObject obj, String key) {
        if (obj == null) return 0;
        try {
            if (obj.isNull(key)) return 0;
            return obj.optInt(key, 0);
        } catch (Exception e) {
            return 0;
        }
    }

    private static long optLong(JSONObject obj, String key) {
        if (obj == null) return 0L;
        try {
            if (obj.isNull(key)) return 0L;
            return obj.optLong(key, 0L);
        } catch (Exception e) {
            return 0L;
        }
    }

    /**
     * 从 JSONObject 解析 VideoItem。
     * 兼容热门(popular)、搜索(search)、相关推荐、历史等多种字段命名。
     */
    public static VideoItem parseVideoItem(JSONObject obj) {
        if (obj == null) return null;
        VideoItem item = new VideoItem();
        try {
            item.bvid = optString(obj, "bvid");
            // avid 可能是 long
            if (!obj.isNull("aid")) {
                item.aid = String.valueOf(obj.optLong("aid", 0L));
            }
            if (!obj.isNull("id")) {
                // 搜索结果里 avid 字段名是 id
                if (item.aid == null || item.aid.length() == 0 || "0".equals(item.aid)) {
                    long id = obj.optLong("id", 0L);
                    if (id != 0L) item.aid = String.valueOf(id);
                }
            }
            item.cid = optString(obj, "cid");
            item.title = optString(obj, "title");
            item.desc = optString(obj, "description");
            if (item.desc.length() == 0) item.desc = optString(obj, "desc");

            // 封面
            item.pic = optString(obj, "pic");
            if (item.pic.length() == 0) item.pic = optString(obj, "cover");
            if (item.pic.length() == 0) item.pic = optString(obj, "pic");
            if (item.pic.startsWith("//")) item.pic = "https:" + item.pic;

            // UP主
            JSONObject owner = obj.optJSONObject("owner");
            if (owner != null) {
                item.ownerName = optString(owner, "name");
                item.ownerMid = String.valueOf(optLong(owner, "mid"));
                item.ownerFace = optString(owner, "face");
            } else {
                item.ownerName = optString(obj, "author");
                if (item.ownerName.length() == 0) item.ownerName = optString(obj, "name");
                item.ownerMid = String.valueOf(optLong(obj, "mid"));
            }

            // 播放/弹幕等计数
            item.view = extractStat(obj, "view", "play", "play_count", "play_views");
            item.danmaku = extractStat(obj, "danmaku", "danmaku_count");
            item.reply = extractStat(obj, "reply", "reply_count");
            item.favorite = extractStat(obj, "favorite", "fav_count");
            item.coin = extractStat(obj, "coin", "coin_count");
            item.share = extractStat(obj, "share", "share_count");
            item.like = extractStat(obj, "like", "like_count", "praise");

            item.duration = optInt(obj, "duration");
            item.tname = optString(obj, "tname");
            if (item.tname.length() == 0) item.tname = optString(obj, "typename");

            // pubdate 可能是 long
            item.pubdate = optLong(obj, "pubdate");
            if (item.pubdate == 0L) item.pubdate = optLong(obj, "senddate");
        } catch (Exception e) {
            // 容错：返回已解析的部分
        }
        return item;
    }

    /** 依次尝试多个候选字段名取 int 计数（部分接口返回字符串） */
    private static int extractStat(JSONObject obj, String... keys) {
        for (String k : keys) {
            try {
                if (obj.isNull(k)) continue;
                Object v = obj.get(k);
                if (v instanceof Number) {
                    return ((Number) v).intValue();
                }
                String s = String.valueOf(v).replace(",", "").trim();
                if (s.endsWith("万")) {
                    return (int) (Float.parseFloat(s.substring(0, s.length() - 1)) * 10000f);
                }
                return Integer.parseInt(s);
            } catch (Exception ignore) {
            }
        }
        return 0;
    }

    /**
     * 解析视频详情 (x/web-interface/view)
     */
    public static VideoDetail parseVideoDetail(JSONObject obj) {
        if (obj == null) return null;
        VideoDetail d = new VideoDetail();
        try {
            d.bvid = optString(obj, "bvid");
            d.aid = String.valueOf(optLong(obj, "aid"));
            d.videos = optInt(obj, "videos");
            d.title = optString(obj, "title");
            d.pic = optString(obj, "pic");
            if (d.pic.startsWith("//")) d.pic = "https:" + d.pic;
            d.desc = optString(obj, "desc");
            d.duration = optInt(obj, "duration");

            JSONObject owner = obj.optJSONObject("owner");
            if (owner != null) {
                d.ownerName = optString(owner, "name");
                d.ownerMid = String.valueOf(optLong(owner, "mid"));
                d.ownerFace = optString(owner, "face");
            }

            JSONObject stat = obj.optJSONObject("stat");
            if (stat != null) {
                d.view = optInt(stat, "view");
                d.danmaku = optInt(stat, "danmaku");
                d.reply = optInt(stat, "reply");
                d.favorite = optInt(stat, "favorite");
                d.coin = optInt(stat, "coin");
                d.share = optInt(stat, "share");
                d.like = optInt(stat, "like");
            }

            JSONArray pages = obj.optJSONArray("pages");
            if (pages != null) {
                for (int i = 0; i < pages.length(); i++) {
                    JSONObject p = pages.optJSONObject(i);
                    if (p == null) continue;
                    PageItem pi = new PageItem();
                    pi.cid = String.valueOf(optLong(p, "cid"));
                    pi.page = optInt(p, "page");
                    pi.part = optString(p, "part");
                    pi.duration = optInt(p, "duration");
                    d.pages.add(pi);
                }
            }
            // 默认 cid = 第一P
            if (d.pages.size() > 0) {
                d.cid = d.pages.get(0).cid;
            }
        } catch (Exception e) {
            // 容错
        }
        return d;
    }

    /**
     * 解析 nav 接口用户信息
     */
    public static UserInfo parseUserInfo(JSONObject obj) {
        UserInfo u = new UserInfo();
        if (obj == null) return u;
        try {
            JSONObject data = obj.optJSONObject("data");
            JSONObject src = data != null ? data : obj;
            u.isLogin = src.optBoolean("isLogin", false);
            u.mid = String.valueOf(optLong(src, "mid"));
            u.uname = optString(src, "uname");
            u.face = optString(src, "face");
            JSONObject levelInfo = src.optJSONObject("level_info");
            if (levelInfo != null) {
                u.level = optInt(levelInfo, "current_level");
            }
            u.coin = optInt(src, "coin");
            u.sign = optString(src, "sign");
        } catch (Exception e) {
            // 容错
        }
        return u;
    }

    /**
     * 解析单条评论 (v2 reply root)
     */
    public static CommentItem parseCommentItem(JSONObject obj) {
        if (obj == null) return null;
        CommentItem c = new CommentItem();
        try {
            c.rpid = String.valueOf(optLong(obj, "rpid"));
            JSONObject member = obj.optJSONObject("member");
            if (member != null) {
                c.mid = optString(member, "mid");
                c.uname = optString(member, "uname");
                c.avatar = optString(member, "avatar");
            }
            JSONObject content = obj.optJSONObject("content");
            if (content != null) {
                c.content = optString(content, "message");
            }
            c.like = optInt(obj, "like");
            c.ctime = optLong(obj, "ctime");
            c.rcount = optInt(obj, "rcount");
        } catch (Exception e) {
            // 容错
        }
        return c;
    }

    /**
     * 解析 B 站弹幕 XML。
     * <d p="time,type,fontsize,color,ctime,pool,userHash,dbid">弹幕文字</d>
     */
    public static ArrayList<DanmakuItem> parseDanmakuXml(String xml) {
        ArrayList<DanmakuItem> list = new ArrayList<DanmakuItem>();
        if (xml == null || xml.length() == 0) return list;
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new ByteArrayInputStream(xml.getBytes("UTF-8")), "UTF-8");
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && "d".equals(parser.getName())) {
                    String p = parser.getAttributeValue(null, "p");
                    String text = parser.nextText();
                    if (p != null && p.length() > 0 && text != null) {
                        String[] arr = p.split(",");
                        if (arr.length >= 4) {
                            DanmakuItem dm = new DanmakuItem();
                            try {
                                dm.time = Float.parseFloat(arr[0]);
                                dm.type = Integer.parseInt(arr[1]);
                                dm.fontSize = Integer.parseInt(arr[2]);
                                // color 是十进制数字
                                int c = (int) Float.parseFloat(arr[3]);
                                dm.color = String.format("#%06X", (0xFFFFFF & c));
                            } catch (Exception ignore) {
                            }
                            dm.content = text;
                            list.add(dm);
                        }
                    }
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            // 容错
        }
        return list;
    }
}
