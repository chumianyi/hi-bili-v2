package com.hi.bili.model;

/**
 * 分P项数据模型
 */
public class PageItem {
    public String cid;
    public int page;
    public String part;   // 分P标题
    public int duration; // 秒

    public PageItem() {
        this.cid = "";
        this.part = "";
    }
}
