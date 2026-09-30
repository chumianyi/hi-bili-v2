package com.hi.bili.model;

/**
 * 评论项数据模型
 */
public class CommentItem {
    public String rpid;
    public String mid;
    public String uname;
    public String avatar;
    public String content;
    public int like;
    public long ctime;
    public int rcount;  // 子评论数

    public CommentItem() {
        this.rpid = "";
        this.mid = "";
        this.uname = "";
        this.avatar = "";
        this.content = "";
    }
}
