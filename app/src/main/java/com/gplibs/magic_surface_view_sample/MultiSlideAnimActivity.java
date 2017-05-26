package com.gplibs.magic_surface_view_sample;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.gplibs.magic_surface_view_sample.common.Direction;
import com.gplibs.magic_surface_view_sample.common.MagicActivity;
import com.gplibs.magic_surface_view_sample.updater.MultiSlideUpdater;
import com.gplibs.magicsurfaceview.MagicMultiSurface;
import com.gplibs.magicsurfaceview.MagicMultiSurfaceUpdater;
import com.gplibs.magicsurfaceview.MagicSurfaceView;
import com.gplibs.magicsurfaceview.MagicUpdater;
import com.gplibs.magicsurfaceview.MagicUpdaterListener;

public class MultiSlideAnimActivity extends MagicActivity {

    private MagicSurfaceView mSurfaceView;
    private TextView mTvTest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_slide_anim);
        setTitle("MultiSlideAnim");

        mSurfaceView = (MagicSurfaceView) findViewById(R.id.magic_surface_view);
        mTvTest = (TextView) findViewById(R.id.tv_test);
        findViewById(R.id.view_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mTvTest.getVisibility() == View.VISIBLE) {
                    hide();
                } else {
                    show();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSurfaceView.onDestroy();
    }

    private void show() {
        MultiSlideUpdater updater = new MultiSlideUpdater(false, Direction.BOTTOM);
        updater.addListener(new MagicUpdaterListener() {
            @Override
            public void onStart() {
                mTvTest.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onStop() {
                mSurfaceView.setVisibility(View.GONE);
                mTvTest.setVisibility(View.VISIBLE);
                mSurfaceView.release();
            }
        });
        mSurfaceView.render(new MagicMultiSurface(mTvTest, 150, 1).setUpdater(updater));
    }

    private void hide() {
        MultiSlideUpdater updater = new MultiSlideUpdater(true, Direction.BOTTOM);
        updater.addListener(new MagicUpdaterListener() {
            @Override
            public void onStart() {
                mTvTest.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onStop() {
                mSurfaceView.setVisibility(View.GONE);
                mTvTest.setVisibility(View.INVISIBLE);
                mSurfaceView.release();
            }
        });
        mSurfaceView.render(new MagicMultiSurface(mTvTest, 150, 1).setUpdater(updater));
    }

    // 返回null禁用 单Surface转场动画
    @Override
    protected MagicUpdater getPageUpdater(boolean isHide) {
        return null;
    }

    // 启用多Surface转场动画
    @Override
    protected MagicMultiSurfaceUpdater getPageMultiUpdater(boolean isHide) {
        return new MultiSlideUpdater(isHide, Direction.TOP);
    }

    // 纵向surface数量
    @Override
    protected int pageAnimRowCount() {
        return 150;
    }

    // 横向surface数量
    @Override
    protected int pageAnimColCount() {
        return 1;
    }
}
