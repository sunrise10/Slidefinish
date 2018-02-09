package com.github.sunrise.slidefinish;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.github.sunrise.slidefinishlayout.SlideFinishManager;

/**
 * Created by yf on 2018/1/8.
 * 描述：BaseActivity
 */

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        SlideFinishManager.getInstance().bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SlideFinishManager.getInstance().unbind();
    }
}
