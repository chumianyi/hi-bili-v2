package com.hi.bili.model;

import java.util.ArrayList;

/**
 * 视频详情数据模型
 */
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
    public int view;
    public int danmaku;
    public int reply;
    public int favorite;
    public int coin;
    public int share;
    public int like;
    public ArrayList<PageItem> pages; // 分P列表

    public VideoDetail() {
        this.bvid = "";
        this.aid = "";
        this.cid = "";
        this.title = "";
        this.pic = "";
        this.desc = "";
        this.ownerName = "";
        this.ownerMid = "";
        this.ownerFace = "";
        this.pages = new ArrayList<PageItem>();
    }
}
