package com.github.sunrise.slidefinish;

import android.os.Bundle;

import com.github.sunrise.slidefinishlayout.SlideFinishManager;

public class Activity3 extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        SlideFinishManager.getInstance().setShadowOrientation(SlideFinishManager.LEFT);
    }
}
