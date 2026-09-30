package com.hi.bili.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hi.bili.R;
import com.hi.bili.model.PageItem;
import com.hi.bili.util.TimeUtil;

import java.util.ArrayList;

/**
 * 分P列表适配器
 */
public class PageAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<PageItem> mList;
    private LayoutInflater mInflater;
    private int mSelected = 0;

    public PageAdapter(Context context, ArrayList<PageItem> list) {
        this.mContext = context;
        this.mList = list;
        this.mInflater = LayoutInflater.from(context);
    }

    public void setSelected(int position) {
        this.mSelected = position;
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
            convertView = mInflater.inflate(R.layout.item_page, parent, false);
            holder = new ViewHolder();
            holder.tvIndex = (TextView) convertView.findViewById(R.id.tv_page_index);
            holder.tvTitle = (TextView) convertView.findViewById(R.id.tv_page_title);
            holder.tvDuration = (TextView) convertView.findViewById(R.id.tv_page_duration);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        PageItem item = mList.get(position);
        holder.tvIndex.setText("P" + item.page);
        holder.tvTitle.setText(item.part == null ? "" : item.part);
        holder.tvDuration.setText(TimeUtil.formatDuration(item.duration));

        if (position == mSelected) {
            convertView.setBackgroundColor(0x22FB7299);
            holder.tvTitle.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
        } else {
            convertView.setBackgroundColor(Color.TRANSPARENT);
            holder.tvTitle.setTextColor(mContext.getResources().getColor(R.color.textPrimary));
        }
        return convertView;
    }

    static class ViewHolder {
        TextView tvIndex;
        TextView tvTitle;
        TextView tvDuration;
    }
}
