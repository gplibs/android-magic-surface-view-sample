package com.gplibs.magic_surface_view_sample.updater;

import com.gplibs.magic_surface_view_sample.common.FloatValueAnimator;
import com.gplibs.magicsurfaceview.MagicSurface;
import com.gplibs.magicsurfaceview.MagicSurfaceModelUpdater;
import com.gplibs.magicsurfaceview.ReusableVec;
import com.gplibs.magicsurfaceview.SurfaceModel;
import com.gplibs.magicsurfaceview.Vec;
import com.gplibs.magicsurfaceview.VecPool;

/**
 * 旋涡动画
 */
public class VortexAnimUpdater extends MagicSurfaceModelUpdater {

    private FloatValueAnimator mAnimator;
    private float mAnimValue = 0;

    private boolean mIsHide;

    private float maxRadius;
    private float mRotateTime = 0.2f;

    public VortexAnimUpdater(boolean isHide) {
        mIsHide = isHide;
        mAnimValue = mIsHide ? 0 : 1;

        mAnimator = new FloatValueAnimator(600);
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
        SurfaceModel model = surface.getModel();
        Vec topLeft = new Vec(3);
        model.getPosition(0, 0, topLeft);
        maxRadius = Math.max(
                getRadius(model, 0, 0),
                Math.max(
                        getRadius(model, 0, 1),
                        Math.max(getRadius(model, 1, 0), getRadius(model, 1, 1))
                )
        );
    }

    // 在开始绘制后调用（绘制第一帧后调用，一般动画可以在此开始）
    @Override
    protected void didStart(MagicSurface surface) {
        mAnimator.start(!mIsHide);
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
        float radius = (float) Math.sqrt(outPos.x() * outPos.x() + outPos.y() * outPos.y());
        float rTime = 1;
        if (mAnimValue <= mRotateTime) {
            rTime = mAnimValue / mRotateTime;
        } else {
            float offset = maxRadius * (mAnimValue - mRotateTime) / (1 - mRotateTime);
            float newRadius = radius - offset;
            if (newRadius < 0) {
                newRadius = 0;
            }
            updatePos(outPos, radius, newRadius);
            radius = newRadius;
        }
        float ratio = radius / maxRadius;
        float zOffset = (1 - ratio) * 2 * rTime;
        outPos.z(outPos.z() - zOffset * zOffset);
        updatePos(outPos, (float) Math.PI * 2 * rTime * (1 - ratio));
    }

    @Override
    protected void updateEnd(MagicSurface surface) {
        if (mAnimator.isStopped()) {
            stop();
        }
    }

    private void updatePos(Vec pos, float oldRadius, float newRadius) {
        if (newRadius == 0) {
            pos.setXY(0, 0);
            return;
        }
        float r = newRadius / oldRadius;
        pos.setXY(
                pos.x() * r,
                pos.y() * r
        );
    }

    private void updatePos(Vec pos, float angle) {
        if (pos.x() == 0 && pos.y() == 0) {
            return;
        }

        float r = (float) Math.sqrt(pos.x() * pos.x() + pos.y() * pos.y());
        float a0;
        int index = getIndex(pos);
        if (index < 0) {
            a0 = getA(pos);
        } else {
            a0 = (float) Math.asin(pos.x() / r);
            if (a0 > 0) {
                if (index > 0) {
                    a0 = (float) Math.PI - a0;
                }
            } else {
                if (index == 3) {
                    a0 = (float) Math.PI * 2 + a0;
                } else {
                    a0 = (float) Math.PI - a0;
                }
            }
        }
        float a1 = a0 + angle;

        double x = Math.sin(a1) * r;
        double y = Math.sqrt((x / Math.sin(a1)) * (x / Math.sin(a1)) - x * x);
        pos.setXY((float) x, (float) y);
        updatePosWithIndex(pos, getNewIndex(a1));
    }

    private int getNewIndex(double angle) {
        return ((int) (angle / ((float) Math.PI / 2))) % 4;
    }

    private void updatePosWithIndex(Vec vec, int index) {
        switch (index) {
            case 1:
            case 2:
                vec.y(-vec.y());
                break;
        }
    }

    private int getIndex(Vec pos) {
        if (pos.x() > 0 && pos.y() > 0) {
            return 0;
        } else if (pos.x() > 0 && pos.y() < 0) {
            return 1;
        } else if (pos.x() < 0 && pos.y() < 0) {
            return 2;
        } else if (pos.x() < 0 && pos.y() > 0) {
            return 3;
        } else {
            return -1;
        }
    }

    private float getA(Vec pos) {
        if (pos.x() == 0) {
            if (pos.y() > 0) {
                return 0;
            } else {
                return (float) Math.PI;
            }
        } else {
            if (pos.x() > 0) {
                return (float) Math.PI / 2;
            } else {
                return (float) Math.PI * 3 / 2;
            }
        }
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
