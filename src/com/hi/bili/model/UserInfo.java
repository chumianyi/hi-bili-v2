package com.hi.bili.model;

/**
 * 用户信息数据模型
 */
public class UserInfo {
    public boolean isLogin;
    public String mid;
    public String uname;
    public String face;
    public int level;
    public int coin;       // 硬币数
    public String sign;

    public UserInfo() {
        this.mid = "";
        this.uname = "";
        this.face = "";
        this.sign = "";
    }
}
