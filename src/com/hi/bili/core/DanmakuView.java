package com.hi.bili.core;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;

import com.hi.bili.model.DanmakuItem;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * 弹幕叠加 View。
 * - 滚动弹幕(类型1)：从右向左滚动
 * - 底部弹幕(类型2)：固定在底部显示数秒
 * - 顶部弹幕(类型3)：固定在顶部显示数秒
 */
public class DanmakuView extends View {

    private static final float SCROLL_SPEED_DP = 120f; // 滚动速度 px/s 基准
    private static final float STABLE_DURATION = 4f;   // 顶部/底部停留秒数

    private ArrayList<DanmakuItem> danmakuList = new ArrayList<DanmakuItem>();
    private final ArrayList<ActiveDanmaku> activeScroll = new ArrayList<ActiveDanmaku>();
    private final ArrayList<ActiveDanmaku> activeFixed = new ArrayList<ActiveDanmaku>();

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float currentTime = 0f;
    private boolean enabled = true;
    private float textSizeSp = 16f;
    private long lastFrameMs = 0L;
    private float density = 1f;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable frameRunnable = new Runnable() {
        @Override
        public void run() {
            updateFrame();
            invalidate();
            handler.postDelayed(this, 33); // ~30fps
        }
    };

    private static class ActiveDanmaku {
        DanmakuItem item;
        float x;        // 滚动弹幕当前 x
        float y;        // 滚动/固定弹幕 y
        long bornMs;    // 固定弹幕出生时间
        boolean top;    // 是否顶部
        float textW;
    }

    public DanmakuView(Context ctx) {
        super(ctx);
        init(ctx);
    }

    public DanmakuView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        init(ctx);
    }

    private void init(Context ctx) {
        density = ctx.getResources().getDisplayMetrics().density;
        paint.setColor(Color.WHITE);
        paint.setTextSize(textSizeSp * density);
        paint.setShadowLayer(4f, 1f, 1f, Color.BLACK);
        handler.post(frameRunnable);
    }

    public void setDanmakuList(ArrayList<DanmakuItem> list) {
        this.danmakuList = list != null ? list : new ArrayList<DanmakuItem>();
        activeScroll.clear();
        activeFixed.clear();
    }

    /** 播放器回调更新当前播放时间(秒) */
    public void setTime(float currentTimeSec) {
        float delta = currentTimeSec - this.currentTime;
        // 进度大幅回退时，清理活动弹幕，避免残留
        if (delta < -1f) {
            activeScroll.clear();
            activeFixed.clear();
        }
        this.currentTime = currentTimeSec;
        spawnNewDanmaku();
    }

    public void setDanmakuEnabled(boolean enabled) {
        this.enabled = enabled;
        setVisibility(enabled ? VISIBLE : GONE);
    }

    public boolean isDanmakuEnabled() {
        return enabled;
    }

    public void setTextSize(float sp) {
        this.textSizeSp = sp;
        paint.setTextSize(sp * density);
    }

    public void release() {
        handler.removeCallbacks(frameRunnable);
        danmakuList.clear();
        activeScroll.clear();
        activeFixed.clear();
    }

    /** 根据当前时间点生成应该出现的弹幕 */
    private void spawnNewDanmaku() {
        int w = getWidth();
        if (w <= 0) w = 1080;
        for (int i = 0; i < danmakuList.size(); i++) {
            DanmakuItem d = danmakuList.get(i);
            // 时间戳落在当前帧窗口内则生成
            if (d.time <= currentTime && d.time > currentTime - 0.2f) {
                ActiveDanmaku a = new ActiveDanmaku();
                a.item = d;
                a.textW = paint.measureText(d.content);
                if (d.type == 2 || d.type == 3) {
                    a.top = (d.type == 3);
                    a.bornMs = System.currentTimeMillis();
                    activeFixed.add(a);
                } else {
                    // 滚动：不同轨道，避免全叠在一起
                    a.x = w;
                    a.y = pickScrollY();
                    activeScroll.add(a);
                }
            }
        }
    }

    private float pickScrollY() {
        int h = getHeight();
        if (h <= 0) h = 400;
        // 在弹幕区上半部分随机轨道
        float areaH = h * 0.6f;
        float lane = (float) (Math.random() * 6);
        return lane * (textSizeSp * density * 1.6f) + textSizeSp * density;
    }

    private void updateFrame() {
        long now = System.currentTimeMillis();
        if (lastFrameMs == 0L) lastFrameMs = now;
        float dt = (now - lastFrameMs) / 1000f;
        lastFrameMs = now;

        float speed = SCROLL_SPEED_DP * density;
        int w = getWidth();

        Iterator<ActiveDanmaku> it = activeScroll.iterator();
        while (it.hasNext()) {
            ActiveDanmaku a = it.next();
            a.x -= speed * dt;
            if (a.x + a.textW < 0) {
                it.remove();
            }
        }

        // 固定弹幕超时移除
        Iterator<ActiveDanmaku> fit = activeFixed.iterator();
        while (fit.hasNext()) {
            ActiveDanmaku a = fit.next();
            if ((now - a.bornMs) / 1000f > STABLE_DURATION) {
                fit.remove();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!enabled) return;
        int h = getHeight();
        int w = getWidth();

        // 滚动弹幕
        for (int i = 0; i < activeScroll.size(); i++) {
            ActiveDanmaku a = activeScroll.get(i);
            paint.setColor(parseColor(a.item.color));
            canvas.drawText(a.item.content, a.x, a.y, paint);
        }

        // 顶部/底部固定弹幕（逐行排布）
        int topLine = 0;
        int bottomLine = 0;
        for (int i = 0; i < activeFixed.size(); i++) {
            ActiveDanmaku a = activeFixed.get(i);
            paint.setColor(parseColor(a.item.color));
            float cx = (w - a.textW) / 2f;
            float y;
            if (a.top) {
                y = (topLine + 1) * (textSizeSp * density * 1.6f);
                topLine++;
            } else {
                y = h - (bottomLine + 1) * (textSizeSp * density * 1.6f) + 10f;
                bottomLine++;
            }
            canvas.drawText(a.item.content, cx, y, paint);
        }
    }

    private int parseColor(String hex) {
        try {
            if (hex == null || hex.length() == 0) return Color.WHITE;
            return Color.parseColor(hex);
        } catch (Exception e) {
            return Color.WHITE;
        }
    }
}
