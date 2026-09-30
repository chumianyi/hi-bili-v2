package com.hi.bili.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hi.bili.R;
import com.hi.bili.core.ImageLoader;
import com.hi.bili.model.VideoItem;
import com.hi.bili.util.TimeUtil;

import java.util.ArrayList;

/**
 * 视频列表适配器 (热门/搜索/历史通用)
 */
public class VideoAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<VideoItem> mList;
    private LayoutInflater mInflater;

    public VideoAdapter(Context context, ArrayList<VideoItem> list) {
        this.mContext = context;
        this.mList = list;
        this.mInflater = LayoutInflater.from(context);
    }

    public void setData(ArrayList<VideoItem> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    public void addData(ArrayList<VideoItem> list) {
        if (this.mList == null) {
            this.mList = new ArrayList<VideoItem>();
        }
        this.mList.addAll(list);
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
            convertView = mInflater.inflate(R.layout.item_video, parent, false);
            holder = new ViewHolder();
            holder.ivCover = (ImageView) convertView.findViewById(R.id.iv_cover);
            holder.tvDuration = (TextView) convertView.findViewById(R.id.tv_duration);
            holder.tvTitle = (TextView) convertView.findViewById(R.id.tv_title);
            holder.tvOwner = (TextView) convertView.findViewById(R.id.tv_owner);
            holder.tvView = (TextView) convertView.findViewById(R.id.tv_view);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        VideoItem item = mList.get(position);
        holder.tvTitle.setText(item.title == null ? "" : item.title);
        holder.tvOwner.setText(item.ownerName == null ? "" : item.ownerName);
        holder.tvView.setText(TimeUtil.formatNumber(item.view) + " 播放 · "
                + TimeUtil.formatNumber(item.danmaku) + " 弹幕");
        holder.tvDuration.setText(TimeUtil.formatDuration(item.duration));

        if (item.pic != null && item.pic.length() > 0) {
            ImageLoader.display(holder.ivCover, item.pic, R.drawable.bg_cover);
        } else {
            holder.ivCover.setImageResource(R.drawable.bg_cover);
        }
        return convertView;
    }

    static class ViewHolder {
        ImageView ivCover;
        TextView tvDuration;
        TextView tvTitle;
        TextView tvOwner;
        TextView tvView;
    }
}
