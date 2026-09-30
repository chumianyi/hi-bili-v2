package com.hi.bili.model;

/**
 * 视频列表项数据模型
 */
public class VideoItem {
    public String bvid;
    public String aid;
    public String cid;
    public String title;
    public String pic;        // 封面URL
    public String desc;
    public String ownerName;  // UP主名
    public String ownerMid;
    public String ownerFace;
    public int view;          // 播放数
    public int danmaku;
    public int reply;
    public int favorite;
    public int coin;
    public int share;
    public int like;
    public int duration;      // 秒
    public String tname;     // 分区名
    public long pubdate;

    public VideoItem() {
        this.bvid = "";
        this.aid = "";
        this.cid = "";
        this.title = "";
        this.pic = "";
        this.desc = "";
        this.ownerName = "";
        this.ownerMid = "";
        this.ownerFace = "";
        this.tname = "";
    }
}
