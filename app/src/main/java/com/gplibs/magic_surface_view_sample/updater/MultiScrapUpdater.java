package com.gplibs.magic_surface_view_sample.updater;

import com.gplibs.magic_surface_view_sample.common.AnimHelper;
import com.gplibs.magic_surface_view_sample.common.Direction;
import com.gplibs.magic_surface_view_sample.common.FloatValueAnimator;
import com.gplibs.magic_surface_view_sample.common.RandomNumber;
import com.gplibs.magicsurfaceview.MagicMultiSurface;
import com.gplibs.magicsurfaceview.MagicMultiSurfaceUpdater;
import com.gplibs.magicsurfaceview.Vec;

public class MultiScrapUpdater extends MagicMultiSurfaceUpdater {

    private final float ANIM_TIME = 0.5f;

    private FloatValueAnimator mAnimator = new FloatValueAnimator(1000);
    private AnimHelper mAnimHelper;
    private int mDirection;
    private boolean mIsHide;
    private boolean mIsVertical;
    private RandomNumber[] mRandoms;
    private RandomNumber mRandom;
    private Vec mAxis = new Vec(0.5f, 0.5f, 1);

    public MultiScrapUpdater(boolean isHide, int direction) {
        mIsHide = isHide;
        mDirection = direction;
        mAnimator.addListener(new FloatValueAnimator.FloatValueAnimatorListener() {
            @Override
            public void onAnimationUpdate(float value) {
                mAnimHelper.update(value);
                notifyChanged();
            }

            @Override
            public void onStop() {
                notifyChanged();
            }
        });
    }

    @Override
    protected void willStart(MagicMultiSurface surface) {
        surface.getScene().setCameraZ(2.f);
        mAnimHelper = new AnimHelper(surface, mDirection, mIsHide);
        mIsVertical = Direction.isVertical(mDirection);
        int count = mIsVertical ? surface.getRows() : surface.getCols();
        float t = (1 - ANIM_TIME) / count;
        mRandoms = new RandomNumber[count];
        for (int i = 0; i < mRandoms.length; ++i) {
            int n = mIsVertical ? surface.getCols(): surface.getRows();
            float time = mAnimHelper.getStartAnimTime(i, mRandoms.length, false, 1 - ANIM_TIME - t);
            mRandoms[i] = new RandomNumber(n, time, time + t);
        }
        mRandom = new RandomNumber(surface.getRows() * surface.getCols(), 0, 1);
    }

    @Override
    protected void didStart(MagicMultiSurface surface) {
        mAnimator.start(!mIsHide);
    }

    @Override
    protected void didStop(MagicMultiSurface surface) {

    }

    @Override
    protected void updateBegin(MagicMultiSurface surface) {

    }

    @Override
    protected void update(MagicMultiSurface surface, int r, int c, float[] matrix, Vec offset, Vec color) {
        float startTime = mRandoms[mIsVertical ? r : c].get(mIsVertical ? c : r);
        float ratio = mAnimHelper.getAnimProgress(startTime, ANIM_TIME);
        float ratio1 = mRandom.get(r * surface.getCols() + c);
        color.a(1 - ratio);
        float angle = 360 * ratio1 * ratio;
        float d = ratio1 * mAnimHelper.getMoveDistance(AnimHelper.MOVE_TYPE_SCENE, startTime, ANIM_TIME);
        if (mIsVertical) {
            offset.y(offset.y() + d);
            offset.x(offset.x() + ratio1 * ratio * surface.getWidth());
        } else {
            offset.x(offset.x() + d);
        }
        offset.z(offset.z() + 0.5f * ratio1 * ratio);
        reset(matrix);
        translate(matrix, offset);
        rotate(matrix, mAxis, angle);
    }

    @Override
    protected void updateEnd(MagicMultiSurface surface) {
        if (mAnimator.isStopped()) {
            stop();
        }
    }
}
