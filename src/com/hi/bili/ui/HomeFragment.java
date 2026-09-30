package com.hi.bili.ui;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hi.bili.R;
import com.hi.bili.adapter.VideoAdapter;
import com.hi.bili.core.BiliApi;
import com.hi.bili.model.VideoItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 首页：热门 / 排行榜 / 分区 视频列表，下拉刷新 + 上拉加载更多。
 */
public class HomeFragment extends Fragment {

    private static final int MODE_POPULAR = 0;
    private static final int MODE_RANKING = 1;
    private static final int MODE_REGION = 2;

    private ListView mListView;
    private ProgressBar mRefreshBar;
    private TextView mTvPopular, mTvRanking, mTvRegion;
    private VideoAdapter mAdapter;
    private ArrayList<VideoItem> mData = new ArrayList<VideoItem>();

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private int mPage = 1;
    private int mMode = MODE_POPULAR;
    private boolean mLoading = false;
    private View mFooter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        mListView = (ListView) root.findViewById(R.id.list_videos);
        mRefreshBar = (ProgressBar) root.findViewById(R.id.refresh_bar);
        mTvPopular = (TextView) root.findViewById(R.id.tab_popular);
        mTvRanking = (TextView) root.findViewById(R.id.tab_ranking);
        mTvRegion = (TextView) root.findViewById(R.id.tab_region);

        mFooter = inflater.inflate(R.layout.item_load_more, null);
        mListView.addFooterView(mFooter);
        mFooter.findViewById(R.id.tv_load_more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMore();
            }
        });

        mAdapter = new VideoAdapter(getActivity(), mData);
        mListView.setAdapter(mAdapter);

        mTvPopular.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchMode(MODE_POPULAR);
            }
        });
        mTvRanking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchMode(MODE_RANKING);
            }
        });
        mTvRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchMode(MODE_REGION);
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                VideoItem item = (VideoItem) parent.getItemAtPosition(position);
                if (item != null) {
                    openVideo(item.bvid);
                }
            }
        });

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount >= totalItemCount && totalItemCount > 0 && !mLoading) {
                    loadMore();
                }
            }
        });

        loadPopular(true);
        return root;
    }

    private void switchMode(int mode) {
        mMode = mode;
        int pink = getResources().getColor(R.color.colorPrimary);
        int gray = getResources().getColor(R.color.textSecondary);
        mTvPopular.setTextColor(mode == MODE_POPULAR ? pink : gray);
        mTvRanking.setTextColor(mode == MODE_RANKING ? pink : gray);
        mTvRegion.setTextColor(mode == MODE_REGION ? pink : gray);
        mTvPopular.getPaint().setFakeBold(mode == MODE_POPULAR);
        mTvRanking.getPaint().setFakeBold(mode == MODE_RANKING);
        mTvRegion.getPaint().setFakeBold(mode == MODE_REGION);
        loadPopular(true);
    }

    private void loadPopular(final boolean refresh) {
        if (mLoading) return;
        mLoading = true;
        if (refresh) mPage = 1;
        mRefreshBar.setVisibility(View.VISIBLE);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String json;
                    if (mMode == MODE_POPULAR) {
                        json = BiliApi.getPopular(mPage, 20);
                    } else if (mMode == MODE_RANKING) {
                        json = BiliApi.getRanking(0, "");
                    } else {
                        json = BiliApi.getRegionNew(0, mPage, 20);
                    }
                    final ArrayList<VideoItem> list = parseList(json);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (refresh) {
                                mData.clear();
                            }
                            mData.addAll(list);
                            mAdapter.notifyDataSetChanged();
                            mPage++;
                            mRefreshBar.setVisibility(View.GONE);
                            mLoading = false;
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mRefreshBar.setVisibility(View.GONE);
                            mLoading = false;
                        }
                    });
                }
            }
        }).start();
    }

    private void loadMore() {
        loadPopular(false);
    }

    private ArrayList<VideoItem> parseList(String json) {
        ArrayList<VideoItem> result = new ArrayList<VideoItem>();
        try {
            JSONObject root = new JSONObject(json);
            JSONObject data = root.optJSONObject("data");
            JSONArray arr = null;
            if (data != null) {
                arr = data.optJSONArray("list");
                if (arr == null) arr = data.optJSONArray("items");
                if (arr == null) arr = data.optJSONArray("media_list");
            }
            if (arr == null) arr = root.optJSONArray("result");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.optJSONObject(i);
                    if (obj != null) {
                        VideoItem vi = com.hi.bili.util.JsonUtil.parseVideoItem(obj);
                        if (vi != null) result.add(vi);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /** 点击视频：先提示加载，再跳转详情页 */
    private void openVideo(final String bvid) {
        final MainActivity act = (MainActivity) getActivity();
        act.showLoading(getString(R.string.loading));
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BiliApi.getVideoView(bvid);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            act.hideLoading();
                            Intent it = new Intent(act, VideoDetailActivity.class);
                            it.putExtra("bvid", bvid);
                            startActivity(it);
                        }
                    });
                }
            }
        }).start();
    }
}
