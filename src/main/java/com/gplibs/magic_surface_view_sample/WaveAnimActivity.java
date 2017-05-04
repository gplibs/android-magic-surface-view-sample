package com.gplibs.magic_surface_view_sample;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.gplibs.magic_surface_view_sample.common.Direction;
import com.gplibs.magic_surface_view_sample.common.MagicActivity;
import com.gplibs.magic_surface_view_sample.updater.WaveAnimUpdater;
import com.gplibs.magicsurfaceview.MagicScene;
import com.gplibs.magicsurfaceview.MagicSurface;
import com.gplibs.magicsurfaceview.MagicUpdaterListener;
import com.gplibs.magicsurfaceview.MagicSurfaceView;

public class WaveAnimActivity extends MagicActivity {

    private MagicScene mScene;
    private MagicSurfaceView mSurfaceView;
    private TextView mTvContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wave_anim);
        setTitle("WaveAnim");

        mSurfaceView = (MagicSurfaceView) findViewById(R.id.surface_view);
        mTvContent = (TextView) findViewById(R.id.tv_content);
        findViewById(R.id.view_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTvContent.getVisibility() == View.VISIBLE) {
                    hide(Direction.RIGHT);
                } else {
                    show(Direction.RIGHT);
                }
            }
        });
    }

    private void show(int direction) {
        WaveAnimUpdater updater = new WaveAnimUpdater(false, direction);
        updater.addListener(new MagicUpdaterListener() {
            @Override
            public void onStart() {
                mTvContent.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onStop() {
                mTvContent.setVisibility(View.VISIBLE);
                mSurfaceView.setVisibility(View.GONE);
                // 释放场景资源
                mScene.release();
                mScene = null;
            }
        });
        MagicSurface s = new MagicSurface(mTvContent)
                .setGrid(getRowLineCount(direction), getColLineCount(direction))
                .drawGrid(false)
                .setModelUpdater(updater);
        mSurfaceView.setVisibility(View.VISIBLE);
        mScene = mSurfaceView.render(s);
    }

    private void hide(int direction) {
        WaveAnimUpdater updater = new WaveAnimUpdater(true, direction);
        updater.addListener(new MagicUpdaterListener() {
            @Override
            public void onStart() {
                mTvContent.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onStop() {
                mSurfaceView.setVisibility(View.GONE);
                // 释放场景资源
                mScene.release();
                mScene = null;
            }
        });
        MagicSurface s = new MagicSurface(mTvContent)
                .setGrid(getRowLineCount(direction), getColLineCount(direction))
                .drawGrid(false)
                .setModelUpdater(updater);
        mSurfaceView.setVisibility(View.VISIBLE);
        mScene = mSurfaceView.render(s);
    }

    private int getRowLineCount(int direction) {
        return Direction.isVertical(direction) ? 50 : 8;
    }

    private int getColLineCount(int direction) {
        return Direction.isVertical(direction) ? 8 : 50;
    }
}
