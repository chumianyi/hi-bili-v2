package com.hi.bili.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.hi.bili.R;
import com.hi.bili.core.ImageLoader;
import com.hi.bili.model.VideoItem;
import com.hi.bili.util.TimeUtil;

import java.util.ArrayList;

/**
 * 观看历史适配器
 */
public class HistoryAdapter extends BaseAdapter {

    public interface OnDeleteListener {
        void onDelete(VideoItem item);
    }

    private Context mContext;
    private ArrayList<VideoItem> mList;
    private LayoutInflater mInflater;
    private OnDeleteListener mDeleteListener;

    public HistoryAdapter(Context context, ArrayList<VideoItem> list) {
        this.mContext = context;
        this.mList = list;
        this.mInflater = LayoutInflater.from(context);
    }

    public void setOnDeleteListener(OnDeleteListener l) {
        this.mDeleteListener = l;
    }

    public void setData(ArrayList<VideoItem> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList == null ? null : mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_history, parent, false);
            holder = new ViewHolder();
            holder.ivCover = (ImageView) convertView.findViewById(R.id.iv_history_cover);
            holder.tvTitle = (TextView) convertView.findViewById(R.id.tv_history_title);
            holder.tvTime = (TextView) convertView.findViewById(R.id.tv_history_time);
            holder.btnDelete = (ImageButton) convertView.findViewById(R.id.btn_delete_history);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final VideoItem item = mList.get(position);
        holder.tvTitle.setText(item.title == null ? "" : item.title);
        holder.tvTime.setText(TimeUtil.formatDate(item.pubdate));

        if (item.pic != null && item.pic.length() > 0) {
            ImageLoader.display(holder.ivCover, item.pic, R.drawable.bg_cover);
        } else {
            holder.ivCover.setImageResource(R.drawable.bg_cover);
        }

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDeleteListener != null) {
                    mDeleteListener.onDelete(item);
                }
            }
        });
        return convertView;
    }

    static class ViewHolder {
        ImageView ivCover;
        TextView tvTitle;
        TextView tvTime;
        ImageButton btnDelete;
    }
}
