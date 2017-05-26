package com.gplibs.magic_surface_view_sample.common;

import com.gplibs.magicsurfaceview.MagicBaseSurface;
import com.gplibs.magicsurfaceview.ReusableVec;
import com.gplibs.magicsurfaceview.Vec;
import com.gplibs.magicsurfaceview.VecPool;

public class AnimHelper {

    public static final int MOVE_TYPE_SCENE = 0;
    public static final int MOVE_TYPE_SELF = 1;

    private float mAnimValue;
    private int mDirection;
    private MagicBaseSurface mSurface;
    private float mTotalDistanceScene;
    private float mTotalDistanceSelf;
    private float mBeginPos = 0, mReverseBeginPos = 0, mSurfaceSize = 1;

    public AnimHelper(MagicBaseSurface surface, int direction, boolean isHide) {
        mSurface = surface;
        mDirection = direction;
        mAnimValue = isHide ? 0 : 1;
        init();
    }

    public void update(float animValue) {
        mAnimValue = animValue;
    }

    public float getStartAnimTime(Vec pos, boolean reverse, float totalStartingTime) {
        float p = (mDirection == Direction.TOP || mDirection == Direction.BOTTOM) ? pos.y() : pos.x();
        float begin = reverse ? mReverseBeginPos : mBeginPos;
        return Math.abs(p - begin) / mSurfaceSize * totalStartingTime;
    }

    public float getStartAnimTime(int index, int count, boolean reverse, float totalStartingTime) {
        float start = 0;
        float end = count - 1;
        switch (mDirection) {
            case Direction.TOP:
            case Direction.LEFT:
                start = reverse ? count - 1 : 0;
                end = reverse ? 0 : count - 1;
                break;
            case Direction.RIGHT:
            case Direction.BOTTOM:
                start = reverse ? 0 : count - 1;
                end = reverse ? count - 1 : 0;
                break;
        }
        return Math.abs((index - start) / (end - start)) * totalStartingTime;
    }

    public float getAnimProgress(float startTime, float animTime) {
        float endTime = startTime + animTime;
        if (endTime > 1) {
            endTime  = 1;
        }
        if (mAnimValue < startTime ) {
            return 0;
        } else if (mAnimValue >= startTime && mAnimValue < endTime) {
            float r = (mAnimValue - startTime) / (endTime - startTime);
            return r * r;
        } else {
            return 1;
        }
    }

    public float getMoveDistance(int moveType, float beginTime, float movingTime) {
        return getMoveDistance(moveType == MOVE_TYPE_SELF ? mTotalDistanceSelf : mTotalDistanceScene, beginTime, movingTime);
    }

    public float getMoveDistance(float totalDistance, float beginTime, float movingTime) {
        float r = getAnimProgress(beginTime, movingTime);
        if (mDirection == Direction.TOP || mDirection == Direction.RIGHT) {
            return totalDistance * r;
        } else {
            return -totalDistance * r;
        }
    }

    private void init() {
        ReusableVec surfacePos = VecPool.get(3);
        ReusableVec scenePos = VecPool.get(3);
        ReusableVec surfacePos1 = VecPool.get(3);
        boolean isVertical = false;

        if (mDirection == Direction.TOP) {
            mSurface.getPosition(0, 1, surfacePos);
            mSurface.getScene().getPosition(0, 0, scenePos);
            mSurface.getPosition(0, 0, surfacePos1);
            isVertical = true;
        } else if (mDirection == Direction.BOTTOM) {
            mSurface.getPosition(0, 0, surfacePos);
            mSurface.getScene().getPosition(0, 1, scenePos);
            mSurface.getPosition(0, 1, surfacePos1);
            isVertical = true;
        } else if (mDirection == Direction.LEFT) {
            mSurface.getPosition(1, 0, surfacePos);
            mSurface.getScene().getPosition(0, 0, scenePos);
            mSurface.getPosition(0, 0, surfacePos1);
            isVertical = false;
        } else if (mDirection == Direction.RIGHT) {
            mSurface.getPosition(0, 0, surfacePos);
            mSurface.getScene().getPosition(1, 0, scenePos);
            mSurface.getPosition(1, 0, surfacePos1);
            isVertical = false;
        }

        if (isVertical) {
            mReverseBeginPos = surfacePos.y();
            mBeginPos = surfacePos1.y();
            mSurfaceSize = mSurface.getHeight();
            mTotalDistanceScene = Math.abs(scenePos.y() - surfacePos.y());
            mTotalDistanceSelf = Math.abs(surfacePos1.y() - surfacePos.y());
        } else {
            mReverseBeginPos = surfacePos.x();
            mBeginPos = surfacePos1.x();
            mSurfaceSize = mSurface.getWidth();
            mTotalDistanceScene = Math.abs(scenePos.x() - surfacePos.x());
            mTotalDistanceSelf = Math.abs(surfacePos1.x() - surfacePos.x());
        }

        scenePos.free();
        surfacePos1.free();
        surfacePos.free();
    }
}
