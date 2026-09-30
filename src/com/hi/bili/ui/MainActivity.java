package com.hi.bili.ui;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hi.bili.R;

/**
 * 主界面：顶部标题栏 + Fragment 容器 + 自定义底部导航(4 Tab)。
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final int TAB_HOME = 0;
    private static final int TAB_SEARCH = 1;
    private static final int TAB_HISTORY = 2;
    private static final int TAB_MINE = 3;

    private HomeFragment mHomeFragment;
    private SearchFragment mSearchFragment;
    private HistoryFragment mHistoryFragment;
    private MineFragment mMineFragment;

    private LinearLayout mTabHome, mTabSearch, mTabHistory, mTabMine;
    private ImageView mIvHome, mIvSearch, mIvHistory, mIvMine;
    private TextView mTvHome, mTvSearch, mTvHistory, mTvMine;
    private TextView mToolbarTitle;
    private RelativeLayout mToolbar;

    private int mCurrentTab = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar = (RelativeLayout) findViewById(R.id.toolbar);
        mToolbarTitle = (TextView) findViewById(R.id.toolbar_title);

        mTabHome = (LinearLayout) findViewById(R.id.tab_home);
        mTabSearch = (LinearLayout) findViewById(R.id.tab_search);
        mTabHistory = (LinearLayout) findViewById(R.id.tab_history);
        mTabMine = (LinearLayout) findViewById(R.id.tab_mine);

        mIvHome = (ImageView) findViewById(R.id.iv_home);
        mIvSearch = (ImageView) findViewById(R.id.iv_search);
        mIvHistory = (ImageView) findViewById(R.id.iv_history);
        mIvMine = (ImageView) findViewById(R.id.iv_mine);

        mTvHome = (TextView) findViewById(R.id.tv_home);
        mTvSearch = (TextView) findViewById(R.id.tv_search);
        mTvHistory = (TextView) findViewById(R.id.tv_history);
        mTvMine = (TextView) findViewById(R.id.tv_mine);

        mTabHome.setOnClickListener(this);
        mTabSearch.setOnClickListener(this);
        mTabHistory.setOnClickListener(this);
        mTabMine.setOnClickListener(this);

        switchTab(TAB_HOME);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tab_home) {
            switchTab(TAB_HOME);
        } else if (id == R.id.tab_search) {
            switchTab(TAB_SEARCH);
        } else if (id == R.id.tab_history) {
            switchTab(TAB_HISTORY);
        } else if (id == R.id.tab_mine) {
            switchTab(TAB_MINE);
        }
    }

    private void switchTab(int tab) {
        if (tab == mCurrentTab) {
            return;
        }
        mCurrentTab = tab;
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        // 隐藏全部
        if (mHomeFragment != null) ft.hide(mHomeFragment);
        if (mSearchFragment != null) ft.hide(mSearchFragment);
        if (mHistoryFragment != null) ft.hide(mHistoryFragment);
        if (mMineFragment != null) ft.hide(mMineFragment);

        switch (tab) {
            case TAB_HOME:
                if (mHomeFragment == null) {
                    mHomeFragment = new HomeFragment();
                    ft.add(R.id.fragment_container, mHomeFragment);
                } else {
                    ft.show(mHomeFragment);
                }
                mToolbarTitle.setText(R.string.tab_home);
                break;
            case TAB_SEARCH:
                if (mSearchFragment == null) {
                    mSearchFragment = new SearchFragment();
                    ft.add(R.id.fragment_container, mSearchFragment);
                } else {
                    ft.show(mSearchFragment);
                }
                mToolbarTitle.setText(R.string.tab_search);
                break;
            case TAB_HISTORY:
                if (mHistoryFragment == null) {
                    mHistoryFragment = new HistoryFragment();
                    ft.add(R.id.fragment_container, mHistoryFragment);
                } else {
                    ft.show(mHistoryFragment);
                }
                mToolbarTitle.setText(R.string.tab_history);
                break;
            case TAB_MINE:
                if (mMineFragment == null) {
                    mMineFragment = new MineFragment();
                    ft.add(R.id.fragment_container, mMineFragment);
                } else {
                    ft.show(mMineFragment);
                }
                mToolbarTitle.setText(R.string.tab_mine);
                break;
        }
        ft.commitAllowingStateLoss();
        updateTabState(tab);
    }

    private void updateTabState(int selected) {
        tintTab(mIvHome, mTvHome, selected == TAB_HOME);
        tintTab(mIvSearch, mTvSearch, selected == TAB_SEARCH);
        tintTab(mIvHistory, mTvHistory, selected == TAB_HISTORY);
        tintTab(mIvMine, mTvMine, selected == TAB_MINE);
    }

    private void tintTab(ImageView icon, TextView text, boolean selected) {
        int color = selected ? getResources().getColor(R.color.nav_selected)
                : getResources().getColor(R.color.nav_normal);
        Drawable d = icon.getDrawable();
        if (d != null) {
            d.mutate().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
            icon.setImageDrawable(d);
        }
        text.setTextColor(selected ? color : Color.parseColor("#666666"));
    }

    /** 供外部(如我的页)切换到历史 Tab */
    public void showHistoryTab() {
        switchTab(TAB_HISTORY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 从我的页返回时刷新登录态
        if (mMineFragment != null && mCurrentTab == TAB_MINE) {
            mMineFragment.refresh();
        }
    }
}
