package com.gplibs.magic_surface_view_sample.launch;

import com.gplibs.magic_surface_view_sample.common.FloatValueAnimator;
import com.gplibs.magicsurfaceview.MagicScene;
import com.gplibs.magicsurfaceview.MagicSceneUpdater;
import com.gplibs.magicsurfaceview.MagicSurface;
import com.gplibs.magicsurfaceview.PointLight;
import com.gplibs.magicsurfaceview.Vec;

class LaunchSceneUpdater extends MagicSceneUpdater {

    private FloatValueAnimator mAnimator;
    private float mAnimValue = 0;

    private float mLightColor = 1.0f;       // 灯光最亮颜色值
    private Vec mBeginPoint = new Vec(3);   // 开始移动的坐标
    private Vec mCurrentPoint = new Vec(3); // 当前移动到的坐标

    LaunchSceneUpdater(FloatValueAnimator animator, int group) {
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
    protected void willStart(MagicScene scene) {
        // 获取文本Surface; 文本Surface 是在构造MagicScene时第二个添加，所以可以通过 scene.getSurface(1) 获取.
        MagicSurface surface = scene.getSurface(1);
        surface.getModel().getPosition(0, 0, mBeginPoint);
        mBeginPoint.y(mBeginPoint.y() - surface.getModel().getHeight() / 2);
        mBeginPoint.z(0.14f);

        Vec pos = new Vec(3);
        scene.getCameraPos(pos);
        float y = mBeginPoint.y() - pos.y();
        mBeginPoint.y(mBeginPoint.y() + 0.14f * y / pos.z());
    }

    // 在开始绘制后调用（绘制第一帧后调用，一般动画可以在此开始）
    @Override
    protected void didStart(MagicScene scene) {
        mAnimator.start(false);
    }

    // 当调用Updater 的 stop() 方法之后，真正停止后会回调此方法
    @Override
    protected void didStop(MagicScene scene) {
        mAnimator.stop();
    }

    // 更新环境光及灯光
    @Override
    protected void update(MagicScene scene, Vec outAmbientColor) {
        PointLight light = scene.getLight(0);
        if (mAnimValue < LaunchActivity.STEP1) {
            // 0~STEP1 不使用灯光

            if (light.isEnable()) {
                light.setEnable(false);
            }
        } else if (mAnimValue >= LaunchActivity.STEP1 && mAnimValue <= LaunchActivity.STEP2) {
            // STEP1~STEP2 灯光逐渐变亮

            if (!light.isEnable()) {
                light.setEnable(true);
            }
            float c = mLightColor * (mAnimValue - 0.2f) / 0.1f;
            light.setColor(c, c, c, 1.f);
            light.setPosition(mBeginPoint);
        } else if (mAnimValue < LaunchActivity.STEP3) {
            // STEP2~STEP3 移动灯光

            if (light.getColor().r() != mLightColor) {
                light.setColor(mLightColor, mLightColor, mLightColor, 1.f);
            }
            float r = (mAnimValue - LaunchActivity.STEP2) / (LaunchActivity.STEP3 - LaunchActivity.STEP2);
            mCurrentPoint.copy(mBeginPoint);
            mCurrentPoint.x(mCurrentPoint.x() + r * scene.getWidth());
            light.setPosition(mCurrentPoint);
        } else if (mAnimValue >= LaunchActivity.STEP3) {
            // STEP3~1 灯光变暗，环境光变亮

            float r = (mAnimValue - LaunchActivity.STEP3) / (1 - LaunchActivity.STEP3);
            outAmbientColor.setRGBA(r, r, r, 1.f);
            float c = mLightColor * (1 - r);
            light.setColor(c, c, c, 1.f);
        }

        // 动画停止时 调用stop()停止Updater
        if (mAnimator.isStopped()) {
            stop();
        }
    }
}
