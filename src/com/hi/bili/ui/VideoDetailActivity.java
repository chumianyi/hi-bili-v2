package com.hi.bili.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.hi.bili.R;
import com.hi.bili.adapter.CommentAdapter;
import com.hi.bili.adapter.PageAdapter;
import com.hi.bili.core.BiliApi;
import com.hi.bili.core.HistoryDB;
import com.hi.bili.core.ImageLoader;
import com.hi.bili.model.CommentItem;
import com.hi.bili.model.PageItem;
import com.hi.bili.model.VideoDetail;
import com.hi.bili.model.VideoItem;
import com.hi.bili.util.JsonUtil;
import com.hi.bili.util.TimeUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 视频详情：封面/标题/UP主/数据/简介/分P/互动/评论。
 */
public class VideoDetailActivity extends BaseActivity {

    private String mBvid;
    private VideoDetail mDetail;

    private ImageView ivCover, ivOwnerFace;
    private TextView tvTitle, tvOwnerName, tvFollow;
    private TextView tvStatView, tvStatDanmaku, tvStatReply, tvStatFav, tvStatCoin, tvStatShare;
    private TextView tvDesc, tvDescToggle;
    private ListView listPages, listComments;
    private PageAdapter mPageAdapter;
    private CommentAdapter mCommentAdapter;
    private ArrayList<PageItem> mPages = new ArrayList<PageItem>();
    private ArrayList<CommentItem> mComments = new ArrayList<CommentItem>();
    private int mCommentNext = 1;
    private boolean mDescExpanded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_detail);

        mBvid = getIntent().getStringExtra("bvid");
        bindViews();
        loadDetail();
    }

    private void bindViews() {
        ivCover = (ImageView) findViewById(R.id.iv_cover);
        ivOwnerFace = (ImageView) findViewById(R.id.iv_owner_face);
        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvOwnerName = (TextView) findViewById(R.id.tv_owner_name);
        tvFollow = (TextView) findViewById(R.id.btn_follow);
        tvStatView = (TextView) findViewById(R.id.tv_stat_view);
        tvStatDanmaku = (TextView) findViewById(R.id.tv_stat_danmaku);
        tvStatReply = (TextView) findViewById(R.id.tv_stat_reply);
        tvStatFav = (TextView) findViewById(R.id.tv_stat_favorite);
        tvStatCoin = (TextView) findViewById(R.id.tv_stat_coin);
        tvStatShare = (TextView) findViewById(R.id.tv_stat_share);
        tvDesc = (TextView) findViewById(R.id.tv_desc);
        tvDescToggle = (TextView) findViewById(R.id.tv_desc_toggle);
        listPages = (ListView) findViewById(R.id.list_pages);
        listComments = (ListView) findViewById(R.id.list_comments);

        findViewById(R.id.btn_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playCurrentPage();
            }
        });
        tvFollow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("关注功能需登录后使用");
            }
        });
        tvDescToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDescExpanded = !mDescExpanded;
                tvDesc.setMaxLines(mDescExpanded ? Integer.MAX_VALUE : 3);
                tvDescToggle.setText(mDescExpanded ? R.string.desc_fold : R.string.desc_more);
            }
        });
        findViewById(R.id.btn_like).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doLike();
            }
        });
        findViewById(R.id.btn_coin).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCoinDialog();
            }
        });
        findViewById(R.id.btn_favorite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doFavorite();
            }
        });
        findViewById(R.id.btn_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showToast("已复制链接(演示)");
            }
        });

        mPageAdapter = new PageAdapter(this, mPages);
        listPages.setAdapter(mPageAdapter);
        listPages.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mPageAdapter.setSelected(position);
                PageItem p = mPages.get(position);
                startPlayer(mDetail.aid, p.cid, mDetail.bvid, mDetail.title);
            }
        });

        mCommentAdapter = new CommentAdapter(this, mComments);
        listComments.setAdapter(mCommentAdapter);
    }

    private void loadDetail() {
        showLoading(getString(R.string.loading));
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String json = BiliApi.getVideoView(mBvid);
                    JSONObject root = new JSONObject(json);
                    JSONObject data = root.optJSONObject("data");
                    final VideoDetail detail = JsonUtil.parseVideoDetail(data);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            hideLoading();
                            mDetail = detail;
                            fillUi();
                            loadComments();
                            // 写入观看历史
                            try {
                                HistoryDB.addHistory(toVideoItem(mDetail));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (final Exception e) {
                    e.printStackTrace();
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            hideLoading();
                            showToast(getString(R.string.load_failed));
                        }
                    });
                }
            }
        }).start();
    }

    private void fillUi() {
        if (mDetail == null) return;
        tvTitle.setText(mDetail.title == null ? "" : mDetail.title);
        tvOwnerName.setText(mDetail.ownerName == null ? "" : mDetail.ownerName);
        tvStatView.setText(TimeUtil.formatNumber(mDetail.view));
        tvStatDanmaku.setText(TimeUtil.formatNumber(mDetail.danmaku));
        tvStatReply.setText(TimeUtil.formatNumber(mDetail.reply));
        tvStatFav.setText(TimeUtil.formatNumber(mDetail.favorite));
        tvStatCoin.setText(TimeUtil.formatNumber(mDetail.coin));
        tvStatShare.setText(TimeUtil.formatNumber(mDetail.share));
        tvDesc.setText(mDetail.desc == null ? "" : mDetail.desc);

        if (mDetail.pic != null && mDetail.pic.length() > 0) {
            ImageLoader.display(ivCover, mDetail.pic, R.drawable.bg_cover);
        }
        if (mDetail.ownerFace != null && mDetail.ownerFace.length() > 0) {
            ImageLoader.display(ivOwnerFace, mDetail.ownerFace, R.drawable.bg_circle_gray);
        }

        mPages.clear();
        if (mDetail.pages != null) mPages.addAll(mDetail.pages);
        mPageAdapter.notifyDataSetChanged();
    }

    private void playCurrentPage() {
        if (mDetail == null) return;
        String cid = mDetail.cid;
        if (!mPages.isEmpty()) cid = mPages.get(0).cid;
        startPlayer(mDetail.aid, cid, mDetail.bvid, mDetail.title);
    }

    private void startPlayer(String avid, String cid, String bvid, String title) {
        Intent it = new Intent(this, PlayerActivity.class);
        it.putExtra("avid", avid);
        it.putExtra("cid", cid);
        it.putExtra("bvid", bvid);
        it.putExtra("title", title);
        startActivity(it);
    }

    private void loadComments() {
        if (mDetail == null) return;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String json = BiliApi.getComments(mDetail.aid, mCommentNext);
                    JSONObject root = new JSONObject(json);
                    JSONObject data = root.optJSONObject("data");
                    JSONArray replies = data == null ? null : data.optJSONArray("replies");
                    final ArrayList<CommentItem> list = new ArrayList<CommentItem>();
                    if (replies != null) {
                        for (int i = 0; i < replies.length(); i++) {
                            JSONObject o = replies.optJSONObject(i);
                            if (o != null) list.add(JsonUtil.parseCommentItem(o));
                        }
                    }
                    mCommentNext++;
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mComments.addAll(list);
                            mCommentAdapter.notifyDataSetChanged();
                            findViewById(R.id.tv_comment_empty).setVisibility(
                                    mComments.isEmpty() ? View.VISIBLE : View.GONE);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void doLike() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BiliApi.likeVideo(mBvid, 1);
                    showToast("点赞成功");
                } catch (Exception e) {
                    e.printStackTrace();
                    showToast("点赞失败，请先登录");
                }
            }
        }).start();
    }

    private void showCoinDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.coin)
                .setItems(new String[]{"投 1 枚", "投 2 枚"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int which) {
                        final int count = which == 0 ? 1 : 2;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    BiliApi.addCoin(mBvid, count);
                                    showToast("投币成功");
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    showToast("投币失败，请先登录");
                                }
                            }
                        }).start();
                    }
                }).show();
    }

    private void doFavorite() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BiliApi.dealFavorite(mDetail == null ? "" : mDetail.aid, "", "");
                    showToast("收藏成功");
                } catch (Exception e) {
                    e.printStackTrace();
                    showToast("收藏失败，请先登录");
                }
            }
        }).start();
    }

    private VideoItem toVideoItem(VideoDetail d) {
        VideoItem vi = new VideoItem();
        vi.bvid = d.bvid;
        vi.aid = d.aid;
        vi.cid = d.cid;
        vi.title = d.title;
        vi.pic = d.pic;
        vi.desc = d.desc;
        vi.ownerName = d.ownerName;
        vi.ownerFace = d.ownerFace;
        vi.view = d.view;
        vi.danmaku = d.danmaku;
        vi.duration = d.duration;
        return vi;
    }
}
