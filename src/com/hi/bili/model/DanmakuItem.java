package com.hi.bili.model;

/**
 * 弹幕项数据模型
 */
public class DanmakuItem {
    public float time;    // 出现时间(秒)
    public int type;      // 1滚动 2底部 3顶部
    public int fontSize;
    public String color;
    public String content;

    public DanmakuItem() {
        this.color = "#FFFFFF";
        this.content = "";
    }
}
