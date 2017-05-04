package com.gplibs.magic_surface_view_sample.launch;

import com.gplibs.magic_surface_view_sample.common.FloatValueAnimator;
import com.gplibs.magicsurfaceview.MagicSurface;
import com.gplibs.magicsurfaceview.MagicSurfaceMatrixUpdater;
import com.gplibs.magicsurfaceview.Vec;

/**
 * STEP3~1
 */
class LaunchButtonMatrixUpdater extends MagicSurfaceMatrixUpdater {

    private FloatValueAnimator mAnimator;
    private float mAnimValue = 0;
    private Vec mAxis = new Vec(3);
    private Vec mScale = new Vec(3);

    LaunchButtonMatrixUpdater(FloatValueAnimator animator, int group) {
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
        mAxis.setXYZ(-1, 0, 0);
    }

    // 在开始绘制后调用（绘制第一帧后调用，一般动画可以在此开始）
    @Override
    protected void didStart(MagicSurface surface) {
        mAnimator.start(false);
    }

    // 当调用Updater 的 stop() 方法之后，真正停止后会回调此方法
    @Override
    protected void didStop(MagicSurface surface) {
    }

    /**
     * 在此方法里进行矩阵变换
     *
     * @param offset offse为模型相对场景中心的坐标偏移量, 如果不进行 offset 位移， model 就会显示在场景中心；
     *
     *               当使用 View 构造 MagicSurface 时，
     *               View中心位置 相对 MagicSurfaceView中心位置的坐标偏移量 在场景坐标系中的表现就是 offset。
     *
     * @param matrix 矩阵
     */
    @Override
    protected void updateMatrix(MagicSurface surface, Vec offset, float[] matrix) {
        if (mAnimValue > LaunchActivity.STEP3) {
            reset(matrix);
            float ratio = (mAnimValue - LaunchActivity.STEP3) / (1 - LaunchActivity.STEP3);
            float angle = 360 * (1 - ratio);
            offset.z(-surface.getModel().getHeight());
            // 平移 要放在 旋转操作 前
            translate(matrix, offset);
            rotate(matrix, mAxis, angle);
            mScale.setXYZ(ratio, ratio, ratio);
            scale(matrix, mScale);
        }
        if (mAnimator.isStopped()) {
            stop();
        }
    }
}
