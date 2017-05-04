package com.gplibs.magic_surface_view_sample.updater;

import com.gplibs.magic_surface_view_sample.common.Direction;
import com.gplibs.magic_surface_view_sample.common.FloatValueAnimator;
import com.gplibs.magicsurfaceview.MagicSurface;
import com.gplibs.magicsurfaceview.MagicSurfaceModelUpdater;
import com.gplibs.magicsurfaceview.SurfaceModel;
import com.gplibs.magicsurfaceview.Vec;

/**
 * WaveAnimUpdater 将 model 中的每个点按PI范围内正弦波移动。
 * 并在移动过程中修改透明度。
 */
public class WaveAnimUpdater extends MagicSurfaceModelUpdater {

    // 每个点移动所需时间占整个动画时间的比例
    private final float MOVING_TIME = 0.3f;

    // 是否为隐藏动画
    private boolean mIsHide;
    // 动画方向
    private int mDirection;

    // 动画为显示过程时，Model中开始移动的点
    private float mModelBeginPos;
    // 动画横向时为Model宽度，纵向时为Model高度
    private float mModelSize;
    // 要移动的距离
    private float mDistance;

    private FloatValueAnimator mAnimator;
    private float mAnimValue = 0;
    private boolean mIsVertical;

    public WaveAnimUpdater(boolean isHide, int direction) {
        mIsHide = isHide;
        mDirection = direction;

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

    public void setDuration(int ms) {
        mAnimator.setDuration(ms);
    }

    // 在绘制第一帧之前调用 (可以在此方法里进行一些初始化操作)
    @Override
    protected void willStart(MagicSurface surface) {

        mAnimValue = mIsHide ? 0 : 1;

        SurfaceModel model = surface.getModel();
        mIsVertical = Direction.isVertical(mDirection);
        Vec scenePos = new Vec(3);
        Vec modelPos = new Vec(3);
        switch (mDirection) {
            case Direction.LEFT:
                model.getPosition(0, model.getColLineCount() - 1, modelPos);
                surface.getScene().getPosition(0, 0, scenePos);
                mModelBeginPos = modelPos.x();
                mDistance = scenePos.x() - modelPos.x();
                mModelSize = model.getWidth();
                break;
            case Direction.RIGHT:
                model.getPosition(0, 0, modelPos);
                surface.getScene().getPosition(1, 0, scenePos);
                mModelBeginPos = modelPos.x();
                mDistance = scenePos.x() - modelPos.x();
                mModelSize = model.getWidth();
                break;
            case Direction.TOP:
                model.getPosition(model.getRowLineCount() - 1, 0, modelPos);
                surface.getScene().getPosition(0, 0, scenePos);
                mModelBeginPos = modelPos.y();
                mDistance = scenePos.y() - modelPos.y();
                mModelSize = model.getHeight();
                break;
            case Direction.BOTTOM:
                model.getPosition(0, 0, modelPos);
                surface.getScene().getPosition(1, 1, scenePos);
                mModelBeginPos = modelPos.y();
                mDistance = scenePos.y() - modelPos.y();
                mModelSize = model.getHeight();
                break;
        }
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
        float coord = mIsVertical ? outPos.y() : outPos.x();
        float ratio = 1 - Math.abs((mModelBeginPos - coord) / mModelSize);
        float startTime = (1 - MOVING_TIME) * ratio;
        float t = mAnimValue - startTime;
        if (t < 0) {
            t = 0;
        }
        t /= MOVING_TIME;
        if (t > 1) {
            t = 1;
        }
        // t 为当前 已经移动的时间 与 总移动时间(MOVING_TIME) 的比, 范围为0~1； 进行平方是让其变化过程不是线性的，范围还是0~1。
        t = t * t;
        float offset = t * mDistance;
        if (mIsVertical) {
            outPos.y(outPos.y() + offset);
        } else {
            outPos.x(outPos.x() + offset);
        }
        // 按水平移动距离修改 Z 坐标, 形成 PI 范围内的正弦波
        outPos.z((float) Math.sin(Math.PI * t) / 2);
        // 按水平移动距离修改透明度
        outColor.a(1 - t);
    }

    @Override
    protected void updateEnd(MagicSurface surface) {
        if (mAnimator.isStopped()) {
            // 调用 stop 方法，通知框架开始停止此 updater, 停止后会调用 didStop 方法。
            stop();
        }
    }

}
