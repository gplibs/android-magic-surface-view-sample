package com.gplibs.magic_surface_view_sample;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.gplibs.magic_surface_view_sample.common.MagicActivity;
import com.gplibs.magicsurfaceview.MagicScene;
import com.gplibs.magicsurfaceview.MagicSceneBuilder;
import com.gplibs.magicsurfaceview.MagicSurface;
import com.gplibs.magicsurfaceview.MagicSurfaceView;
import com.gplibs.magicsurfaceview.PointLight;
import com.gplibs.magicsurfaceview.ReusableVec;
import com.gplibs.magicsurfaceview.Vec;
import com.gplibs.magicsurfaceview.VecPool;

public class LightSampleActivity extends MagicActivity {

    private View mViewContainer;
    private MagicSurfaceView mSurfaceView;
    private TextView mTvContent;

    private TextView mTvPos;
    private TextView mTvShininess;
    private SeekBar mSbZ;
    private SeekBar mSbShininess;
    private RadioGroup mRadioGroup;
    private ToggleButton mToggleButton;

    private MagicScene mScene;          // 场景对象
    private MagicSurface mSurface;      // 渲染对象
    private PointLight mPointLight;     // 点光源对象
    private Vec mLightPos = new Vec(3); // 光源位置

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_light_sample);
        setTitle("LightSample");

        initViews();
    }

    private void initViews() {
        mSurfaceView = (MagicSurfaceView) findViewById(R.id.surface_view);
        mTvContent = (TextView) findViewById(R.id.tv_content);

        mViewContainer = findViewById(R.id.view_container);
        mViewContainer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                moveLight(event.getX(), event.getY());
                return true;
            }
        });

        mTvPos = (TextView) findViewById(R.id.tv_pos);
        mTvShininess = (TextView) findViewById(R.id.tv_shininess);

        mSbZ = (SeekBar) findViewById(R.id.sb_z);
        mSbShininess = (SeekBar) findViewById(R.id.sb_shininess);
        SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (seekBar == mSbZ) {
                    moveLightZ(0.2f + 0.8f * progress / 100.f);
                    mTvPos.setText("z:" + mLightPos.z());
                } else {
                    mSurface.setShininess(progress);
                    mTvShininess.setText("shininess:" + progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };
        mSbZ.setOnSeekBarChangeListener(onSeekBarChangeListener);
        mSbShininess.setOnSeekBarChangeListener(onSeekBarChangeListener);

        mRadioGroup = (RadioGroup) findViewById(R.id.radio_group);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                int color;
                switch (checkedId) {
                    case R.id.rb_red:
                        color = 0XFF800000;
                        break;
                    case R.id.rb_green:
                        color = 0XFF008000;
                        break;
                    case R.id.rb_blue:
                        color = 0XFF000080;
                        break;
                    default:
                        color = 0XFF808080;
                        break;
                }
                updateColor(color);
            }
        });

        mToggleButton = (ToggleButton) findViewById(R.id.toggle_button);
        mToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mPointLight.setEnable(isChecked);
            }
        });
    }

    private void render() {
        // 光源默认位置 (0.f, 0.f, 0.2f): MagicSurfaceView 正中, Z轴向屏幕外0.2
        mLightPos.setXYZ(0, 0, 0.2f);
        mPointLight = new PointLight(0XFF808080, mLightPos);
        mSurface = new MagicSurface(mTvContent);
        mSurface.setShininess(128);
        mSurface.setGrid(50, 50);
        mScene = new MagicSceneBuilder(mSurfaceView)
                .addSurfaces(mSurface)
                .ambientColor(0XFF333333) // 环境光，设置暗些灯光效果才明显
                .addLights(mPointLight)
                .build();
        mSurfaceView.render(mScene);
    }

    public void moveLight(float x, float y) {
        ReusableVec sceneTopLeft = VecPool.get(3);
        mScene.getPosition(0, 0, sceneTopLeft);
        float sceneX = sceneTopLeft.x() + mScene.getWidth() * x / mViewContainer.getWidth();
        float sceneY = sceneTopLeft.y() - mScene.getHeight() * y / mViewContainer.getHeight(); // 场景Y轴向上所以要减
        sceneTopLeft.free();
        mLightPos.setXY(sceneX, sceneY);
        updatePosition();
    }

    // 修改灯光位置Z坐标
    private void moveLightZ(float z) {
        mLightPos.z(z);
        updatePosition();
    }

    // 修改灯光颜色
    private void updateColor(int color) {
        mPointLight.setColor(color);
    }

    // 修改灯光水平位置
    private void updatePosition() {
        mPointLight.setPosition(mLightPos);
    }

    @Override
    protected void onPageAnimEnd() {
        render();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSurfaceView.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放场景资源
        mScene.release();
        mScene = null;
    }

    @Override
    public void onBackPressed() {
        mSurfaceView.setVisibility(View.INVISIBLE);
        mViewContainer.postDelayed(new Runnable() {
            @Override
            public void run() {
                LightSampleActivity.super.onBackPressed();
            }
        }, 50);
    }
}
