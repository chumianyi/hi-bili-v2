package com.hi.bili.core;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * B 站全部开放接口封装。
 * 所有方法均为同步阻塞，调用方须在子线程执行，返回原始 JSON 字符串。
 */
public class BiliApi {

    private static final String API = "https://api.bilibili.com";
    private static final String PASSPORT = "https://passport.bilibili.com";
    private static final String REFERER = "https://www.bilibili.com";

    private static Map<String, String> refererHeader() {
        Map<String, String> h = new HashMap<String, String>();
        h.put("Referer", REFERER);
        return h;
    }

    /** 确保 WBI keys 已就绪；未缓存则先请求 nav */
    private static void ensureWbiKeys() {
        try {
            if ((Prefs.getWbiImgKey() == null || Prefs.getWbiImgKey().length() == 0)
                    || (Prefs.getWbiSubKey() == null || Prefs.getWbiSubKey().length() == 0)) {
                getNav();
            }
        } catch (Exception e) {
            // 忽略，签名时会退化为无签名
        }
    }

    // ===== 登录 =====

    /** 申请登录二维码 */
    public static String generateQrcode() {
        try {
            return HttpUtil.get(PASSPORT + "/x/passport-login/web/qrcode/generate");
        } catch (Exception e) {
            return null;
        }
    }

    /** 轮询扫码状态。成功响应体中含 url 字段，需从中解析 Set-Cookie */
    public static String pollQrcode(String qrcodeKey) {
        try {
            String url = PASSPORT + "/x/passport-login/web/qrcode/poll?qrcode_key="
                    + java.net.URLEncoder.encode(qrcodeKey, "UTF-8");
            return HttpUtil.get(url);
        } catch (Exception e) {
            return null;
        }
    }

    /** 当前登录状态 / 用户信息；同时更新 WBI keys */
    public static String getNav() {
        try {
            String json = HttpUtil.get(API + "/x/web-interface/nav");
            if (json != null) {
                try {
                    JSONObject obj = new JSONObject(json);
                    JSONObject data = obj.optJSONObject("data");
                    if (data != null) {
                        JSONObject wbi = data.optJSONObject("wbi_img");
                        if (wbi != null) {
                            WbiSign.updateKeys(wbi.optString("img_url", ""), wbi.optString("sub_url", ""));
                        }
                    }
                } catch (Exception ignore) {
                }
            }
            return json;
        } catch (Exception e) {
            return null;
        }
    }

    /** 硬币数 */
    public static String getCoin() {
        try {
            return HttpUtil.get("https://account.bilibili.com/site/getCoin");
        } catch (Exception e) {
            return null;
        }
    }

    /** 退出登录 */
    public static String logout() {
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("csrf", Prefs.getCsrf());
            String result = HttpUtil.post(PASSPORT + "/login/exit/v2", params);
            Prefs.clearLogin();
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    // ===== 列表 =====

    /** 热门列表 */
    public static String getPopular(int pn, int ps) {
        try {
            return HttpUtil.get(API + "/x/web-interface/popular?pn=" + pn + "&ps=" + ps);
        } catch (Exception e) {
            return null;
        }
    }

    /** 排行榜 rid=0 全站 */
    public static String getRanking(int rid, String type) {
        try {
            return HttpUtil.get(API + "/x/web-interface/ranking/v2?rid=" + rid + "&type=" + type);
        } catch (Exception e) {
            return null;
        }
    }

    /** 相关推荐 */
    public static String getRelated(String bvid) {
        try {
            return HttpUtil.get(API + "/x/web-interface/related?bvid=" + bvid);
        } catch (Exception e) {
            return null;
        }
    }

    /** 分区最新 */
    public static String getRegionNew(int rid, int pn, int ps) {
        try {
            return HttpUtil.get(API + "/x/web-interface/dynamic/region?rid=" + rid + "&pn=" + pn + "&ps=" + ps);
        } catch (Exception e) {
            return null;
        }
    }

    // ===== 视频信息 =====

    /** 视频详情 */
    public static String getVideoView(String bvid) {
        try {
            return HttpUtil.get(API + "/x/web-interface/view?bvid=" + bvid);
        } catch (Exception e) {
            return null;
        }
    }

    /** 分P列表 */
    public static String getPageList(String bvid) {
        try {
            return HttpUtil.get(API + "/x/player/pagelist?bvid=" + bvid);
        } catch (Exception e) {
            return null;
        }
    }

    /** 播放/点赞等状态数 */
    public static String getVideoStat(String bvid) {
        try {
            return HttpUtil.get(API + "/x/web-interface/archive/stat?bvid=" + bvid);
        } catch (Exception e) {
            return null;
        }
    }

    // ===== 搜索（WBI 签名） =====

    /** 搜索视频 */
    public static String searchVideo(String keyword, int page) {
        try {
            ensureWbiKeys();
            Map<String, String> params = new HashMap<String, String>();
            params.put("search_type", "video");
            params.put("keyword", keyword);
            params.put("page", String.valueOf(page));
            params.put("page_size", "20");
            String url = WbiSign.signUrl(API + "/x/web-interface/wbi/search/type", params);
            return HttpUtil.get(url, refererHeader());
        } catch (Exception e) {
            return null;
        }
    }

    // ===== 播放地址（WBI 签名，带 Referer） =====

    /** 播放地址，fnval=1 mp4，qn=32 */
    public static String getPlayUrl(String avid, String cid, int qn) {
        try {
            ensureWbiKeys();
            Map<String, String> params = new HashMap<String, String>();
            params.put("avid", avid);
            params.put("cid", cid);
            params.put("qn", String.valueOf(qn));
            params.put("fnval", "1");
            params.put("fnver", "0");
            params.put("fourk", "1");
            String url = WbiSign.signUrl(API + "/x/player/wbi/playurl", params);
            return HttpUtil.get(url, refererHeader());
        } catch (Exception e) {
            return null;
        }
    }

    // ===== 弹幕 =====

    /** 弹幕 XML */
    public static String getDanmaku(String oid) {
        try {
            return HttpUtil.get(API + "/x/v1/dm/list.so?oid=" + oid, refererHeader());
        } catch (Exception e) {
            return null;
        }
    }

    // ===== 评论 =====

    /** 评论列表，type=1 视频 */
    public static String getComments(String oid, int next) {
        try {
            return HttpUtil.get(API + "/x/v2/reply?type=1&oid=" + oid + "&pn=" + next + "&ps=20");
        } catch (Exception e) {
            return null;
        }
    }

    /** 评论点赞 action=1 赞 / 0 取消 */
    public static String likeComment(String rpid, String oid, int action) {
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("type", "1");
            params.put("oid", oid);
            params.put("rpid", rpid);
            params.put("action", String.valueOf(action));
            params.put("csrf", Prefs.getCsrf());
            return HttpUtil.post(API + "/v2/reply/action", params);
        } catch (Exception e) {
            return null;
        }
    }

    // ===== 互动（需登录 + csrf） =====

    /** 点赞 1=赞 2=取消 */
    public static String likeVideo(String bvid, int like) {
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("bvid", bvid);
            params.put("like", String.valueOf(like));
            params.put("csrf", Prefs.getCsrf());
            return HttpUtil.post(API + "/x/web-interface/archive/like", params);
        } catch (Exception e) {
            return null;
        }
    }

    /** 投币 multiply=1 或 2 */
    public static String addCoin(String bvid, int multiply) {
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("bvid", bvid);
            params.put("multiply", String.valueOf(multiply));
            params.put("select_like", "1");
            params.put("eab_x", "1");
            params.put("ramval", "1");
            params.put("source", "web_normal");
            params.put("csrf", Prefs.getCsrf());
            return HttpUtil.post(API + "/x/web-interface/coin/add", params);
        } catch (Exception e) {
            return null;
        }
    }

    /** 收藏 addIds/delIds 为收藏夹 media_id 逗号分隔 */
    public static String dealFavorite(String rid, String addIds, String delIds) {
        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("rid", rid);
            params.put("type", "2");
            params.put("add_media_ids", addIds == null ? "" : addIds);
            params.put("del_media_ids", delIds == null ? "" : delIds);
            params.put("csrf", Prefs.getCsrf());
            return HttpUtil.post(API + "/x/v3/fav/resource/deal", params);
        } catch (Exception e) {
            return null;
        }
    }
}
