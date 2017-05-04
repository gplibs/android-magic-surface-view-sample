package com.gplibs.magic_surface_view_sample;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.gplibs.magic_surface_view_sample.common.FloatValueAnimator;
import com.gplibs.magic_surface_view_sample.common.MagicActivity;
import com.gplibs.magicsurfaceview.MagicScene;
import com.gplibs.magicsurfaceview.MagicSurface;
import com.gplibs.magicsurfaceview.MagicSurfaceMatrixUpdater;
import com.gplibs.magicsurfaceview.MagicSurfaceView;
import com.gplibs.magicsurfaceview.MagicUpdaterListener;
import com.gplibs.magicsurfaceview.Vec;

/**
 * 矩形变换示例
 */
public class MatrixAnimSampleActivity extends MagicActivity {

    private MagicScene mScene;
    private MagicSurfaceView mSurfaceView;
    private TextView mTvContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_matrix_anim_sample);
        setTitle("MatrixAnimSample");

        mSurfaceView = (MagicSurfaceView) findViewById(R.id.surface_view);
        mTvContent = (TextView) findViewById(R.id.tv_content);
        findViewById(R.id.view_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                start();
            }
        });
    }

    private void start() {
        MatrixUpdater updater = new MatrixUpdater();
        updater.addListener(new MagicUpdaterListener() {
            @Override
            public void onStart() {
                mTvContent.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onStop() {
                mTvContent.setVisibility(View.VISIBLE);
                mSurfaceView.setVisibility(View.GONE);
                // 释放场景资源
                mScene.release();
                mScene = null;
            }
        });
        MagicSurface s = new MagicSurface(mTvContent)
                .setGrid(2, 2)
                .drawGrid(false)
                .setMatrixUpdater(updater);
        mSurfaceView.setVisibility(View.VISIBLE);
        mScene = mSurfaceView.render(s);
    }

    /**
     * 矩阵变换示例
     */
    private static class MatrixUpdater extends MagicSurfaceMatrixUpdater {

        private FloatValueAnimator mAnimator;
        private float mAnimValue = 0;

        // y轴
        private Vec mAxis = new Vec(0, 1, 0);
        // 缩放量
        private Vec mScale = new Vec(1, 1, 1);
        // 位移量
        private Vec mTranslate = new Vec(0, 0, 0);

        public MatrixUpdater() {
            mAnimator = new FloatValueAnimator(1000);
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
            mAnimValue = 0;
            mAnimator.reset();
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

        /**
         * 在此方法里进行矩阵变换
         *
         * @param offset 为 model 相对场景中心的坐标偏移量, 如果不进行 offset 位移， model 就会显示在场景中心；
         *
         *               当使用 View 构造 MagicSurface 时，
         *               View中心位置 相对 MagicSurfaceView中心位置 的坐标偏移量 在场景坐标系中的表现就是 offset。
         *
         * @param matrix model 矩阵
         */
        @Override
        protected void updateMatrix(MagicSurface surface, Vec offset, float[] matrix) {

            // 旋转角度
            float angle = 360.f * 2 * mAnimValue;
            float scale;
            float translateY;

            if (mAnimValue < 0.5f) { // 前一半时间
                // 从 1倍 缩小到 0.5倍
                scale = 0.5f + 0.5f * (0.5f - mAnimValue) / 0.5f;
                // 从 offset 处移到其y轴上方 0.8 位置处
                translateY = 0.8f * mAnimValue / 0.5f;
            } else { // 后一半时间
                // 从 0.5倍 放大到 1倍
                scale = 0.5f + 0.5f * (mAnimValue - 0.5f) / 0.5f;
                // 从 offset 上方 0.8 位置移到 offset 处
                translateY = 0.8f * (1 -  mAnimValue) / 0.5f;
            }

            mScale.setXYZ(scale, scale, 1);

            mTranslate.copy(offset);
            mTranslate.add(0, translateY, 0);

            // 重置matrix
            reset(matrix);
            // 缩放
            scale(matrix, mScale);
            // 位移
            translate(matrix, mTranslate);
            // 绕 mAxis 旋转 angle 度。
            rotate(matrix, mAxis, angle);

            if (mAnimator.isStopped()) {
                stop();
            }
        }
    }
}
