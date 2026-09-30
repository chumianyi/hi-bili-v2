# Hi！bili v2 层间契约 (Core ↔ UI Contract)

## 包结构
- `com.hi.bili.core` — 核心层（网络、API、存储、播放器引擎）
- `com.hi.bili.model` — 数据模型
- `com.hi.bili.ui` — Activity / Fragment
- `com.hi.bili.adapter` — RecyclerView/ListView Adapter
- `com.hi.bili.util` — 工具类

## 构建配置
- minSdk=22, targetSdk=34, versionCode=1, versionName="2.0.0"
- 纯 Java 7 语法（无 lambda、无 Kotlin）
- javac -source 1.7 -target 1.7 编译
- 包名: com.hi.bili

## 主题策略（重要）
- 默认 M2 主题 (AppTheme)
- API >= 28 (Android 9): 切换 M3 主题 (AppTheme.M3)
- API >= 31 (Android 12+): M3 下可选动态取色 (Material You)
- 在 BaseActivity / Application onCreate 中按 SDK_INT 调用 setTheme()
- styles.xml 中定义 AppTheme (M2) 和 AppTheme.M3 (M3) 两套

## ========== 核心层必须提供的类与方法签名 ==========

### model/VideoItem.java
```java
public class VideoItem {
    public String bvid;
    public String aid;
    public String cid;
    public String title;
    public String pic;       // 封面URL
    public String desc;
    public String ownerName; // UP主名
    public String ownerMid;
    public String ownerFace;
    public int view;         // 播放数
    public int danmaku;
    public int reply;
    public int favorite;
    public int coin;
    public int share;
    public int like;
    public int duration;     // 秒
    public String tname;     // 分区名
    public long pubdate;
}
```

### model/VideoDetail.java
```java
public class VideoDetail {
    public String bvid;
    public String aid;
    public String cid;
    public int videos;       // 分P数
    public String title;
    public String pic;
    public String desc;
    public int duration;
    public String ownerName;
    public String ownerMid;
    public String ownerFace;
    public int view, danmaku, reply, favorite, coin, share, like;
    public ArrayList<PageItem> pages; // 分P列表
}
```

### model/PageItem.java
```java
public class PageItem {
    public String cid;
    public int page;
    public String part;   // 分P标题
    public int duration;
}
```

### model/UserInfo.java
```java
public class UserInfo {
    public boolean isLogin;
    public String mid;
    public String uname;
    public String face;
    public int level;
    public int coin;       // 硬币数
    public String sign;
}
```

### model/CommentItem.java
```java
public class CommentItem {
    public String rpid;
    public String mid;
    public String uname;
    public String avatar;
    public String content;
    public int like;
    public long ctime;
    public int rcount;  // 子评论数
}
```

### model/DanmakuItem.java
```java
public class DanmakuItem {
    public float time;    // 出现时间(秒)
    public int type;      // 1滚动 2底部 3顶部
    public int fontSize;
    public String color;
    public String content;
}
```

### core/HttpUtil.java
```java
public class HttpUtil {
    // GET请求，返回响应体字符串
    public static String get(String url) throws Exception;
    // GET请求带请求头
    public static String get(String url, Map<String,String> headers) throws Exception;
    // POST请求，body为表单参数
    public static String post(String url, Map<String,String> params) throws Exception;
    // POST请求带请求头
    public static String post(String url, Map<String,String> params, Map<String,String> headers) throws Exception;
    // 下载文件到指定路径
    public static void download(String url, String savePath, Map<String,String> headers) throws Exception;
    // 获取响应头（用于登录时取Set-Cookie）
    public static Map<String,String> getResponseHeaders(String url, Map<String,String> headers) throws Exception;
}
```

### core/WbiSign.java
```java
public class WbiSign {
    // 从nav接口的wbi_img提取并缓存img_key/sub_key
    public static void updateKeys(String imgUrl, String subUrl);
    // 对参数Map进行WBI签名，返回追加了w_rid和wts的新Map
    public static Map<String,String> sign(Map<String,String> params);
    // 获取带签名的完整URL
    public static String signUrl(String baseUrl, Map<String,String> params);
}
```

### core/BiliApi.java
所有方法均为同步阻塞，调用方需在子线程执行。返回原始JSON字符串，由UI层解析。
```java
public class BiliApi {
    // === 登录 ===
    public static String generateQrcode();           // 申请二维码 JSON
    public static String pollQrcode(String qrcodeKey); // 轮询扫码状态 JSON
    public static String getNav();                    // 当前登录状态/用户信息 JSON
    public static String getCoin();                   // 硬币数 JSON
    public static String logout();                    // 退出登录 JSON

    // === 列表 ===
    public static String getPopular(int pn, int ps);  // 热门列表
    public static String getRanking(int rid, String type); // 排行榜
    public static String getRelated(String bvid);     // 相关推荐
    public static String getRegionNew(int rid, int pn, int ps); // 分区最新

    // === 视频信息 ===
    public static String getVideoView(String bvid);   // 视频详情
    public static String getPageList(String bvid);    // 分P列表
    public static String getVideoStat(String bvid);   // 状态数

    // === 搜索（WBI签名）===
    public static String searchVideo(String keyword, int page); // 搜索视频

    // === 播放（WBI签名）===
    public static String getPlayUrl(String avid, String cid, int qn); // 播放地址 fnval=1

    // === 弹幕 ===
    public static String getDanmaku(String oid);      // XML弹幕

    // === 评论 ===
    public static String getComments(String oid, int next); // 评论列表 type=1
    public static String likeComment(String rpid, String oid, int action); // 评论点赞

    // === 互动（需登录+csrf）===
    public static String likeVideo(String bvid, int like); // 点赞 1=赞 2=取消
    public static String addCoin(String bvid, int multiply); // 投币 1或2
    public static String dealFavorite(String rid, String addIds, String delIds); // 收藏
}
```

### core/Prefs.java
```java
public class Prefs {
    public static void init(Context ctx);
    // 登录态
    public static void setCookies(Map<String,String> cookies);
    public static Map<String,String> getCookies();
    public static String getSessdata();
    public static String getCsrf(); // bili_jct
    public static String getDedeUserID();
    public static boolean isLoggedIn();
    public static void clearLogin();
    // 设置
    public static void setPlayerType(String type); // "internal" or "external"
    public static String getPlayerType();
    public static void setThemeMode(String mode);  // "auto"/"light"/"dark"
    public static String getThemeMode();
    // 搜索历史
    public static ArrayList<String> getSearchHistory();
    public static void addSearchHistory(String keyword);
    public static void clearSearchHistory();
    // WBI keys缓存
    public static void setWbiKeys(String imgKey, String subKey);
    public static String getWbiImgKey();
    public static String getWbiSubKey();
}
```

### core/HistoryDB.java
```java
public class HistoryDB {
    public static void init(Context ctx);
    public static void addHistory(VideoItem item);  // 观看时插入
    public static ArrayList<VideoItem> getAllHistory(); // 按时间倒序
    public static void deleteHistory(String bvid);
    public static void clearAll();
    public static boolean exists(String bvid);
}
```

### core/ImageLoader.java
```java
public class ImageLoader {
    public static void init(Context ctx);
    // 异步加载图片到ImageView，带内存缓存
    public static void display(ImageView iv, String url);
    // 带占位图
    public static void display(ImageView iv, String url, int placeholderResId);
    // 同步获取Bitmap（子线程调用）
    public static Bitmap getBitmap(String url) throws Exception;
    public static void clearCache();
}
```

### core/MimianPlayer.java
自研播放器，基于MediaPlayer+SurfaceView封装
```java
public class MimianPlayer {
    public MimianPlayer(Context ctx, SurfaceView surface);
    public void setVideoPath(String url);
    public void setReferer(String referer); // 设置Referer头
    public void prepare();
    public void start();
    public void pause();
    public void stop();
    public void release();
    public boolean isPlaying();
    public int getCurrentPosition();
    public int getDuration();
    public void seekTo(int msec);
    public void setOnPreparedListener(OnPreparedListener l);
    public void setOnErrorListener(OnErrorListener l);
    public void setOnCompletionListener(OnCompletionListener l);
    public interface OnPreparedListener { void onPrepared(); }
    public interface OnErrorListener { void onError(int what, int extra); }
    public interface OnCompletionListener { void onCompletion(); }
}
```

### core/DanmakuView.java
弹幕叠加View，继承View
```java
public class DanmakuView extends View {
    public DanmakuView(Context ctx);
    public DanmakuView(Context ctx, AttributeSet attrs);
    public void setDanmakuList(ArrayList<DanmakuItem> list);
    public void setTime(float currentTimeSec); // 播放器回调更新时间
    public void setDanmakuEnabled(boolean enabled);
    public boolean isDanmakuEnabled();
    public void setTextSize(float sp);
    public void release();
}
```

### core/CrashHandler.java
```java
public class CrashHandler implements UncaughtExceptionHandler {
    public static void init(Context ctx);
    // 崩溃后启动CrashActivity显示日志
}
```

### core/UpdateChecker.java
```java
public class UpdateChecker {
    public static class UpdateInfo {
        public String versionName;
        public int versionCode;
        public String downloadUrl;
        public String changelog;
    }
    public static UpdateInfo check(String versionJsonUrl) throws Exception;
    public static void downloadAndInstall(Context ctx, String url);
}
```

### util/TimeUtil.java
```java
public class TimeUtil {
    public static String formatDuration(int seconds); // "12:34"
    public static String formatNumber(int num);       // "1.2万"
    public static String formatDate(long timestamp);  // "2026-09-30"
}
```

### util/JsonUtil.java
```java
public class JsonUtil {
    // 简单JSON解析封装（org.json）
    public static String optString(JSONObject obj, String key);
    public static int optInt(JSONObject obj, String key);
    // VideoItem从JSONObject解析
    public static VideoItem parseVideoItem(JSONObject obj);
    public static VideoDetail parseVideoDetail(JSONObject obj);
    public static UserInfo parseUserInfo(JSONObject obj);
    public static CommentItem parseCommentItem(JSONObject obj);
    public static ArrayList<DanmakuItem> parseDanmakuXml(String xml);
}
```

## ========== UI层必须提供的类 ==========

### ui/SplashActivity.java — 开屏页
- 居中显示图标 + "Hi！bili" 文字
- 淡入+缩放动画
- 3秒后跳转 MainActivity
- 导出为 LAUNCHER

### ui/MainActivity.java — 主界面
- 底部导航 4 Tab: 首页(热门/推荐)、搜索、历史、我的
- Fragment 切换
- 继承 BaseActivity（主题切换逻辑）

### ui/HomeFragment.java — 首页
- 热门视频列表 (popular接口)
- 下拉刷新/上拉加载更多
- 点击视频 → 显示"正在加载中..." → VideoDetailActivity

### ui/SearchFragment.java — 搜索
- 搜索框 + 搜索历史
- 搜索结果列表 (WBI签名搜索接口)
- 点击 → VideoDetailActivity

### ui/HistoryFragment.java — 历史
- 本地历史记录列表
- 左滑删除 / 清空全部
- 点击 → VideoDetailActivity

### ui/MineFragment.java — 我的
- 未登录: 显示登录按钮 → LoginActivity
- 已登录: 头像/昵称/等级/硬币数
- 设置入口 → SettingsActivity
- 历史记录入口
- 退出登录

### ui/VideoDetailActivity.java — 视频详情
- 封面/标题/UP主/播放量/简介
- 分P列表
- 点赞/投币/收藏按钮
- 评论列表
- 点击播放 → PlayerActivity（先显示加载中）

### ui/PlayerActivity.java — 播放页
- MimianPlayer + SurfaceView
- DanmakuView 弹幕叠加
- 自定义控制栏: 播放/暂停、进度条、时间、全屏、弹幕开关
- 外置播放器选项: Intent.ACTION_VIEW

### ui/LoginActivity.java — 扫码登录
- 显示二维码（从generateQrcode获取url生成二维码）
- 轮询pollQrcode
- 成功后保存Cookie → 返回

### ui/SettingsActivity.java — 设置
- 播放方式切换(内置/外置)
- 主题切换(浅色/深色/跟随系统)
- 清除缓存
- 清除历史
- 检查更新
- 关于

### ui/CrashActivity.java — 崩溃显示
- 显示崩溃日志

### adapter/VideoAdapter.java — 视频列表适配器
- 封面/标题/UP主/播放量/时长

### adapter/CommentAdapter.java — 评论适配器
### adapter/HistoryAdapter.java — 历史适配器
### adapter/PageAdapter.java — 分P适配器

## ========== 资源文件 ==========

### res/values/strings.xml
- app_name = "Hi！bili"
- 其他字符串资源

### res/values/colors.xml
- M2 主题色: colorPrimary #FB7299 (B站粉), colorPrimaryDark #E85A80, colorAccent #00A1D6
- M3 主题色定义

### res/values/styles.xml
- AppTheme (M2): parent="@android:style/Theme.Material.Light.NoActionBar" 或兼容
- AppTheme.M3 (M3): 基于API28+的Material风格
- 注意: 纯原生无Material库依赖，用android:style/Theme.Material.*

### res/layout/*.xml
- activity_splash.xml
- activity_main.xml
- fragment_home.xml
- fragment_search.xml
- fragment_history.xml
- fragment_mine.xml
- activity_video_detail.xml
- activity_player.xml
- activity_login.xml
- activity_settings.xml
- activity_crash.xml
- item_video.xml
- item_comment.xml
- item_history.xml
- item_page.xml

### 图标
- 矢量绘制: 上方"Hi！"大字，下方"bili"
- 使用内置开源TTF字体 (assets/fonts/)
- 生成 mdpi~xxxhdpi 五档 PNG → res/mipmap-*/ic_launcher.png
- 可用 Python + Pillow 绘制

### assets/fonts/
- 内置开源TTF字体（如 NotoSansSC-Regular.ttf 或 Lato-Regular.ttf）
- 用于图标文字和界面美化

## ========== 构建文件 ==========

### build.sh
- 参考旧版: aapt2 compile → aapt2 link → javac → d8 → zip → zipalign → apksigner
- min-api 22
- 输出 output/Hi！bili.apk

### .github/workflows/build.yml
- ubuntu-latest, Java 11, Android SDK 34 build-tools 34.0.0
- 执行 build.sh
- 上传 artifact

### AndroidManifest.xml
- 权限: INTERNET, ACCESS_NETWORK_STATE, WRITE_EXTERNAL_STORAGE(maxSdk28), READ_EXTERNAL_STORAGE(maxSdk32), READ_MEDIA_VIDEO, READ_MEDIA_IMAGES, REQUEST_INSTALL_PACKAGES, MODIFY_AUDIO_SETTINGS, FOREGROUND_SERVICE, POST_NOTIFICATIONS
- Application: name=".core.HibiApplication", theme按版本切换
- Activity注册全部UI类
- SplashActivity为LAUNCHER
