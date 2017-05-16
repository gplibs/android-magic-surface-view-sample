package com.gplibs.magic_surface_view_sample;

import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.gplibs.magic_surface_view_sample.common.Direction;
import com.gplibs.magic_surface_view_sample.common.MagicActivity;
import com.gplibs.magic_surface_view_sample.updater.MacWindowAnimUpdater;
import com.gplibs.magicsurfaceview.MagicScene;
import com.gplibs.magicsurfaceview.MagicSceneBuilder;
import com.gplibs.magicsurfaceview.MagicSurface;
import com.gplibs.magicsurfaceview.MagicSurfaceView;
import com.gplibs.magicsurfaceview.MagicUpdater;
import com.gplibs.magicsurfaceview.MagicUpdaterListener;

public class MacWindowAnimActivity extends MagicActivity implements View.OnTouchListener, View.OnClickListener {

    private MagicSurfaceView mSurfaceView;
    private TextView mTvContent;

    private Button mBtnLeft;
    private Button mBtnTop;
    private Button mBtnRight;
    private Button mBtnBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mac_window_anim);
        setTitle("MacWindowAnim");

        mSurfaceView = (MagicSurfaceView) findViewById(R.id.surface_view);
        mTvContent = (TextView) findViewById(R.id.tv_content);

        mBtnLeft = (Button) findViewById(R.id.btn_left);
        mBtnTop = (Button) findViewById(R.id.btn_top);
        mBtnRight = (Button) findViewById(R.id.btn_right);
        mBtnBottom = (Button) findViewById(R.id.btn_bottom);

        mBtnLeft.setOnClickListener(this);
        mBtnTop.setOnClickListener(this);
        mBtnRight.setOnClickListener(this);
        mBtnBottom.setOnClickListener(this);

        findViewById(R.id.view_left_container).setOnTouchListener(this);
        findViewById(R.id.view_top_container).setOnTouchListener(this);
        findViewById(R.id.view_right_container).setOnTouchListener(this);
        findViewById(R.id.view_bottom_container).setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        boolean isVertical = false;
        Button btn = null;
        switch (v.getId()) {
            case R.id.view_left_container:
                btn = mBtnLeft;
                break;
            case R.id.view_top_container:
                btn = mBtnTop;
                isVertical = true;
                break;
            case R.id.view_right_container:
                btn = mBtnRight;
                break;
            case R.id.view_bottom_container:
                btn = mBtnBottom;
                isVertical = true;
                break;
        }
        updateBtnPosition(btn, isVertical, isVertical ? event.getX() : event.getY());
        return true;
    }

    @Override
    public void onClick(View v) {
        float center = 0.5f;
        int direction = Direction.BOTTOM;
        switch (v.getId()) {
            case R.id.btn_left:
                direction = Direction.LEFT;
                center = (v.getTop() + v.getHeight() / 2.f) / ((View)v.getParent()).getHeight();
                break;
            case R.id.btn_right:
                direction = Direction.RIGHT;
                center = (v.getTop() + v.getHeight() / 2.f) / ((View)v.getParent()).getHeight();
                break;
            case R.id.btn_top:
                direction = Direction.TOP;
                center = (v.getLeft() + v.getWidth() / 2.f) / ((View)v.getParent()).getWidth();
                break;
            case R.id.btn_bottom:
                direction = Direction.BOTTOM;
                center = (v.getLeft() + v.getWidth() / 2.f) / ((View)v.getParent()).getWidth();
                break;
        }
        if (isHide()) {
            show(center, direction);
        } else {
            hide(center, direction);
        }
    }
    
    private void hide(float center, int direction) {
        MacWindowAnimUpdater updater = new MacWindowAnimUpdater(true, direction, center, false);
        updater.addListener(new MagicUpdaterListener() {
            @Override
            public void onStart() {
                mTvContent.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onStop() {
                mSurfaceView.setVisibility(View.GONE);
                // 释放资源
                mSurfaceView.release();
            }
        });
        MagicSurface s = new MagicSurface(mTvContent)
                .setGrid(getRowLineCount(direction), getColLineCount(direction))
                .setModelUpdater(updater);
        MagicScene scene = new MagicSceneBuilder(mSurfaceView)
                .addSurfaces(s)
                .build();
        mSurfaceView.setVisibility(View.VISIBLE);
        mSurfaceView.render(scene);
    }

    private void show(float center, int direction) {
        MacWindowAnimUpdater updater = new MacWindowAnimUpdater(false, direction, center, false);
        updater.addListener(new MagicUpdaterListener() {
            @Override
            public void onStart() {
                mTvContent.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onStop() {
                mTvContent.setVisibility(View.VISIBLE);
                mSurfaceView.setVisibility(View.GONE);
                // 释放资源
                mSurfaceView.release();
            }
        });
        MagicSurface s = new MagicSurface(mTvContent)
                .setGrid(getRowLineCount(direction), getColLineCount(direction))
                .setModelUpdater(updater);
        mSurfaceView.setVisibility(View.VISIBLE);
        mSurfaceView.render(s);
    }

    private int getRowLineCount(int direction) {
        return Direction.isVertical(direction) ? 20 : 8;
    }

    private int getColLineCount(int direction) {
        return Direction.isVertical(direction) ? 8 : 20;
    }

    private boolean isHide() {
        return mTvContent.getVisibility() != View.VISIBLE;
    }

    private void updateBtnPosition(Button btn, boolean isVertical, float value) {
        if (value < 0) {
            value = 0;
        }
        View parent = (View) btn.getParent();
        float max = isVertical ? parent.getWidth() : parent.getHeight();
        if (value > max) {
            value = max;
        }
        FrameLayout.LayoutParams p = (FrameLayout.LayoutParams) btn.getLayoutParams();
        if (isVertical) {
            p.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
            p.leftMargin = (int) (value - btn.getWidth() / 2.0);
        } else {
            p.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            p.topMargin = (int) (value - btn.getHeight() / 2.0);
        }
        btn.setLayoutParams(p);
    }


    // 设置Page转场动画
    @Override
    protected MagicUpdater getPageUpdater(boolean isHide) {
        return new MacWindowAnimUpdater(isHide, Direction.RIGHT, 0.184f, false);
    }

}
