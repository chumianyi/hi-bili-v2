package com.hi.bili.ui;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hi.bili.R;
import com.hi.bili.core.BiliApi;
import com.hi.bili.core.ImageLoader;
import com.hi.bili.core.Prefs;
import com.hi.bili.model.UserInfo;
import com.hi.bili.util.JsonUtil;

/**
 * 我的：登录态展示 / 头像昵称硬币 / 功能入口 / 退出登录。
 */
public class MineFragment extends Fragment {

    private LinearLayout mLoginArea, mUserArea;
    private TextView mBtnLogin, mBtnLogout, mTvNick, mTvMeta, mTvCoin;
    private ImageView mIvAvatar;
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_mine, container, false);

        mLoginArea = (LinearLayout) root.findViewById(R.id.login_area);
        mUserArea = (LinearLayout) root.findViewById(R.id.user_area);
        mBtnLogin = (TextView) root.findViewById(R.id.btn_login);
        mBtnLogout = (TextView) root.findViewById(R.id.btn_logout);
        mTvNick = (TextView) root.findViewById(R.id.tv_nickname);
        mTvMeta = (TextView) root.findViewById(R.id.tv_user_meta);
        mTvCoin = (TextView) root.findViewById(R.id.tv_coin);
        mIvAvatar = (ImageView) root.findViewById(R.id.iv_avatar);

        mBtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), LoginActivity.class));
            }
        });

        root.findViewById(R.id.row_history).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).showHistoryTab();
            }
        });

        root.findViewById(R.id.row_favorite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).showToast("收藏功能开发中");
            }
        });

        root.findViewById(R.id.row_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
            }
        });

        root.findViewById(R.id.row_about).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new android.app.AlertDialog.Builder(getActivity())
                        .setTitle(R.string.settings_about)
                        .setMessage(R.string.about_desc)
                        .setPositiveButton("确定", null)
                        .show();
            }
        });

        mBtnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLogout();
            }
        });
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    public void refresh() {
        if (!Prefs.isLoggedIn()) {
            showLogoutState();
            return;
        }
        // 已登录，拉取用户信息
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String json = BiliApi.getNav();
                    final UserInfo info = JsonUtil.parseUserInfo(new org.json.JSONObject(json).optJSONObject("data"));
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (info != null && info.isLogin) {
                                showLoginState(info);
                            } else {
                                showLogoutState();
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void showLoginState(UserInfo info) {
        mLoginArea.setVisibility(View.GONE);
        mUserArea.setVisibility(View.VISIBLE);
        mBtnLogout.setVisibility(View.VISIBLE);
        mTvNick.setText(info.uname == null ? "" : info.uname);
        mTvMeta.setText("LV." + info.level);
        mTvCoin.setText("硬币: " + info.coin);
        if (info.face != null && info.face.length() > 0) {
            ImageLoader.display(mIvAvatar, info.face, R.drawable.bg_circle_gray);
        }
    }

    private void showLogoutState() {
        mLoginArea.setVisibility(View.VISIBLE);
        mUserArea.setVisibility(View.GONE);
        mBtnLogout.setVisibility(View.GONE);
    }

    private void doLogout() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BiliApi.logout();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    Prefs.clearLogin();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            showLogoutState();
                            ((MainActivity) getActivity()).showToast(getString(R.string.logout));
                        }
                    });
                }
            }
        }).start();
    }
}
