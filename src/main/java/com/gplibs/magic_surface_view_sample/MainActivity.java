package com.gplibs.magic_surface_view_sample;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.gplibs.magic_surface_view_sample.common.Direction;
import com.gplibs.magic_surface_view_sample.common.MagicActivity;
import com.gplibs.magic_surface_view_sample.mac_window.MacWindowAnimActivity;
import com.gplibs.magic_surface_view_sample.updater.VortexAnimUpdater;
import com.gplibs.magic_surface_view_sample.updater.WaveAnimUpdater;
import com.gplibs.magicsurfaceview.MagicSurfaceModelUpdater;

public class MainActivity extends MagicActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("MagicSurfaceView");

        findViewById(R.id.btn_mac_window_anim).setOnClickListener(this);
        findViewById(R.id.btn_wave_anim).setOnClickListener(this);
        findViewById(R.id.btn_vortex_anim).setOnClickListener(this);
        findViewById(R.id.btn_matrix_anim_sample).setOnClickListener(this);
        findViewById(R.id.btn_light_sample).setOnClickListener(this);
        findViewById(R.id.tv_address).setOnClickListener(this);
    }

    @Override
    protected MagicSurfaceModelUpdater getPageUpdater(boolean isHide) {
        if (isHide) {
            return new VortexAnimUpdater(true);
        } else {
            return new WaveAnimUpdater(false, Direction.RIGHT);
        }
    }

    @Override
    protected int pageAnimRowCount() {
        return 60;
    }

    @Override
    protected int pageAnimColCount() {
        return 60;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_mac_window_anim:
                start(MacWindowAnimActivity.class);
                break;
            case R.id.btn_wave_anim:
                start(WaveAnimActivity.class);
                break;
            case R.id.btn_vortex_anim:
                start(VortexAnimActivity.class);
                break;
            case R.id.btn_matrix_anim_sample:
                start(MatrixAnimSampleActivity.class);
                break;
            case R.id.btn_light_sample:
                start(LightSampleActivity.class);
                break;
            case R.id.tv_address:
                openGitHub();
                break;
            default:
                break;
        }
    }

    private void start(Class<? extends Activity> activityClass) {
        startActivity(new Intent(this, activityClass));
    }

    private void openGitHub() {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.setData(Uri.parse("http://github.com/gplibs"));
        startActivity(intent);
    }
}
