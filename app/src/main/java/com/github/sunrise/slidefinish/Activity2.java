package com.github.sunrise.slidefinish;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.github.sunrise.slidefinishlayout.SlideFinishManager;


public class Activity2 extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        SlideFinishManager.getInstance().setShadowOrientation(SlideFinishManager.RIGHT);
        findViewById(R.id.tv2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Activity2.this, Activity3.class));
            }
        });
    }
}
