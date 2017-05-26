package com.gplibs.magic_surface_view_sample.common;

import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.LayoutRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.gplibs.magic_surface_view_sample.R;
import com.gplibs.magic_surface_view_sample.updater.WaveAnimUpdater;
import com.gplibs.magicsurfaceview.MagicMultiSurface;
import com.gplibs.magicsurfaceview.MagicMultiSurfaceUpdater;
import com.gplibs.magicsurfaceview.MagicSurface;
import com.gplibs.magicsurfaceview.MagicSurfaceMatrixUpdater;
import com.gplibs.magicsurfaceview.MagicSurfaceModelUpdater;
import com.gplibs.magicsurfaceview.MagicSurfaceView;
import com.gplibs.magicsurfaceview.MagicUpdater;
import com.gplibs.magicsurfaceview.MagicUpdaterListener;

public abstract class MagicActivity extends AppCompatActivity {

    // 页面可视部分的根View
    private View mPageViewContainer;
    // 用于页面转场动画的 MagicSurfaceView
    private MagicSurfaceView mPageSurfaceView;
    // 页面TitleBar
    private View mPageTitleBar;
    // 页面内容容器
    private FrameLayout mFlPageContent;
    // Title TextView
    private TextView mTvPageTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_magic);
        mPageViewContainer = findViewById(R.id.page_view_container);
        mPageSurfaceView = (MagicSurfaceView) findViewById(R.id.page_surface_view);
        mPageTitleBar = findViewById(R.id.page_title_bar);
        mFlPageContent = (FrameLayout) findViewById(R.id.fl_page_content);
        mTvPageTitle = (TextView) findViewById(R.id.tv_page_title);

        // 进行入场动画
        if (!show()) {
            mPageViewContainer.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPageSurfaceView.onDestroy();
    }

    @Override
    public void onBackPressed() {
        // 进行离场动画
        if(!hide()) {
            finish();
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        mTvPageTitle.setText(title);
    }

    @Override
    public void setTitle(int titleId) {
        mTvPageTitle.setText(titleId);
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        LayoutInflater.from(this).inflate(layoutResID, mFlPageContent, true);
    }

    protected MagicSurfaceView getPageSurfaceView() {
        return mPageSurfaceView;
    }

    protected View getPageViewContainer() {
        return mPageViewContainer;
    }

    protected void showPage() {
        mPageViewContainer.setVisibility(View.VISIBLE);
    }

    protected void hidePage() {
        mPageViewContainer.setVisibility(View.INVISIBLE);
    }

    protected void hidePageTitleBar() {
        mPageTitleBar.setVisibility(View.GONE);
    }

    protected void setPageBackground(@ColorRes int colorResource) {
        mPageViewContainer.setBackgroundColor(ContextCompat.getColor(this, colorResource));
    }

    protected void setPageContentBg(@ColorRes int colorResource) {
        mFlPageContent.setBackgroundResource(colorResource);
    }

    /**
     * 获取页面转场动画对应的 Updater
     * @param isHide true:离场 false:入场
     * @return
     */
    protected MagicUpdater getPageUpdater(boolean isHide) {
        if (isHide) {
            return new WaveAnimUpdater(true, Direction.RIGHT, false);
        } else {
            return new WaveAnimUpdater(false, Direction.RIGHT, false);
        }
    }

    /**
     * 获取页面碎片化转场动画对应的 Updater
     * @param isHide true:离场 false:入场
     * @return
     */
    protected MagicMultiSurfaceUpdater getPageMultiUpdater(boolean isHide) {
        return null;
    }

    /**
     * 页面转场动画对应 SurfaceModel 行数
     * @return
     */
    protected int pageAnimRowCount() {
        return 30;
    }

    /**
     * 页面转场动画对应 SurfaceModel 列数
     * @return
     */
    protected int pageAnimColCount() {
        return 30;
    }

    /**
     * 页面转场动画入场动画完成后调用
     * @return
     */
    protected void onPageAnimEnd() {
    }

    /**
     * 开始入场动画
     * @return
     */
    private boolean show() {
        MagicUpdater updater = getPageUpdater(false);
        if (updater != null) {
            return showWithSurface(updater);
        }
        MagicMultiSurfaceUpdater multiUpdater = getPageMultiUpdater(false);
        if (multiUpdater != null) {
            return showWithMultiSurface(multiUpdater);
        }
        return false;
    }

    private boolean showWithSurface(MagicUpdater updater) {
        updater.addListener(new MagicUpdaterListener() {
            @Override
            public void onStart() {
                mPageViewContainer.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onStop() {
                mPageViewContainer.setVisibility(View.VISIBLE);
                mPageSurfaceView.setVisibility(View.GONE);
                // 动画完成释放资源
                mPageSurfaceView.release();
                onPageAnimEnd();
            }
        });
        final MagicSurface s = new MagicSurface(mPageViewContainer)
                .setGrid(pageAnimRowCount(), pageAnimColCount())
                .drawGrid(false);
        if (updater instanceof MagicSurfaceMatrixUpdater) {
            s.setMatrixUpdater((MagicSurfaceMatrixUpdater) updater);
        } else {
            s.setModelUpdater((MagicSurfaceModelUpdater) updater);
        }
        mPageSurfaceView.setVisibility(View.VISIBLE);
        mPageSurfaceView.render(s);
        return true;
    }

    private boolean showWithMultiSurface(MagicMultiSurfaceUpdater updater) {
        updater.addListener(new MagicUpdaterListener() {
            @Override
            public void onStart() {
                mPageViewContainer.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onStop() {
                mPageViewContainer.setVisibility(View.VISIBLE);
                mPageSurfaceView.setVisibility(View.GONE);
                // 动画完成释放资源
                mPageSurfaceView.release();
                onPageAnimEnd();
            }
        });
        final MagicMultiSurface s = new MagicMultiSurface(mPageViewContainer, pageAnimRowCount(), pageAnimColCount());
        s.setUpdater(updater);
        mPageSurfaceView.setVisibility(View.VISIBLE);
        mPageSurfaceView.render(s);
        return true;
    }


    /**
     * 开始离场动画
     * @return
     */
    private boolean hide() {
        MagicUpdater updater = getPageUpdater(true);
        if (updater != null) {
            return hideWithSurface(updater);
        }

        MagicMultiSurfaceUpdater multiUpdater = getPageMultiUpdater(true);
        if (multiUpdater != null) {
            return hideWithMultiSurface(multiUpdater);
        }
        return false;
    }

    private boolean hideWithSurface(MagicUpdater updater) {
        updater.addListener(new MagicUpdaterListener() {
            @Override
            public void onStart() {
                mPageViewContainer.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onStop() {
                mPageSurfaceView.setVisibility(View.GONE);
                // 动画完成释放资源
                mPageSurfaceView.release();
                finish();
            }
        });
        MagicSurface s = new MagicSurface(mPageViewContainer)
                .setGrid(pageAnimRowCount(), pageAnimColCount())
                .drawGrid(false);
        if (updater instanceof MagicSurfaceMatrixUpdater) {
            s.setMatrixUpdater((MagicSurfaceMatrixUpdater) updater);
        } else {
            s.setModelUpdater((MagicSurfaceModelUpdater) updater);
        }
        mPageSurfaceView.setVisibility(View.VISIBLE);
        mPageSurfaceView.render(s);
        return true;
    }

    private boolean hideWithMultiSurface(MagicMultiSurfaceUpdater updater) {
        updater.addListener(new MagicUpdaterListener() {
            @Override
            public void onStart() {
                mPageViewContainer.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onStop() {
                mPageSurfaceView.setVisibility(View.GONE);
                // 动画完成释放资源
                mPageSurfaceView.release();
                finish();
            }
        });
        final MagicMultiSurface s = new MagicMultiSurface(mPageViewContainer, pageAnimRowCount(), pageAnimColCount());
        s.setUpdater(updater);
        mPageSurfaceView.setVisibility(View.VISIBLE);
        mPageSurfaceView.render(s);
        return true;
    }

}
