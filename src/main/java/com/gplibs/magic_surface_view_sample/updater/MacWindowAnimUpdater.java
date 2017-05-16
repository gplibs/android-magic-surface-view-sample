package com.gplibs.magic_surface_view_sample.updater;

import com.gplibs.magic_surface_view_sample.common.Direction;
import com.gplibs.magic_surface_view_sample.common.FloatValueAnimator;
import com.gplibs.magicsurfaceview.MagicSurface;
import com.gplibs.magicsurfaceview.MagicSurfaceModelUpdater;
import com.gplibs.magicsurfaceview.SurfaceModel;
import com.gplibs.magicsurfaceview.Vec;

/**
 * 模仿Mac窗口动画
 */
public class MacWindowAnimUpdater extends MagicSurfaceModelUpdater {

    private final float MODIFY_TARGET_TOTAL_TIME = 0.1f; // 动画使用 1/10 的时间完成下端收缩或者展开。

    private FloatValueAnimator mAnimator;
    private float mAnimValue = 0;

    private float mStartChangeOffsetTime;   // 开始移动位置的时间
    private boolean mIsHideAnim = true;
    private float mTarget;
    private int mDirection = Direction.BOTTOM;

    private Vec mToCenter = new Vec(3);
    private Vec mToBegin = new Vec(3);
    private Vec mToEnd = new Vec(3);
    private Vec mFromBegin = new Vec(3);
    private Vec mFromEnd = new Vec(3);
    private float mOffset;
    private boolean mIsVertical;
    private boolean mRangeOfSelf = false;
    private float mMoveLengthValue;
    private float mFromSize;

    public MacWindowAnimUpdater(boolean isHideAnim, int direction, float target, boolean rangeOfSelf) {
        super();
        mIsHideAnim = isHideAnim;
        mDirection = direction;
        mTarget = target;
        mRangeOfSelf = rangeOfSelf;

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
        switch (mDirection) {
            case Direction.LEFT:
                model.getPosition(0, model.getColLineCount() - 1, mFromBegin);
                model.getPosition(model.getRowLineCount() - 1, model.getColLineCount() - 1, mFromEnd);
                if (mRangeOfSelf) {
                    surface.getPosition(0, mTarget, mToCenter);
                } else {
                    surface.getScene().getPosition(0, mTarget, mToCenter);
                }
                break;
            case Direction.RIGHT:
                model.getPosition(0, 0, mFromBegin);
                model.getPosition(model.getRowLineCount() - 1, 0, mFromEnd);
                if (mRangeOfSelf) {
                    surface.getPosition(1, mTarget, mToCenter);
                } else {
                    surface.getScene().getPosition(1, mTarget, mToCenter);
                }
                break;
            case Direction.TOP:
                model.getPosition(model.getRowLineCount() - 1, 0, mFromBegin);
                model.getPosition(model.getRowLineCount() - 1, model.getColLineCount() - 1, mFromEnd);
                if (mRangeOfSelf) {
                    surface.getPosition(mTarget, 0, mToCenter);
                } else {
                    surface.getScene().getPosition(mTarget, 0, mToCenter);
                }
                break;
            case Direction.BOTTOM:
                model.getPosition(0, 0, mFromBegin);
                model.getPosition(0, model.getColLineCount() - 1, mFromEnd);
                if (mRangeOfSelf) {
                    surface.getPosition(mTarget, 1, mToCenter);
                } else {
                    surface.getScene().getPosition(mTarget, 1, mToCenter);
                }
                break;
        }

        mAnimValue = mIsHideAnim ? 0 : 1;
        mIsVertical = Direction.isVertical(mDirection);
        float h;
        if (mIsVertical) {
            h = model.getHeight();
            mFromSize = model.getWidth();
            mMoveLengthValue = mFromBegin.y() - mToCenter.y();
        } else {
            h = model.getWidth();
            mFromSize = model.getHeight();
            mMoveLengthValue = mFromBegin.x() - mToCenter.x();
        }
        float r = Math.abs(h / mMoveLengthValue);
        mStartChangeOffsetTime =  r * r * MODIFY_TARGET_TOTAL_TIME;
    }

    // 在开始绘制后调用（绘制第一帧后调用，一般动画可以在此开始）
    @Override
    protected void didStart(MagicSurface surface) {
        mAnimator.start(!mIsHideAnim);
    }

    // 当调用Updater 的 stop() 方法之后，真正停止后会回调此方法
    @Override
    protected void didStop(MagicSurface surface) {
        mAnimator.stop();
    }

    @Override
    protected void updateBegin(MagicSurface surface) {
        updateTarget(surface);
        updateOffset();
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
        if (mIsVertical) {
            outPos.y(outPos.y() - mOffset);
            float y = Math.abs(outPos.y());
            float t = mRangeOfSelf ? surface.getHeight() / 2 : surface.getScene().getHeight() / 2;
            if (y > t) {
                outPos.y(mOffset > 0 ? -t : t);
            }
        } else {
            outPos.x(outPos.x() - mOffset);
            float x = Math.abs(outPos.x());
            float t = mRangeOfSelf ? surface.getWidth() / 2 : surface.getScene().getWidth() / 2;
            if (x > t) {
                outPos.x(mOffset > 0 ? -t : t);
            }
        }
        updatePosition(outPos, mFromBegin, mFromEnd, mToBegin, mToEnd);
    }

    @Override
    protected void updateEnd(MagicSurface surface) {
        if (mAnimator.isStopped()) {
            // 调用 stop 方法，通知框架开始停止此 updater, 停止后会调用 didStop 方法。
            stop();
        }
    }

    private void updateTarget(MagicSurface surface) {
        SurfaceModel model = surface.getModel();

        float r = MODIFY_TARGET_TOTAL_TIME - mAnimValue;
        if (r < 0) {
            r = 0;
        }
        r /= MODIFY_TARGET_TOTAL_TIME;
        float range = r * (mIsVertical ? model.getWidth() : model.getHeight());
        if (range < 0.02f) {
            range = 0.02f;
        }

        if (mIsVertical) {
            float modelCenterX = mFromBegin.x() + model.getWidth() / 2;
            float targetX = modelCenterX + (1 - r) * (mToCenter.x() - modelCenterX);
            mToBegin.setXY(targetX - range / 2, mToCenter.y());
            mToEnd.setXY(targetX + range / 2, mToCenter.y());
        } else {
            float modelCenterY = mFromBegin.y() - model.getHeight() / 2;
            float targetY = modelCenterY + (1 - r) * (mToCenter.y() - modelCenterY);
            mToBegin.setXY(mToCenter.x(), targetY + range / 2);
            mToEnd.setXY(mToCenter.x(), targetY - range / 2);
        }
    }

    private void updateOffset() {
        if (mAnimValue > mStartChangeOffsetTime) {
            float r = (mAnimValue - mStartChangeOffsetTime) / (1 - mStartChangeOffsetTime);
            mOffset = mMoveLengthValue * r;
        } else {
            mOffset = 0;
        }
    }

    private void updatePosition(
            Vec pos, Vec fromBegin, Vec fromEnd,
            Vec toBegin, Vec toEnd) {
        float coordBegin = getNewPos(pos, fromBegin, toBegin);
        float coordEnd = getNewPos(pos, fromEnd, toEnd);
        float r = mIsVertical ? ((pos.x() - fromBegin.x()) / mFromSize) : ((fromBegin.y() - pos.y()) / mFromSize);
        float coord = coordBegin + (coordEnd - coordBegin) * r;
        if (mIsVertical) {
            pos.x(coord);
        } else {
            pos.y(coord);
        }
    }

    private float getNewPos(Vec pos, Vec start, Vec end) {
        float halfRatio = 0.5f;
        float ratio;
        if (mIsVertical) {
            ratio = (pos.y() - start.y()) / (end.y() - start.y());
        } else {
            ratio = (pos.x() - start.x()) / (end.x() - start.x());
        }
        float half;
        if (mIsVertical) {
            half = (end.x() - start.x()) / 2;
        } else {
            half = (end.y() - start.y()) / 2;
        }
        float coord = (mIsVertical ? start.x() : start.y());
        if (ratio < halfRatio) {
            float r = ratio / halfRatio;
            coord += r * r * half;
        } else {
            float r = (1 - ratio) / (1 - halfRatio);
            coord += (2 - r * r) * half;
        }
        return coord;
    }

}
