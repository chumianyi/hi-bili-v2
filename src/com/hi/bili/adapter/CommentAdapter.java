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
import com.hi.bili.model.CommentItem;
import com.hi.bili.util.TimeUtil;

import java.util.ArrayList;

/**
 * 评论列表适配器
 */
public class CommentAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<CommentItem> mList;
    private LayoutInflater mInflater;

    public CommentAdapter(Context context, ArrayList<CommentItem> list) {
        this.mContext = context;
        this.mList = list;
        this.mInflater = LayoutInflater.from(context);
    }

    public void addData(ArrayList<CommentItem> list) {
        if (this.mList == null) {
            this.mList = new ArrayList<CommentItem>();
        }
        this.mList.addAll(list);
        notifyDataSetChanged();
    }

    public void setData(ArrayList<CommentItem> list) {
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
            convertView = mInflater.inflate(R.layout.item_comment, parent, false);
            holder = new ViewHolder();
            holder.ivAvatar = (ImageView) convertView.findViewById(R.id.iv_comment_avatar);
            holder.tvName = (TextView) convertView.findViewById(R.id.tv_comment_name);
            holder.tvContent = (TextView) convertView.findViewById(R.id.tv_comment_content);
            holder.tvTime = (TextView) convertView.findViewById(R.id.tv_comment_time);
            holder.tvLike = (TextView) convertView.findViewById(R.id.tv_comment_like);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        CommentItem item = mList.get(position);
        holder.tvName.setText(item.uname == null ? "" : item.uname);
        holder.tvContent.setText(item.content == null ? "" : item.content);
        holder.tvTime.setText(TimeUtil.formatDate(item.ctime));
        holder.tvLike.setText(String.valueOf(item.like));

        if (item.avatar != null && item.avatar.length() > 0) {
            ImageLoader.display(holder.ivAvatar, item.avatar, R.drawable.bg_circle_gray);
        } else {
            holder.ivAvatar.setImageResource(R.drawable.bg_circle_gray);
        }
        return convertView;
    }

    static class ViewHolder {
        ImageView ivAvatar;
        TextView tvName;
        TextView tvContent;
        TextView tvTime;
        TextView tvLike;
    }
}
