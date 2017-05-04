package com.gplibs.magic_surface_view_sample.launch;

import com.gplibs.magic_surface_view_sample.common.FloatValueAnimator;
import com.gplibs.magicsurfaceview.MagicSurface;
import com.gplibs.magicsurfaceview.MagicSurfaceModelUpdater;
import com.gplibs.magicsurfaceview.Vec;

/**
 * STEP2~STEP3
 */
class LaunchTextModelUpdater extends MagicSurfaceModelUpdater {

    private FloatValueAnimator mAnimator;
    private float mAnimValue = 0;
    private float mRatio = 0;
    private boolean mIsCompleted;
    private float mLeft;

    LaunchTextModelUpdater(FloatValueAnimator animator, int group) {
        super(group);
        mAnimator = animator;
        mAnimator.addListener(new FloatValueAnimator.FloatValueAnimatorListener() {
            @Override
            public void onAnimationUpdate(float value) {
                mAnimValue = value;
                // 通知框架，数据改变，可以调用 update 方法进行更新
                notifyChanged();
            }

            @Override
            public void onStop() {
                // 通知框架，数据改变，可以调用 update 方法进行更新
                notifyChanged();
            }
        });
    }

    // 在绘制第一帧之前调用 (可以在此方法里进行一些初始化操作)
    @Override
    protected void willStart(MagicSurface surface) {
        Vec pos = new Vec(3);
        surface.getModel().getPosition(0, 0, pos);
        mLeft = pos.x();
    }

    // 在开始绘制后调用（绘制第一帧后调用，一般动画可以在此开始）
    @Override
    protected void didStart(MagicSurface surface) {
        mAnimator.start(false);
    }

    // 当调用Updater 的 stop() 方法之后，真正停止后会回调此方法
    @Override
    protected void didStop(MagicSurface surface) {
        mAnimator.stop();
    }

    @Override
    protected void updateBegin(MagicSurface surface) {
    }

    /**
     * 修改网格模型 r行, c列处 的坐标及颜色， 修改后的值存到 outPos 和 outColor
     * (只需要修改网格模型各行各列点及可，点与点之间的坐标和颜色由 openGL 自动进行插值计算完成)
     * 注：此方法执行频率非常高；一般不要有分配新的堆内存的逻辑，会频繁产生gc操作影响性能,可以使用全局变量或者ReusableVec、VecPool
     *
     * @param surface
     * @param r 行
     * @param c 列
     * @param outPos 默认值为 r行c列点包含偏移量的原始坐标, 计算完成后的新坐标要更新到此变量
     * @param outColor 默认值为 rgba(1,1,1,1), 计算完成后的新颜色要更新到此变量
     */
    @Override
    protected void updatePosition(MagicSurface surface, int r, int c, Vec outPos, Vec outColor) {
        if (mAnimValue > LaunchActivity.STEP2 && !mIsCompleted) {
            mRatio = (mAnimValue - LaunchActivity.STEP2) / (LaunchActivity.STEP3 - LaunchActivity.STEP2);
            if (mRatio > 1) {
                mRatio = 1;
            }
            float centerX = mLeft + mRatio * surface.getModel().getWidth();
            float offset = centerX - outPos.x();
            outColor.a(offset <= 0 ? 0 : 1);
        }
    }

    @Override
    protected void updateEnd(MagicSurface surface) {
        if(mAnimValue > LaunchActivity.STEP2 && !mIsCompleted) {
            surface.setVisible(true);
        }
        mIsCompleted = mRatio == 1;
        if (mAnimator.isStopped()) {
            stop();
        }
    }
}
