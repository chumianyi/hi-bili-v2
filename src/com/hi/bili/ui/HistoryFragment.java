package com.hi.bili.ui;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hi.bili.R;
import com.hi.bili.adapter.HistoryAdapter;
import com.hi.bili.core.HistoryDB;
import com.hi.bili.model.VideoItem;

import java.util.ArrayList;

/**
 * 观看历史：本地数据库列表，支持单条删除 / 清空全部。
 */
public class HistoryFragment extends Fragment {

    private ListView mListView;
    private TextView mEmpty;
    private HistoryAdapter mAdapter;
    private ArrayList<VideoItem> mData = new ArrayList<VideoItem>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_history, container, false);
        mListView = (ListView) root.findViewById(R.id.list_history);
        mEmpty = (TextView) root.findViewById(R.id.tv_history_empty);

        mAdapter = new HistoryAdapter(getActivity(), mData);
        mAdapter.setOnDeleteListener(new HistoryAdapter.OnDeleteListener() {
            @Override
            public void onDelete(VideoItem item) {
                HistoryDB.deleteHistory(item.bvid);
                refresh();
            }
        });
        mListView.setAdapter(mAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                VideoItem item = (VideoItem) parent.getItemAtPosition(position);
                if (item != null) {
                    Intent it = new Intent(getActivity(), VideoDetailActivity.class);
                    it.putExtra("bvid", item.bvid);
                    startActivity(it);
                }
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
        mData.clear();
        ArrayList<VideoItem> list = HistoryDB.getAllHistory();
        if (list != null) mData.addAll(list);
        mAdapter.notifyDataSetChanged();
        mEmpty.setVisibility(mData.isEmpty() ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_history, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_clear_all) {
            HistoryDB.clearAll();
            refresh();
            Toast.makeText(getActivity(), R.string.history_cleared, Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
