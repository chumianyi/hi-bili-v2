package com.hi.bili.ui;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hi.bili.R;
import com.hi.bili.adapter.VideoAdapter;
import com.hi.bili.core.BiliApi;
import com.hi.bili.core.Prefs;
import com.hi.bili.model.VideoItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 搜索：搜索框 + 历史标签流 + 结果列表(WBI 签名)。
 */
public class SearchFragment extends Fragment {

    private EditText mEtSearch;
    private ListView mListResults;
    private LinearLayout mHistoryContainer;
    private View mHistoryScroller, mHistoryHeader;
    private TextView mTvEmpty;

    private VideoAdapter mAdapter;
    private ArrayList<VideoItem> mData = new ArrayList<VideoItem>();
    private Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_search, container, false);

        mEtSearch = (EditText) root.findViewById(R.id.et_search);
        mListResults = (ListView) root.findViewById(R.id.list_results);
        mHistoryContainer = (LinearLayout) root.findViewById(R.id.history_container);
        mHistoryScroller = root.findViewById(R.id.history_scroller);
        mHistoryHeader = root.findViewById(R.id.history_header);
        mTvEmpty = (TextView) root.findViewById(R.id.tv_search_empty);

        mAdapter = new VideoAdapter(getActivity(), mData);
        mListResults.setAdapter(mAdapter);

        root.findViewById(R.id.btn_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSearch();
            }
        });

        root.findViewById(R.id.btn_clear_history).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Prefs.clearSearchHistory();
                renderHistory();
            }
        });

        mListResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                VideoItem item = (VideoItem) parent.getItemAtPosition(position);
                if (item != null && !TextUtils.isEmpty(item.bvid)) {
                    openVideo(item.bvid);
                }
            }
        });

        renderHistory();
        return root;
    }

    private void renderHistory() {
        ArrayList<String> history = Prefs.getSearchHistory();
        mHistoryContainer.removeAllViews();
        if (history == null || history.isEmpty()) {
            mHistoryScroller.setVisibility(View.GONE);
            mHistoryHeader.setVisibility(View.GONE);
            mTvEmpty.setVisibility(View.VISIBLE);
            return;
        }
        mHistoryScroller.setVisibility(View.VISIBLE);
        mHistoryHeader.setVisibility(View.VISIBLE);
        mTvEmpty.setVisibility(View.GONE);

        for (final String kw : history) {
            TextView chip = (TextView) LayoutInflater.from(getActivity())
                    .inflate(R.layout.item_chip, null);
            chip.setText(kw);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mEtSearch.setText(kw);
                    doSearch();
                }
            });
            mHistoryContainer.addView(chip);
        }
    }

    private void doSearch() {
        final String keyword = mEtSearch.getText().toString().trim();
        if (TextUtils.isEmpty(keyword)) return;

        Prefs.addSearchHistory(keyword);
        renderHistory();

        // 收起键盘
        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(getActivity().INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(mEtSearch.getWindowToken(), 0);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String json = BiliApi.searchVideo(keyword, 1);
                    final ArrayList<VideoItem> list = parseSearch(json);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mData.clear();
                            mData.addAll(list);
                            mAdapter.notifyDataSetChanged();
                            mHistoryScroller.setVisibility(View.GONE);
                            mHistoryHeader.setVisibility(View.GONE);
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ((MainActivity) getActivity()).showToast(getString(R.string.load_failed));
                        }
                    });
                }
            }
        }).start();
    }

    private ArrayList<VideoItem> parseSearch(String json) {
        ArrayList<VideoItem> result = new ArrayList<VideoItem>();
        try {
            JSONObject root = new JSONObject(json);
            JSONObject data = root.optJSONObject("data");
            JSONArray arr = data == null ? null : data.optJSONArray("result");
            if (arr != null) {
                for (int i = 0; i < arr.length(); i++) {
                    JSONObject obj = arr.optJSONObject(i);
                    if (obj == null) continue;
                    // 仅保留视频类型(type==1)
                    int type = obj.optInt("type", 1);
                    if (type == 1) {
                        VideoItem vi = com.hi.bili.util.JsonUtil.parseVideoItem(obj);
                        if (vi != null && !TextUtils.isEmpty(vi.bvid)) {
                            result.add(vi);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

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
