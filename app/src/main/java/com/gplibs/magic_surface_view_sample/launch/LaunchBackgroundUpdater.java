package com.gplibs.magic_surface_view_sample.launch;

import com.gplibs.magic_surface_view_sample.common.FloatValueAnimator;
import com.gplibs.magicsurfaceview.MagicSurface;
import com.gplibs.magicsurfaceview.MagicSurfaceModelUpdater;
import com.gplibs.magicsurfaceview.ReusableVec;
import com.gplibs.magicsurfaceview.SurfaceModel;
import com.gplibs.magicsurfaceview.Vec;
import com.gplibs.magicsurfaceview.VecPool;


/**
 * 0~STEP1
 */
class LaunchBackgroundUpdater extends MagicSurfaceModelUpdater {

    private final float WAVE_LENGTH = 0.5f;

    private FloatValueAnimator mAnimator;
    private float mAnimValue = 0;

    private float mMaxRadius;
    private float mRatio = 0;
    private boolean mIsCompleted = false;

    LaunchBackgroundUpdater(FloatValueAnimator animator, int group) {
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
        mMaxRadius = getRadius(surface.getModel(), 0, 0);
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
        if (mIsCompleted) {
            return;
        }
        mRatio = 1;
        if (mAnimValue <= LaunchActivity.STEP1) {
            mRatio = mAnimValue / LaunchActivity.STEP1;
        }
        float startRadius = mRatio * (mMaxRadius + WAVE_LENGTH)- WAVE_LENGTH;
        float radius = getRadius(outPos);
        float offset = radius - startRadius;
        if (offset < 0) {
            outColor.setColor(0XFFFFFFFF);
        } else if (offset > WAVE_LENGTH) {
            outColor.a(0);
        } else {
            float ratio = offset / WAVE_LENGTH;
            float a = ratio - 0.6f;
            if (a < 0) {
                a = 1;
            } else {
                a = (0.4f - a) / 0.4f;
            }
            outColor.setRGBA(1 - ratio, 1 - ratio, 1, a);
        }
    }

    @Override
    protected void updateEnd(MagicSurface surface) {
        if (!mIsCompleted) {
            mIsCompleted = mRatio == 1;
        }
        if (mAnimator.isStopped()) {
            stop();
        }
    }

    private float getRadius(Vec pos) {
        return (float) Math.sqrt(pos.x() * pos.x() + pos.y() * pos.y());
    }

    private float getRadius(SurfaceModel model, int x, int y) {
        ReusableVec pos = VecPool.get(3);
        try {
            model.getPosition(x, y, pos);
            return (float) Math.sqrt(pos.x() * pos.x() + pos.y() * pos.y());
        } finally {
            pos.free();
        }
    }
}
