package com.gplibs.magic_surface_view_sample.launch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.gplibs.magic_surface_view_sample.common.MagicActivity;
import com.gplibs.magic_surface_view_sample.MainActivity;
import com.gplibs.magic_surface_view_sample.R;
import com.gplibs.magic_surface_view_sample.common.FloatValueAnimator;
import com.gplibs.magicsurfaceview.MagicScene;
import com.gplibs.magicsurfaceview.MagicSceneBuilder;
import com.gplibs.magicsurfaceview.MagicSurface;
import com.gplibs.magicsurfaceview.MagicUpdater;
import com.gplibs.magicsurfaceview.MagicUpdaterListener;
import com.gplibs.magicsurfaceview.PointLight;

/**
 * 环境光: 开始黑色                                                  STEP3~1 由黑变白色
 * 背景: 0~STEP1由中心开始逐渐显示
 * 光源: STEP1~STEP2 由黑变亮      STEP2~STEP3 从左到右               STEP3~1 逐渐消失（颜色变为0）
 * 文本:                          STEP2~STEP3 从左到右与光源同步显示
 * 按钮:                                                           STEP3~1 逐渐显示出来
 */
public class LaunchActivity extends MagicActivity {

    static final float STEP1 = 0.12f;
    static final float STEP2 = 0.3f;
    static final float STEP3 = 0.8f;

    private FloatValueAnimator mAnimator = new FloatValueAnimator(2000);

    private MagicScene mScene;
    private PointLight mLight = new PointLight(0XFFFFFFFF, 0, 0, 0.1f);
    private int endAnimatorCount = 0;

    private View mViewText;
    private Button mBtnEnter;

    // 各Updater运行时间段都是按时间线错开的，btnMatrixUpdater 和 btnModelUpdater是需要同步运行的，所以可以都分到一组，在一个线程运行。

    // 场景动画
    private LaunchSceneUpdater sceneUpdater = new LaunchSceneUpdater(mAnimator, 1);
    // 背景模型动画
    private LaunchBackgroundUpdater backgroundUpdater = new LaunchBackgroundUpdater(mAnimator, 1);
    // 文本模型动画
    private LaunchTextModelUpdater textModelUpdater = new LaunchTextModelUpdater(mAnimator, 1);
    // 按钮矩阵动画
    private LaunchButtonMatrixUpdater btnMatrixUpdater = new LaunchButtonMatrixUpdater(mAnimator, 1);
    // 按钮模型动画
    private LaunchButtonModelUpdater btnModelUpdater = new LaunchButtonModelUpdater(mAnimator, 1);

    private MagicUpdaterListener updaterListener = new MagicUpdaterListener() {
        @Override
        public void onStart() {

        }
        @Override
        public void onStop() {
            if (++endAnimatorCount == 5) { // 所有动画结束后调用
                // 显示真实页面 和 文本、按钮
                showPage();
                mBtnEnter.setVisibility(View.VISIBLE);
                mViewText.setVisibility(View.VISIBLE);

                // 隐藏 MagicSurfaceView
                getPageSurfaceView().setVisibility(View.GONE);

                // 动画完成释放场景资源
                mScene.release();
                mScene = null;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setPageBackground(R.color.white);
        setPageContentBg(R.color.white);
        hidePageTitleBar();
        hidePage(); // 起始页动画是个入场的过程，开始需要隐藏整个页面（隐藏必须使用INVISIBLE 不要使用 GONE）
        setContentView(R.layout.activity_launch);

        initViews();
        initMagicObjects();

        render();
    }

    private void initViews() {
        mViewText = findViewById(R.id.view_text);
        mBtnEnter = (Button) findViewById(R.id.btn_enter);
        mBtnEnter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LaunchActivity.this, MainActivity.class));
                mBtnEnter.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        finish();
                    }
                }, 1500);
            }
        });
        // 动画是个入场的过程，开始需要隐藏 (隐藏必须使用INVISIBLE 不要使用 GONE)
        mViewText.setVisibility(View.INVISIBLE);
        // 动画是个入场的过程，开始需要隐藏 (隐藏必须使用INVISIBLE 不要使用 GONE)
        mBtnEnter.setVisibility(View.INVISIBLE);
    }

    private void initMagicObjects() {
        backgroundUpdater.addListener(updaterListener);
        sceneUpdater.addListener(updaterListener);
        btnMatrixUpdater.addListener(updaterListener);
        textModelUpdater.addListener(updaterListener);
        btnModelUpdater.addListener(updaterListener);
    }

    /**
     * 返回 null 禁用Page转场动画
     */
    @Override
    protected MagicUpdater getPageUpdater(boolean isHide) {
        return null;
    }

    private void render() {
        MagicSurface bgSurface = new MagicSurface(getPageViewContainer())
                .setGrid(60, 60)
                .setModelUpdater(backgroundUpdater);

        MagicSurface textSurface = new MagicSurface(mViewText)
                .setGrid(2, 600)
                .setEnableDepthTest(false)
                .setModelUpdater(textModelUpdater)
                .setVisible(false);

        MagicSurface btnSurface = new MagicSurface(mBtnEnter)
                .setGrid(30, 5)
                .setEnableDepthTest(false)
                .setMatrixUpdater(btnMatrixUpdater)
                .setModelUpdater(btnModelUpdater)
                .setVisible(false);

        mScene = new MagicSceneBuilder(getPageSurfaceView())
                .ambientColor(0XFF000000)
                .addLights(mLight)
                .setUpdater(sceneUpdater)
                .addSurfaces(bgSurface, textSurface, btnSurface)
                .build();
        getPageSurfaceView().render(mScene);
    }
}
